package com.arellomobile.terminal.service;

import android.app.Service;
import android.content.Intent;
import android.os.*;
import com.arellomobile.terminal.data.data.Passenger;
import com.arellomobile.terminal.data.data.TripInfo;
import com.arellomobile.terminal.data.data.commit.CommitHelperObject;
import com.arellomobile.terminal.data.manager.cache.StorageApi;
import com.arellomobile.terminal.data.manager.datamanager.DataManager;
import com.arellomobile.terminal.data.manager.langservice.LangFactory;
import com.arellomobile.terminal.data.manager.service.ServerApi;
import com.arellomobile.terminal.helper.application.TerminalApplication;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * User: AndreyKo
 * Date: 08.06.12
 * Time: 20:00
 */
public class CommitService extends Service
{
    public static final int UPDATE = 1;
    public static final int ADD_COMMIT = 2;
    public static final int SEND_COMMIT = 3;
    public static final int ADD_STATION_TO_COMMIT = 8;
    public static final int ADD_HANDSHAKE_COMMIT = 4;
    public static final int ADD_TRIP_INFO_LISTENER = 5;
    public static final int REMOVE_TRIP_INFO_LISTENER = 6;
    public static final int REMOVE_TRIP_INFO = 7;
    public static final int ADD_ROUTE_ID_TO_COMMIT = 9;
    public static final int SET_PASSENGER_STATUS = 10;

    private static final long CREATE_COMMIT_DELAY = 60 * 1000;
    private static final long COMMIT_DELAY = 60 * 1000;

    private Handler mHandler;

    private CommitFileHelper mFileHelper;
    private CommitGeneratorHelper mCommitGeneratorHelper;

    private TripInfo mTripInfo;
    private List<Messenger> mMessengerCallBacks = new ArrayList<Messenger>();
    private final Object mTripInfoMutex = new Object();
    private final Object mCommitSendSyncObj = new Object();

    private boolean mIsTripInfoNeeded = false;

    /**
     * Handler of incoming messages from clients.
     */
    class IncomingHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case UPDATE:
                    checkAndAddCommitMessage();
                    mHandler.sendMessageDelayed(Message.obtain(null, UPDATE), CREATE_COMMIT_DELAY);
                    break;
                case SET_PASSENGER_STATUS:
                    Passenger passenger = (Passenger) msg.getData().getSerializable(String.valueOf(msg.what));

                    if (null != passenger)
                    {
                        CommitHelperObject commitObject2 = mCommitGeneratorHelper.getCommit(passenger);
                        if (null != commitObject2)
                        {
                            mFileHelper.addCommit(commitObject2);
                            sendCommitsFromFile(true);
                        }
                    }
                    break;
                case ADD_ROUTE_ID_TO_COMMIT:
                    CommitHelperObject commitObject1 = mCommitGeneratorHelper.getCommit(msg.arg1);
                    if (null != commitObject1)
                    {
                        mFileHelper.addCommit(commitObject1);
                        sendCommitsFromFile(true);
                    }
                    break;
                case ADD_STATION_TO_COMMIT:
                    String station = msg.getData().getString(String.valueOf(msg.what));
                    CommitHelperObject commitObject = mCommitGeneratorHelper.getCommit(station);
                    if (null != commitObject)
                    {
                        mFileHelper.addCommit(commitObject);
                        sendCommitsFromFile(true);
                    }
                    break;
                case ADD_HANDSHAKE_COMMIT:
                    synchronized (mTripInfoMutex)
                    {
                        mIsTripInfoNeeded = true;
                    }
                case ADD_COMMIT:
                    String commit = msg.getData().getString(String.valueOf(msg.what));
                    mFileHelper.addCommit(CommitHelperObjectGenerator.generateFromJsonString(commit));
                    sendCommitsFromFile(true);
                    break;
                case SEND_COMMIT:
                    sendCommitsFromFile(false);
                    break;
                case ADD_TRIP_INFO_LISTENER:
                    synchronized (mTripInfoMutex)
                    {
                        mMessengerCallBacks.add(msg.replyTo);
                        if (mTripInfo != null)
                        {
                            try
                            {
                                sendTripInfoMessage(msg.replyTo, mTripInfo);
                            } catch (RemoteException e)
                            {
                                // pass
                            }
                        }
                    }
                    break;
                case REMOVE_TRIP_INFO_LISTENER:
                    synchronized (mTripInfoMutex)
                    {
                        mMessengerCallBacks.remove(msg.replyTo);
                    }
                    break;
                case REMOVE_TRIP_INFO:
                    synchronized (mTripInfoMutex)
                    {
                        mTripInfo = null;
                        mIsTripInfoNeeded = false;
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void sendTripInfoMessage(Messenger replyTo, TripInfo tripInfo) throws RemoteException
    {
        Message message = new Message();
        message.what = ADD_TRIP_INFO_LISTENER;

        Bundle bundle = new Bundle();
        bundle.putSerializable(String.valueOf(message.what), tripInfo);

        message.setData(bundle);

        replyTo.send(message);
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    @Override
    public IBinder onBind(Intent intent)
    {
        return mMessenger.getBinder();
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        handleCommand();
    }

    private void handleCommand()
    {
        if (null == mFileHelper)
        {
            mFileHelper = new CommitFileHelper(getApplicationContext());
        }
        if (null == mCommitGeneratorHelper)
        {
            mCommitGeneratorHelper = new CommitGeneratorHelper(getApplicationContext());
        }
        if (null == mHandler)
        {
            mHandler = new IncomingHandler();
            Message message = new Message();
            message.what = UPDATE;
            mHandler.sendMessage(message);
            sendSendCommitMessageWithDelay(COMMIT_DELAY);
        }
    }

    private synchronized void checkAndAddCommitMessage()
    {
        if (null != mCommitGeneratorHelper)
        {
            CommitHelperObject commitObject = mCommitGeneratorHelper.getCommit();
            if (null != commitObject)
            {
                mFileHelper.addCommit(commitObject);
            }
        }
    }

    private synchronized void sendSendCommitMessageWithDelay(long commitDelay)
    {
        if (null != mHandler)
        {
            Message message = new Message();
            message.what = SEND_COMMIT;
            mHandler.sendMessageDelayed(message, commitDelay);
        }
    }

    private void sendCommitsFromFile(final boolean isForce)
    {
        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                CommitHelperObject commitHelperObject;
                synchronized (mCommitSendSyncObj)
                {
                    try
                    {
                        while ((commitHelperObject = mFileHelper.getFirstCommit()) != null)
                        {
                            ServerApi serverApi = ((TerminalApplication) getApplication()).getServerApi();
                            String commit = CommitHelperObjectGenerator
                                    .generateJSONStringFromHelper(commitHelperObject);
                            TripInfo tripInfo = serverApi.sentCommit(commit);
                            if (null != tripInfo)
                            {
                                synchronized (mTripInfoMutex)
                                {
                                    if (mIsTripInfoNeeded)
                                    {
                                        mTripInfo = tripInfo;
                                        StorageApi storageApi = ((TerminalApplication) getApplication())
                                                .getStorageApi();
                                        DataManager dataManager = ((TerminalApplication) getApplication())
                                                .getDataManager();
                                        storageApi.saveTripInfo(mTripInfo,
                                                                dataManager.getTripId(getApplicationContext()));
                                        if (mTripInfo.getLangPack() != null)
                                        {
                                            storageApi.saveLangPack(mTripInfo.getLangPack());
                                            LangFactory mLangFactory = ((TerminalApplication) getApplication())
                                                    .getLangFactory();
                                            mLangFactory.setJson(mTripInfo.getLangPack().getJsonString());
                                        }
                                        Iterator<Messenger> messengerIterator = mMessengerCallBacks.iterator();
                                        while (messengerIterator.hasNext())
                                        {
                                            Messenger messenger = messengerIterator.next();
                                            try
                                            {
                                                sendTripInfoMessage(messenger, mTripInfo);
                                            } catch (RemoteException e)
                                            {
                                                messengerIterator.remove();
                                            }
                                        }
                                    }
                                }
                            }
                            mFileHelper.removeCommit(commitHelperObject);
                        }
                    } catch (Exception e)
                    {
                        Logger.getLogger(getClass().getName()).severe("can't close commit file");
                        e.printStackTrace();
                    }

                    if (!isForce)
                    {
                        sendSendCommitMessageWithDelay(COMMIT_DELAY);
                    }
                }
            }
        });
        thread.start();
    }

    @Override
    public synchronized void onDestroy()
    {
        if (null != mCommitGeneratorHelper)
        {
            mCommitGeneratorHelper.onDestroy();
            mCommitGeneratorHelper = null;
        }
        if (null != mHandler)
        {
            mHandler.removeMessages(ADD_COMMIT);
            mHandler.removeMessages(SEND_COMMIT);
            mHandler = null;
        }
    }
}

