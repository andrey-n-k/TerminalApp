package com.arellomobile.terminal.helper.uihelper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.arellomobile.terminal.R;

/**
 * @author AndreyKo
 */
public class AlertAdapter extends BasicActivity
{
    protected static final String HEADER_ID_KEY = "headerId";
    protected static final String MESSAGE_ID_KEY = "messageId";
    protected static final String RIGHT_ID_KEY = "rightId";
    protected static final String HAS_INPUT = "hasInput";
    protected static final String MESSAGE_TEXT_KEY = "messageText";

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.alert_layout);

        if (getIntent().hasExtra(HEADER_ID_KEY))
        {
            TextView title = (TextView) findViewById(R.id.title);
            title.setText(getIntent().getIntExtra(HEADER_ID_KEY, 0));
            title.setVisibility(View.VISIBLE);
        }

        TextView message = (TextView) findViewById(R.id.message);
        int id = getIntent().getIntExtra(MESSAGE_ID_KEY, 0);
        if (id != 0)
        {
            message.setText(id);
        }
        String text = getIntent().getStringExtra(MESSAGE_TEXT_KEY);
        if (text != null)
        {
            message.setText(text);
        }

        ((TextView) findViewById(R.id.leftBtn)).setText(mLangFactory.getString("okLabel"));
        findViewById(R.id.leftBtn).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                setResult(RESULT_OK);
                finish();
            }
        });

        if (getIntent().hasExtra(RIGHT_ID_KEY))
        {
            TextView rightBtn = (TextView) findViewById(R.id.rightBtn);
            rightBtn.setText(getIntent().getIntExtra(RIGHT_ID_KEY, 0));
            rightBtn.setText(mLangFactory.getString("cancelLabel"));
            rightBtn.setVisibility(View.VISIBLE);
            rightBtn.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    finish();
                }
            });
        }

        final EditText input = (EditText) findViewById(R.id.input);
        input.setVisibility(getIntent().getBooleanExtra(HAS_INPUT, false) ? View.VISIBLE : View.GONE);
        if (getIntent().getBooleanExtra(HAS_INPUT, false))
        {
            findViewById(R.id.leftBtn).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    String inputString = input.getText().toString().trim();
                    if (inputString.length() > 0)
                    {
                        Intent data = new Intent();
                        data.putExtra(String.valueOf(RESULT_OK), inputString);
                        setResult(RESULT_OK, data);
                    }
                    else
                    {
                        setResult(RESULT_CODE_CANCEL);
                    }
                    finish();
                }
            });
        }
    }

    public static void showAlert(final Activity context, final int headerId, final int messageId)
    {
        context.startActivity(new Intent(context, AlertAdapter.class).putExtra(HEADER_ID_KEY, headerId)
                                      .putExtra(MESSAGE_ID_KEY, messageId));
    }

    public static void showAlert(final Activity context, final int messageId)
    {
        context.startActivity(new Intent(context, AlertAdapter.class).putExtra(MESSAGE_ID_KEY, messageId));
    }

    public static void showAlert(final Activity context, final String message)
    {
        context.startActivity(new Intent(context, AlertAdapter.class).putExtra(MESSAGE_TEXT_KEY, message));
    }

    public static void showAlert(final Activity context, int messageId, int cancelLabel, int code)
    {
        context.startActivityForResult(new Intent(context, AlertAdapter.class).putExtra(MESSAGE_ID_KEY, messageId)
                .putExtra(RIGHT_ID_KEY, cancelLabel), code);
    }

    public static void showAlert(final Activity context, String message, int cancelLabel, int code)
    {
        context.startActivityForResult(new Intent(context, AlertAdapter.class).putExtra(MESSAGE_TEXT_KEY, message)
                .putExtra(RIGHT_ID_KEY, cancelLabel), code);
    }
}



