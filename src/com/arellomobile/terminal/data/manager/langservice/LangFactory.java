package com.arellomobile.terminal.data.manager.langservice;

import android.content.Context;
import org.json.JSONException;

/**
 * User: AndreyKo
 * Date: 20.06.12
 */
public interface LangFactory
{
    void setLangid(String langId);
    void setJson(String json);
    String getString(String label);
}
