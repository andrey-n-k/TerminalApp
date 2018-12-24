package com.arellomobile.terminal.helper.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.arellomobile.terminal.R;
import com.arellomobile.terminal.data.data.Route;
import com.arellomobile.terminal.data.data.Station;
import com.arellomobile.terminal.data.data.Trip;
import com.arellomobile.terminal.data.data.Vehicle;

import java.util.List;

/**
 * User: AndreyKo
 * Date: 22.05.12
 */
public class ListScreenAdapter<T> extends BaseAdapter
{
    private List<T> list;
    private Context context;
    private int selectedPos = -1;

    public ListScreenAdapter(Context context, List<T> list)
    {
        this.list = list;
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
            view = View.inflate(context, R.layout.list_item, null);
        }

        T item = list.get(i);

        if (selectedPos == i)
        {
            view.findViewById(R.id.check).setVisibility(View.VISIBLE);
        }
        else
        {
            view.findViewById(R.id.check).setVisibility(View.INVISIBLE);
        }
        ((TextView) view.findViewById(R.id.text_item)).setText(item.toString());

        if (item instanceof Station)
        {
            view.findViewById(R.id.price).setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.price)).setText(((Station) list.get(i)).getPrice());
        }
        else
        {
            view.findViewById(R.id.price).setVisibility(View.GONE);
        }


        view.findViewById(R.id.line).setVisibility(View.GONE);
        if (item instanceof Vehicle)
        {
            if (i == 3)
            {
                // 3 position
                view.findViewById(R.id.line).setVisibility(View.VISIBLE);
            }
        }

        return view;
    }

    public void setSelectedPosition(int pos)
    {
        selectedPos = pos;
        // inform the view of this change
        notifyDataSetChanged();
    }
}
