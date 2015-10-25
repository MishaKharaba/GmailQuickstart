package com.example.misha.gmailquickstart;

import android.content.res.Resources;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.History;
import com.google.api.services.gmail.model.Label;
import com.google.api.services.gmail.model.ListHistoryResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.ListLabelsResponse;
import com.google.api.services.gmail.model.ListMessagesResponse;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class GmailReader
{
    private Gmail mService;
    private Exception mLastError;

    public GmailReader(GoogleAccountCredential credential)
    {
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        Resources res = credential.getContext().getResources();
        mService = new Gmail.Builder(
                transport, jsonFactory, credential)
                .setApplicationName(res.getString(R.string.app_name))
                .build();
    }

    public List<Label> GetLabelList()
    {
        try
        {
            String user = "me";
            ListLabelsResponse listResponse = mService.users().labels().list(user).execute();
            List<Label> labels = listResponse.getLabels();
            return labels;
        }
        catch (Exception e)
        {
            mLastError = e;
            return null;
        }
    }

    public List<Message> GetMessages(int maxCount)
    {
        try
        {
            long maxResult = (maxCount > 100) ? 100 : maxCount;
            ListMessagesResponse response = mService.users().messages().list("me")
                    .setMaxResults(maxResult)
                    .setFields("messages(historyId,id,threadId)").execute();
            List<Message> messages = new ArrayList<>();
            while (response.getMessages() != null)
            {
                messages.addAll(response.getMessages());
                if (messages.size() >= maxCount)
                    break;
                if (response.getNextPageToken() != null)
                {
                    String pageToken = response.getNextPageToken();
                    response = mService.users().messages().list("me")
                            .setMaxResults(maxResult).setPageToken(pageToken)
                            .setFields("messages(historyId,id,threadId)").execute();
                }
                else
                {
                    break;
                }
            }
            for (Message message : messages)
            {
                Log.i("gmail-service", message.toPrettyString());
            }
            return messages;
        }
        catch (Exception e)
        {
            mLastError = e;
            return null;
        }
    }

    public List<History> GetHistory(BigInteger startHistoryId, int maxCount)
    {
        try
        {
            long maxResult = (maxCount > 100) ? 100 : maxCount;
            List<History> histories = new ArrayList<>();
            ListHistoryResponse response = mService.users().history().list("me")
                    .setMaxResults(maxResult).setStartHistoryId(startHistoryId).execute();
            while (response.getHistory() != null)
            {
                histories.addAll(response.getHistory());
                if (histories.size() >= maxCount)
                    break;
                if (response.getNextPageToken() != null)
                {
                    String pageToken = response.getNextPageToken();
                    response = mService.users().history().list("me").setPageToken(pageToken)
                            .setMaxResults(maxResult).setStartHistoryId(startHistoryId).execute();
                }
                else
                {
                    break;
                }
            }

            for (History history : histories)
            {
                Log.i("gmail-service", history.toPrettyString());
            }
            return histories;
        }
        catch (Exception e)
        {
            mLastError = e;
            return null;
        }
    }

    public Exception getLastError()
    {
        return mLastError;
    }
}
