package com.arellomobile.terminal.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.arellomobile.terminal.R;
import com.arellomobile.terminal.data.data.Company;
import com.arellomobile.terminal.data.data.LangPack;
import com.arellomobile.terminal.data.data.Language;
import com.arellomobile.terminal.helper.uihelper.AlertAdapter;
import com.arellomobile.terminal.helper.uihelper.BasicActivity;
import com.arellomobile.terminal.service.CommitService;
import com.arellomobile.terminal.service.LocationService;
import com.arellomobile.terminal.tasks.LoadFirstInfo;

import java.util.List;

public class ZeroScreenActivity extends BasicActivity implements View.OnClickListener,
                                                                 LoadFirstInfo.LoadFirstInfoCallback
{
    private List<Company> companies;
    private List<Language> languages;

    private Handler timeHandler;

    private String languageAppId;
    private Long companyId;

    private ImageView langView;
    private TextView langText;

    private String mLangAlert;
    private String mCompanyAlert;
    private String mPasswordAlert;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zero_screen);

        findViewById(R.id.doneBtn).setOnClickListener(this);
        findViewById(R.id.exitBtn).setOnClickListener(this);
        findViewById(R.id.languageBtn).setOnClickListener(this);
        findViewById(R.id.companyBtn).setOnClickListener(this);

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        timeHandler = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                switch (msg.what)
                {
                    case UPDATE:
                        setCurrentDataTime((TextView) findViewById(R.id.date), (TextView) findViewById(R.id.time));
                        timeHandler.removeMessages(UPDATE);
                        Message message = new Message();
                        message.what = UPDATE;
                        timeHandler.sendMessageDelayed(message, TIME);
                        break;
                }
                super.handleMessage(msg);
            }
        };
        languageAppId = mDataManager.getLanguage(this);
        langView = (ImageView) findViewById(R.id.lang_image);
        langText = (TextView) findViewById(R.id.lang_text);

        if (mDataManager.getItemFlag(languageAppId) != null)
        {
            langView.setImageResource(mDataManager.getItemFlag(languageAppId));
            findViewById(R.id.lang_image).setVisibility(View.VISIBLE);
            langText.setVisibility(View.GONE);
        }
        else
        {
            findViewById(R.id.lang_image).setVisibility(View.GONE);
            langText.setVisibility(View.VISIBLE);
            langText.setText(languageAppId);
        }

        new LoadFirstInfo(this, this).execute();

        initLabel();
    }

    private void initLabel()
    {
        ((TextView) findViewById(R.id.selectAppLanguageLabel))
                .setText(mLangFactory.getString("selectAppLanguageLabel"));
        ((TextView) findViewById(R.id.companyLabel)).setText(mLangFactory.getString("companyLabel"));
        ((TextView) findViewById(R.id.companyHiddenLabel)).setText(mLangFactory.getString("companyHiddenLabel"));
        ((TextView) findViewById(R.id.adminPasswordLabel)).setText(mLangFactory.getString("adminPasswordLabel"));
        ((TextView) findViewById(R.id.exitBtn)).setText(mLangFactory.getString("exitLabel"));
        ((TextView) findViewById(R.id.doneBtn)).setText(mLangFactory.getString("doneLabel"));

        mLangAlert = mLangFactory.getString("notSelectLang");
        mCompanyAlert = mLangFactory.getString("notSelectCompany");
        mPasswordAlert = mLangFactory.getString("notEnterPassword");
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.doneBtn:
                if (languageAppId == null)
                {
                    AlertAdapter.showAlert(this, mLangAlert);
                    break;
                }
                if (companyId == null)
                {
                    AlertAdapter.showAlert(this, mCompanyAlert);
                    break;
                }

                if (((EditText) findViewById(R.id.password)).getText().toString() == null ||
                        ((EditText) findViewById(R.id.password)).getText().toString().trim().length() == 0)
                {
                    AlertAdapter.showAlert(this, mPasswordAlert);
                    break;
                }
                String password = ((EditText) findViewById(R.id.password)).getText().toString();
                mDataManager.savePreferences(languageAppId, companyId, password, this);
                mDataManager.logInApp(this); // if good response
                startService(new Intent(this, CommitService.class));
                startActivity(new Intent(this, SetTripActivity.class).putExtra(COMPANY_ID_KEY, companyId));
                finish();   // for release
                break;
            case R.id.languageBtn:
                startActivityForResult(new Intent(this, ListScreenActivity.class).
                        putExtra(ListScreenActivity.DATA_TYPE, ListScreenActivity.typeEnum.LANG_TYPE),
                                       RESULT_CODE_OK);
                break;
            case R.id.companyBtn:
                startActivityForResult(new Intent(this, ListScreenActivity.class).
                        putExtra(ListScreenActivity.DATA_TYPE, ListScreenActivity.typeEnum.COMPANY_TYPE),
                                       RESULT_CODE_OK);
                break;
            case R.id.exitBtn:
                stopService(new Intent(this, LocationService.class));
                finish();
                break;
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        timeHandler.removeMessages(UPDATE);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        timeHandler.removeMessages(UPDATE);
        Message message = new Message();
        message.what = UPDATE;
        timeHandler.sendMessageDelayed(message, 0);

        if (mDataManager.getCompanyId(this) > -1)
        {
            Company company = mStorageApi.getCompanyById(mDataManager.getCompanyId(this));
            findViewById(R.id.companyHiddenLabel).setVisibility(View.GONE);
            findViewById(R.id.mainText).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.mainText)).setText(company.getName());
            companyId = company.getId();
        }
        if (mDataManager.getLanguage(this) != null)
        {
            languageAppId = mDataManager.getLanguage(this);
            if (mDataManager.getItemFlag(languageAppId) != null)
            {
                langView.setImageResource(mDataManager.getItemFlag(languageAppId));
                findViewById(R.id.lang_image).setVisibility(View.VISIBLE);
                langText.setVisibility(View.GONE);
            }
            else
            {
                findViewById(R.id.lang_image).setVisibility(View.GONE);
                langText.setVisibility(View.VISIBLE);
                langText.setText(languageAppId);
            }
        }
    }

    @Override
    public void setData(List<Company> companies, List<Language> languages, LangPack langPack)
    {
        this.companies = companies;
        this.languages = languages;
        mLangPack = langPack;

        mLangFactory.setJson(mLangPack.getJsonString());
        initLabel();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == RESULT_CODE_OK)
        {
            Object obj = data.getSerializableExtra(ListScreenActivity.DATA_TYPE);
            if (obj instanceof Language)
            {
                languageAppId = ((Language) obj).getLangID();
                if (mDataManager.getItemFlag(languageAppId) != null)
                {
                    langView.setImageResource(mDataManager.getItemFlag(languageAppId));
                    findViewById(R.id.lang_image).setVisibility(View.VISIBLE);
                    langText.setVisibility(View.GONE);
                }
                else
                {
                    findViewById(R.id.lang_image).setVisibility(View.GONE);
                    langText.setVisibility(View.VISIBLE);
                    langText.setText(languageAppId);
                }
                mDataManager.setLanguageAppId(this, languageAppId);

                mLangFactory.setLangid(languageAppId);
                initLabel();
            }
            else if (obj instanceof Company)
            {
                findViewById(R.id.companyHiddenLabel).setVisibility(View.GONE);
                findViewById(R.id.mainText).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.mainText)).setText(((Company) obj).getName());
                companyId = ((Company) obj).getId();
                mDataManager.setCompanyId(this, companyId);
            }
        }
    }
}
