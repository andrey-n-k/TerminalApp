package com.arellomobile.android.libs.ui.weel.adapters;

import android.content.Context;
import com.arellomobile.android.libs.ui.weel.WheelAdapter;

/**
 * Adapter class for old wheel adapter (deprecated WheelAdapter class).
 * 
 * @deprecated Will be removed soon
 */
public class AdapterWheel<T> extends AbstractWheelTextAdapter<T> {

    // Source adapter
    private WheelAdapter<T> adapter;
    
    /**
     * Constructor
     * @param context the current context
     * @param adapter the source adapter
     */
    public AdapterWheel(Context context, WheelAdapter<T> adapter) {
        super(context);
        
        this.adapter = adapter;
    }

    /**
     * Gets original adapter
     * @return the original adapter
     */
    public WheelAdapter getAdapter() {
        return adapter;
    }
    
    @Override
    public int getItemsCount() {
        return adapter.getItemsCount();
    }

    @Override
    public T getItem(int position)
    {
        return adapter.getItem(position);
    }

    @Override
    protected CharSequence getItemText(int index) {
        return adapter.getItem(index).toString();
    }

}
