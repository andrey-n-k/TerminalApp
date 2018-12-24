package com.arellomobile.terminal.service;

import android.content.Context;
import com.arellomobile.terminal.data.data.commit.CommitHelperObject;
import com.arellomobile.terminal.data.manager.cache.StorageApi;
import com.arellomobile.terminal.data.manager.datamanager.DataManager;
import com.arellomobile.terminal.helper.application.TerminalApplication;

import java.util.Collections;
import java.util.List;

/**
 * User: AndreyKo
 * Date: 12.06.12
 * Time: 12:52
 */
public class CommitFileHelper
{
    private final Object mFileSyncObject = new Object();

    private DataManager mDataManager;
    private StorageApi mStorageApi;


    public CommitFileHelper(Context context)
    {
        mDataManager = ((TerminalApplication) context.getApplicationContext()).getDataManager();
        mStorageApi = ((TerminalApplication) context.getApplicationContext()).getStorageApi();

        synchronized (mFileSyncObject)
        {
            int commitCount = getCommitCount();
            mDataManager.setCommitCount(commitCount);
        }
    }

    private int getCommitCount()
    {
        synchronized (mFileSyncObject)
        {
            List<CommitHelperObject> commitHelperObjects = mStorageApi.getCommitHelperObjects();
            int count = 0;

            for (CommitHelperObject commitHelperObject : commitHelperObjects)
            {
                count += commitHelperObject.getTrackJsonArray().length();
            }

            return count;
        }
    }

    CommitHelperObject getFirstCommit()
    {
        synchronized (mFileSyncObject)
        {
            List<CommitHelperObject> commitHelperObjects = mStorageApi.getCommitHelperObjects();

            Collections.sort(commitHelperObjects);

            if (commitHelperObjects.size() > 0)
            {
                return commitHelperObjects.get(0);
            }
        }
        return null;
    }

    void removeCommit(CommitHelperObject commit)
    {
        synchronized (mFileSyncObject)
        {
            mStorageApi.removeCommitHelpObject(commit);

            mDataManager.setCommitCount(getCommitCount());
        }
    }

    void addCommit(CommitHelperObject commit)
    {
        if(null == commit)
        {
            return;
        }
        synchronized (mFileSyncObject)
        {
            mStorageApi.addCommitHelpObject(commit);
            mDataManager.setCommitCount(getCommitCount());
        }
    }

    public void onDestroy()
    {
    }
}
