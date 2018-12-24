package com.arellomobile.terminal.ui;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

/**
 * User: MiG35
 * Date: 08.06.12
 * Time: 12:16
 */
public class LockscreenActivity extends Activity
{

    @Override
    public void onCreate(Bundle savedState)
    {
        super.onCreate(savedState);

        TextView textView = new TextView(this);

        textView.setText("asdadasda");
        textView.setTextColor(Color.RED);

        setContentView(textView);
    }
}
