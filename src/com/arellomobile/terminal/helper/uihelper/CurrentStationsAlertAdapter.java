package com.arellomobile.terminal.helper.uihelper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import com.arellomobile.terminal.R;
import com.arellomobile.terminal.data.data.NearestStation;
import com.arellomobile.terminal.helper.adapter.StationsAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * User: AndreyKo
 * Date: 20.06.12
 * Time: 20:45
 */
public class CurrentStationsAlertAdapter extends BasicActivity
{
    protected static final String header_key = "header";
    protected static final String right_id_key = "rightId";
    protected static final String has_input = "has_input";
    private static final String stations_key = "stations";

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.station_alert_layout);

        if (getIntent().hasExtra(header_key))
        {
            TextView title = (TextView) findViewById(R.id.title);
            title.setText(getIntent().getStringExtra(header_key));
            title.setVisibility(View.VISIBLE);
        }

        if (getIntent().hasExtra(right_id_key))
        {
            TextView rightBtn = (TextView) findViewById(R.id.rightBtn);
            rightBtn.setText(getIntent().getIntExtra(right_id_key, 0));
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
        ListView listView = (ListView) findViewById(R.id.stations_list);
        final StationsAdapter adapter = new StationsAdapter(this,
                                                            (List<NearestStation>) getIntent()
                                                                    .getSerializableExtra(stations_key));
        listView.setAdapter(adapter);

        input.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {
            }

            @Override
            public void afterTextChanged(Editable editable)
            {
                if (editable.toString().length() > 0)
                {
                    adapter.setSelectedItem(null);
                    adapter.notifyDataSetChanged();
                }
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                input.setText("");
                adapter.setSelectedItem(i);
                adapter.notifyDataSetChanged();
            }
        });

        ((TextView) findViewById(R.id.leftBtn)).setText(mLangFactory.getString("okLabel"));
        findViewById(R.id.leftBtn).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                NearestStation selectedItem = adapter.getSelectedItem();
                String inputString;
                if (null == selectedItem)
                {
                    inputString = input.getText().toString().trim();
                }
                else
                {
                    inputString = String.valueOf(selectedItem.getStationId());
                }


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

    public static void showInputAlertDialog(Activity context, String header, int cancelLabel, int code,
                                            ArrayList<NearestStation> stations)
    {
        context.startActivityForResult(
                new Intent(context, CurrentStationsAlertAdapter.class).putExtra(header_key, header)
                        .putExtra(right_id_key, cancelLabel).putExtra(has_input, true).putExtra(stations_key, stations),
                code);
    }

}
