package com.example.misha.gmailquickstart;

import android.Manifest;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.History;
import com.google.api.services.gmail.model.Label;
import com.google.api.services.gmail.model.ListLabelsResponse;
import com.google.api.services.gmail.model.Message;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class StartActivity extends AppCompatActivity
{
    AlarmReceiver alarm = new AlarmReceiver();

    GoogleAccountCredential mCredential;

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 10;
    private static final int REQUEST_ACCOUNT_PICKER = 1000;
    private static final int REQUEST_AUTHORIZATION = 1001;
    private static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {
            GmailScopes.MAIL_GOOGLE_COM,
            GmailScopes.GMAIL_LABELS,
            GmailScopes.GMAIL_READONLY};
    private TextView mMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        mMsg = (TextView) findViewById(R.id.msg);
        // Initialize credentials and service object.
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));

    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_start, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onSelectAccountClick(View view)
    {
        chooseAccount();
        CheckPermission(Manifest.permission.GET_ACCOUNTS);
    }

    private void CheckPermission(String permission)
    {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
        {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission))
            {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            }
            else
            {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, new String[]{permission},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    /**
     * Starts an activity in Google Play Services so the user can pick an
     * account.
     */
    private void chooseAccount()
    {
        startActivityForResult(mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     *
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode  code indicating the result of the incoming
     *                    activity result.
     * @param data        Intent (containing result data) returned by incoming
     *                    activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK)
                {
                    mMsg.setText("isGooglePlayServicesAvailable()");
                    //isGooglePlayServicesAvailable();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null)
                {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null)
                    {
                        mCredential.setSelectedAccountName(accountName);
                        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mMsg.setText(accountName);
                    }
                }
                else if (resultCode == RESULT_CANCELED)
                {
                    mMsg.setText(R.string.account_unspecified);
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode != RESULT_OK)
                {
                    chooseAccount();
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Checks whether the device currently has a network connection.
     *
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline()
    {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    public void onLoadGMailLabelsClick(View view)
    {
        if (isDeviceOnline())
        {
            MakeRequestTask task = new MakeRequestTask(mCredential);
            task.execute();
        }
        else
        {
            mMsg.setText("No network connection available.");
        }
    }

    public void onLoadMailListClick(View view)
    {
        if (isDeviceOnline())
        {
            LoadMailListTask task = new LoadMailListTask(mCredential);
            task.execute();
        }
        else
        {
            mMsg.setText("No network connection available.");
        }
    }

    public void onStartClick(View view)
    {
        alarm.setAlarm(this);
    }

    public void onStopClick(View view)
    {
        alarm.cancelAlarm(this);
    }

    public void onAlarmClick(View view)
    {
        Intent intent = new Intent(this, AlarmClockScreenActivity.class);
        startActivity(intent);
    }

    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>>
    {
        private com.google.api.services.gmail.Gmail mService = null;
        private Exception mLastError = null;

        public MakeRequestTask(GoogleAccountCredential credential)
        {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.gmail.Gmail.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("GMail API Android Quickstart")
                    .build();
        }

        /**
         * Background task to call Gmail API.
         *
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params)
        {
            try
            {
                return getDataFromApi();
            }
            catch (Exception e)
            {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch a list of Gmail labels attached to the specified account.
         *
         * @return List of Strings labels.
         * @throws IOException
         */
        private List<String> getDataFromApi() throws IOException
        {
            // Get the labels in the user's account.
            String user = "me";
            List<String> labels = new ArrayList<String>();
            ListLabelsResponse listResponse =
                    mService.users().labels().list(user).execute();
            for (Label label : listResponse.getLabels())
            {
                labels.add(label.getId() + ": " + label.getName());
            }
            return labels;
        }


        @Override
        protected void onPreExecute()
        {
            mMsg.setText("Getting label list...");
            //mProgress.show();
        }

        @Override
        protected void onPostExecute(List<String> output)
        {
            //mProgress.hide();
            if (output == null || output.size() == 0)
            {
                mMsg.setText("No results returned.");
            }
            else
            {
                output.add(0, "Data retrieved using the Gmail API:");
                mMsg.setText(TextUtils.join("\n", output));
            }
        }

        @Override
        protected void onCancelled()
        {
            //mProgress.hide();
            if (mLastError != null)
            {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException)
                {
//                    showGooglePlayServicesAvailabilityErrorDialog(
//                            ((GooglePlayServicesAvailabilityIOException) mLastError)
//                                    .getConnectionStatusCode());
                    mMsg.setText("The following error occurred:\n"
                            + mLastError.getMessage());
                }
                else if (mLastError instanceof UserRecoverableAuthIOException)
                {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            MainActivity.REQUEST_AUTHORIZATION);
                }
                else
                {
                    mMsg.setText("The following error occurred:\n"
                            + mLastError.getMessage());
                }
            }
            else
            {
                mMsg.setText("Request cancelled.");
            }
        }
    }

    private class LoadMailListTask extends AsyncTask<Void, Void, List<Message>>
    {
        private GmailReader mService = null;

        public LoadMailListTask(GoogleAccountCredential credential)
        {
            mService = new GmailReader(credential);
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            mMsg.setText("Getting mail list...");
        }

        @Override
        protected void onPostExecute(List<Message> output)
        {
            super.onPostExecute(output);
            //mProgress.hide();
            if (output == null || output.size() == 0)
            {
                mMsg.setText("No results returned.");
            }
            else
            {
                StringBuilder sb = new StringBuilder();
                for (Message msg : output)
                {
                    try
                    {
                        sb.append(msg.toPrettyString());
                    }
                    catch (Exception e)
                    {
                        sb.append(msg.getId());
                    }
                    sb.append('\n');
                }
                mMsg.setText(sb.toString());
            }
        }

        @Override
        protected void onCancelled()
        {
            super.onCancelled();
            if (mService.getLastError() != null)
            {
                mMsg.setText("The following error occurred:\n" + mService.getLastError().getMessage());
            }
            else
            {
                mMsg.setText("Request cancelled.");
            }
        }

        @Override
        protected List<Message> doInBackground(Void... params)
        {
            List<Message> messages = mService.GetMessages(20);
            if (mService.getLastError() != null)
            {
                messages = null;
                cancel(true);
            }
            return messages;
        }

    }

    private class LoadHistoryListTask extends AsyncTask<Void, Void, List<History>>
    {
        private GmailReader mService = null;

        public LoadHistoryListTask(GoogleAccountCredential credential)
        {
            mService = new GmailReader(credential);
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            mMsg.setText("Getting mail list...");
        }

        @Override
        protected void onPostExecute(List<History> output)
        {
            super.onPostExecute(output);
            //mProgress.hide();
            if (output == null || output.size() == 0)
            {
                mMsg.setText("No results returned.");
            }
            else
            {
                StringBuilder sb = new StringBuilder();
                for (History msg : output)
                {
                    try
                    {
                        sb.append(msg.toPrettyString());
                    }
                    catch (Exception e)
                    {
                        sb.append(msg.getId());
                    }
                    sb.append('\n');
                }
                mMsg.setText(sb.toString());
            }
        }

        @Override
        protected void onCancelled()
        {
            super.onCancelled();
            if (mService.getLastError() != null)
            {
                mMsg.setText("The following error occurred:\n" + mService.getLastError().getMessage());
            }
            else
            {
                mMsg.setText("Request cancelled.");
            }
        }

        @Override
        protected List<History> doInBackground(Void... params)
        {
            List<History> messages = mService.GetHistory(BigInteger.ZERO, 100);
            if (mService.getLastError() != null)
            {
                messages = null;
                cancel(true);
            }
            return messages;
        }

    }
}
