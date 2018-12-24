package com.arellomobile.terminal.helper.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.arellomobile.terminal.R;
import com.arellomobile.terminal.data.data.NearestStation;

import java.util.List;

/**
 * User: MiG35
 * Date: 20.06.12
 * Time: 20:53
 */
public class StationsAdapter extends BaseAdapter
{
    private Context mContext;
    private List<NearestStation> mStations;
    private Integer mSelectedItem;

    public StationsAdapter(Context context, List<NearestStation> stations)
    {
        mContext = context;
        mStations = stations;
    }

    @Override
    public int getCount()
    {
        return mStations.size();
    }

    @Override
    public NearestStation getItem(int i)
    {
        return mStations.get(i);
    }

    @Override
    public long getItemId(int i)
    {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup)
    {
        if (null == view)
        {
            view = View.inflate(mContext, R.layout.station_selector_item, null);
        }

        NearestStation station = getItem(i);

        ((TextView) view.findViewById(R.id.station)).setText(station.getName());

        if (null == mSelectedItem || mSelectedItem != i)
        {
            view.findViewById(R.id.check).setVisibility(View.GONE);
        }
        else
        {
            view.findViewById(R.id.check).setVisibility(View.VISIBLE);
        }
        return view;
    }

    public void setSelectedItem(Integer selectedItem)
    {
        this.mSelectedItem = selectedItem;
    }

    public NearestStation getSelectedItem()
    {
        if (null != mSelectedItem)
        {
            return mStations.get(mSelectedItem);
        }
        return null;
    }
}
