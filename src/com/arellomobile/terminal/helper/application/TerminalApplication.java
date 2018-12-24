package com.arellomobile.terminal.helper.application;

import com.arellomobile.terminal.data.manager.Managers;
import com.arellomobile.terminal.data.manager.cache.*;
import com.arellomobile.terminal.data.manager.datamanager.DataManager;
import com.arellomobile.terminal.data.manager.langservice.LangFactory;
import com.arellomobile.terminal.data.manager.service.ServerApi;

/**
 * Date: 07.03.12
 * Time: 11:51
 *
 * @author AndreyKo
 */
public interface TerminalApplication
{
    ServerApi getServerApi();

    StorageApi getStorageApi();

    DataManager getDataManager();

    LangFactory getLangFactory();
}
