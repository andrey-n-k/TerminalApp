package com.arellomobile.terminal.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import com.arellomobile.terminal.R;
import com.arellomobile.terminal.data.data.LangPack;
import com.arellomobile.terminal.data.manager.datamanager.DataManager;
import com.arellomobile.terminal.helper.application.TerminalApplication;
import com.arellomobile.terminal.helper.uihelper.BasicActivity;
import com.arellomobile.terminal.tasks.LoadLangPack;

/**
 * User: AndreyKo
 * Date: 14.05.12
 */
public class AppLoader extends BasicActivity
{
    private boolean cancel = false;
    private TerminalApplication terminalApplication;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        terminalApplication = (TerminalApplication) getApplication();

        // start app thread
        Thread loadThread = new Thread(new Runnable()
        {
            public void run()
            {
                try
                {
                    Thread.sleep(2000);
                }
                catch (InterruptedException e) {/*pass*/}
                if (cancel)
                {
                    return;
                }
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        startApp();
                    }
                });

            }
        });
        loadThread.start();
    }

    private void startApp()
    {
        if (terminalApplication.getDataManager().isLogIn(this))
        {
            startActivity(new Intent(this, SetTripActivity.class).putExtra(BasicActivity.COMPANY_ID_KEY, terminalApplication.getDataManager().getCompanyId(this)));
        }
        else
        {
            startActivity(new Intent(this, ZeroScreenActivity.class));
        }
        finish();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        cancel = true;
    }
}
