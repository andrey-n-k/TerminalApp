package com.arellomobile.terminal.helper.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.arellomobile.terminal.R;
import com.arellomobile.terminal.data.data.Passenger;

import java.util.ArrayList;
import java.util.List;

/**
 * User: AndreyKo
 * Date: 22.05.12
 */
public class PassengerListAdapter<T extends Passenger> extends BaseAdapter
{
    private List<T> list;
    private Context context;

    public PassengerListAdapter(Context context, List<T> onlineList, List<T> offlineList)
    {
        this.list = new ArrayList<T>();
        this.list.addAll(offlineList);
        this.list.addAll(onlineList);
        this.context = context;
    }
    
    @Override
    public int getCount()
    {
        return list.size();
    }

    @Override
    public T getItem(int i)
    {
        return list.get(i);
    }

    @Override
    public long getItemId(int i)
    {
        return list.get(i).hashCode();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup)
    {
        if (view == null) 
        {
            view = View.inflate(context, R.layout.passenger_list_item, null);
        }
        Passenger passenger = list.get(i);

        ((TextView) view.findViewById(R.id.name)).setText(passenger.getName());
        ((TextView) view.findViewById(R.id.trip)).setText(String.format(context.getString(R.string.tripPassenger), 
                String.valueOf(passenger.getFromStationId()), String.valueOf(passenger.getToStationId())));
        ((TextView) view.findViewById(R.id.status)).setText(passenger.getStatus());
        
        return view;
    }
}
