package com.arellomobile.terminal.helper.uihelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.arellomobile.terminal.R;
import com.arellomobile.terminal.data.data.LangPack;
import com.arellomobile.terminal.data.manager.cache.StorageApi;
import com.arellomobile.terminal.data.manager.datamanager.DataManager;
import com.arellomobile.terminal.data.manager.langservice.LangFactory;
import com.arellomobile.terminal.data.manager.service.ServerApi;
import com.arellomobile.terminal.helper.application.TerminalApplication;
import com.arellomobile.terminal.service.CommitService;
import com.arellomobile.terminal.tasks.callback.AsyncTaskCallBack;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * User: AndreyKo
 * Date: 12.05.12
 */
public abstract class BasicActivity extends Activity implements AsyncTaskCallBack
{
    protected View progress;

    protected static final int UPDATE = 0;
    protected static final long TIME = 10000;

    public static final int RESULT_CODE_OK = 1002;
    public static final int RESULT_CODE_CANCEL = 1003;

    public static final String COMPANY_ID_KEY = "company_key";

    protected ServerApi mServerApi;
    protected StorageApi mStorageApi;
    protected DataManager mDataManager;
    protected LangFactory mLangFactory;
    protected LangPack mLangPack;

    private boolean mIsUserLeave;

    private static Map<Integer, String> mDaysOfWeek;
    static
    {
        mDaysOfWeek = new HashMap<Integer, String>();
        mDaysOfWeek.put(0, "MonLabel");
        mDaysOfWeek.put(1, "TueLabel");
        mDaysOfWeek.put(2, "WedLabel");
        mDaysOfWeek.put(3, "ThuLabel");
        mDaysOfWeek.put(4, "FriLabel");
        mDaysOfWeek.put(5, "SatLabel");
        mDaysOfWeek.put(6, "SunLabel");
    }
    private static Map<Integer, String> mMonths;
    static
    {
        mMonths = new HashMap<Integer, String>();
        mMonths.put(0, "JanLabel");
        mMonths.put(1, "FebLabel");
        mMonths.put(2, "MarLabel");
        mMonths.put(3, "AprLabel");
        mMonths.put(4, "MayLabel");
        mMonths.put(5, "JunLabel");
        mMonths.put(6, "JulLabel");
        mMonths.put(7, "AugLabel");
        mMonths.put(8, "SepLabel");
        mMonths.put(9, "OctLabel");
        mMonths.put(10, "NovLabel");
        mMonths.put(11, "DecLabel");
    }

    @Override
    public void onCreate(Bundle savedState)
    {
        super.onCreate(savedState);

        startService(new Intent(this, CommitService.class));

        TerminalApplication terminalApplication = (TerminalApplication) getApplicationContext();
        mServerApi = terminalApplication.getServerApi();
        mStorageApi = terminalApplication.getStorageApi();
        mDataManager = terminalApplication.getDataManager();
        mLangFactory = terminalApplication.getLangFactory();

        mLangPack = mStorageApi.getLangPack();
        if (mLangPack != null)
        {
            mLangFactory.setJson(mLangPack.getJsonString());
            mLangFactory.setLangid(mDataManager.getLanguage(this));
        }
    }

    @Override
    public void setContentView(int layoutResID)
    {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock lock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE);
        lock.disableKeyguard();


        RelativeLayout rootLayout = new RelativeLayout(this);
        rootLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));

        setContentView(rootLayout);

        LayoutInflater inflater = getLayoutInflater();
        progress = inflater.inflate(R.layout.progress_layout, null);
        progress.setLayoutParams(
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        View content = inflater.inflate(layoutResID, null);
        content.setLayoutParams(
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));

        rootLayout.addView(content);
        rootLayout.addView(progress);
    }

    @Override
    public void showProgress()
    {
        progress.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgress()
    {
        progress.setVisibility(View.GONE);
    }

    private boolean mIsVisible = false;

    @Override
    public void onResume()
    {
        super.onResume();

        mIsVisible = true;
    }

    @Override
    public void onPause()
    {
        super.onPause();

        mIsVisible = true;
    }

    @Override
    public void onServerSideError(String errorMessage)
    {
        new AlertDialog.Builder(this).setMessage(errorMessage).setPositiveButton(mLangFactory.getString("okLabel"), null).show();
    }

    @Override
    public void onServerError(String errorMessage)
    {
        new AlertDialog.Builder(this).setMessage(errorMessage).setPositiveButton(mLangFactory.getString("okLabel"), null).show();
    }

    @Override
    public void onNetworkError()
    {
        new AlertDialog.Builder(this).setMessage(mLangFactory.getString("networkErrorMessage"))
                .setPositiveButton(mLangFactory.getString("okLabel"), null).show();
    }

    @Override
    public void onUnknownError()
    {
        new AlertDialog.Builder(this).setMessage(mLangFactory.getString("oopsError"))
                .setPositiveButton(mLangFactory.getString("okLabel"), null).show();
    }

    protected void setCurrentDataTime(TextView date, TextView time)
    {
        Calendar calendar = Calendar.getInstance(new Locale(mDataManager.getLanguage(getApplicationContext())));

        String dayOfWeek = mLangFactory.getString(mDaysOfWeek.get(Integer.valueOf(calendar.get(Calendar.DAY_OF_WEEK))));
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String month = mLangFactory.getString(mMonths.get(Integer.valueOf(calendar.get(Calendar.MONTH))));

        date.setText(String.format("%s %d %s", dayOfWeek, day, month));
        time.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.getTime()));
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event)
    {
        if (event.getKeyCode() == KeyEvent.KEYCODE_HOME)
        {
            return true;
        }
        else
        {
            return super.dispatchKeyEvent(event);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME)
        {
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_SEARCH || keyCode == KeyEvent.KEYCODE_MENU
                && (event.getFlags() & KeyEvent.FLAG_LONG_PRESS) == KeyEvent.FLAG_LONG_PRESS)
        {
            return true;
        }
//        return super.onKeyDown(keyCode, event);
        return true;
    }

    @Override
    public void onUserLeaveHint()
    {
        super.onUserLeaveHint();
    }

    @Override
    public void onStop()
    {
        super.onStop();
    }

    @Override
    public void onAttachedToWindow()
    {
        getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);

        super.onAttachedToWindow();
    }

    @Override
    public void onDestroy()
    {
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock lock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE);
        lock.reenableKeyguard();

        super.onDestroy();
    }
}
