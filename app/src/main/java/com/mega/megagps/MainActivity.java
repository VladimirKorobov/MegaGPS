package com.mega.megagps;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.mega.graphics.Views.IViewSurface;
import com.mega.megagps.Views.ViewGL;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MainActivity extends Activity { //AppCompatActivity {
    /**
     * Called when the activity is first created.
     */
    private enum ViewMode {
        Compass,
        Table
    };

    public MegaModel megaModel;
    private IViewSurface view;
    private Compass compass;
    private SmsTracker smsTracker;
    private GpsTracker gpsTracker;

    static final int PICK_CONTACT_REQUEST = 1;  // The request code
    static final int PICK_FROMFILE_REQUEST = 2;  // The request code

    private final Lock lock = new ReentrantLock();
    ViewMode vewMode = ViewMode.Compass;

    // lists for permissions
    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();
    // integer for permissions results request
    private static final int ALL_PERMISSIONS_RESULT = 1011;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gpsTracker = new GpsTracker(this);
        megaModel = new MegaModel(this, gpsTracker);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        // we add permissions we need to request location of the users
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_NETWORK_STATE);
        permissions.add(Manifest.permission.INTERNET);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissions.add(Manifest.permission.READ_SMS);
        permissions.add(Manifest.permission.SEND_SMS);
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        permissionsToRequest = new ArrayList<String>(permissions);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(permissionsToRequest.size() > 0) {
                requestPermissions(
                        permissionsToRequest.toArray(new String[permissionsToRequest.size()]),
                        ALL_PERMISSIONS_RESULT);
            }
        }

        compass = new Compass(this);
        // Test
        smsTracker = new SmsTracker(this);

        view = new ViewGL(this, megaModel);
        //view = new View2D(this, megaModel);
        LinearLayout layout = (LinearLayout)findViewById(R.id.mainLayout);
        layout.addView(view.getView());

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        /*
        handler = new Handler() {
            @Override
            public void
            handleMessage(Message msg)
            {
                {
                    lock.lock();
                    try
                    {
                        if(msg.what == 1)
                        {
                            view.Invalidate();
                        }
                    }
                    finally
                    {
                        lock.unlock();
                    }
                }
            }
        };
        */
        //mainThread = new WatchThread(handler);
        //mainThread.start();
    }

    private boolean hasPermission(String permission) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }
    public MegaModel getMegaModel() {
        return megaModel;
    }

    public void setDestination(double lat, double lon) {
        megaModel.setDestination(lat, lon);
        gpsTracker.setDestination(lat, lon);
    }

    public void Invalidate() {
        lock.lock();
        try {
            view.Invalidate();
        }
        finally {
            lock.unlock();
        }
    }

    public void setWatchView() {
        lock.lock();
        try {
            LinearLayout layout = (LinearLayout) findViewById(R.id.mainLayout);
            layout.removeViewAt(0);
            layout.addView(view.getView());
            vewMode = ViewMode.Compass;
        }
        finally {
            lock.unlock();
        }
        Invalidate();
    }
    private void EndMainThread() {
        //mainThread.EndThread();
        try {
            Thread.sleep(120);
        }
        catch (InterruptedException e) {
        }
    }
    public void CloseApp() {
        EndMainThread();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        compass.Resume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        compass.Pause();
    }
    //@TargetApi(Build.VERSION_CODES.CUPCAKE)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        for(int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            SpannableString spanString = new SpannableString(menu.getItem(i).getTitle().toString());
            int end = spanString.length();
            spanString.setSpan(new RelativeSizeSpan(1.2f), 0, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            item.setTitle(spanString);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        MenuItem item = menu.findItem(R.id.action_read_coord_from_file);
        if (gpsTracker.canGetLocation()) {
            item.setEnabled(true);
        }
        else {
            item.setEnabled(false);
        }
        item = menu.findItem(R.id.action_read_coord_from_sms);
        if (gpsTracker.canGetLocation()) {
            item.setEnabled(true);
        }
        else {
            item.setEnabled(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_exit:
                CloseApp();
                return true;
            case R.id.action_save_coord_to_file:
                if(gpsTracker.canGetLocation()) {
                    final Context This = this;
                    final EditSmsDialog dlg = new EditSmsDialog(
                            this,
                            new ContextThemeWrapper(this, android.R.style.Theme_Dialog));
                    final SmsTracker.Sms sms = smsTracker.createSms("", "", "Place", new Date().getTime(),
                            gpsTracker  );

                    dlg.setPositiveButton("OK", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            List<EditText> edits = dlg.getTextEdits();
                            SmsTracker.ParsedBody parsedBody = sms.parse();
                            parsedBody.place = edits.get(0).getText().toString();
                            parsedBody.lat = Double.parseDouble(edits.get(1).getText().toString());
                            parsedBody.lon = Double.parseDouble(edits.get(2).getText().toString());
                            sms.updateFrom(parsedBody);
                            SmsTracker.writeToFile(This, sms);
                            dialog.dismiss();
                        }
                    });

                    dlg.show(sms, "Сохранить:");
                }

                return true;

            case R.id.action_send_coord_by_sms:
                if(gpsTracker.canGetLocation()) {
                    Intent intent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts"));
                    intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE); // Show user only contacts w/ phone numbers
                    startActivityForResult(intent, PICK_CONTACT_REQUEST);
                }
                return true;
            case R.id.action_read_coord_from_file: {
                Intent intent = new Intent(this, TableActivity.class);
                intent.putExtra("width", (int) view.getView().getWidth());
                intent.putExtra("height", (int) view.getView().getHeight());
                intent.putExtra("action", "FromFile");
                startActivityForResult(intent, PICK_FROMFILE_REQUEST );
                return true;
            }
            case R.id.action_read_coord_from_sms: {
                Intent intent = new Intent(this, TableActivity.class);
                intent.putExtra("width", (int) view.getView().getWidth());
                intent.putExtra("height", (int) view.getView().getHeight());
                intent.putExtra("action", "FromSms");
                startActivityForResult(intent, PICK_FROMFILE_REQUEST);
            }
                return true;
            case R.id.action_stop_tracking:
                megaModel.eraseDestination();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == PICK_CONTACT_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // Get the URI that points to the selected contact
                Uri contactUri = data.getData();
                // We only need the NUMBER column, because there will be only one row in the result
                String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};

                // Perform the query on the contact to get the NUMBER column
                // We don't need a selection or sort order (there's only one result for the given URI)
                // CAUTION: The query() method should be called from a separate thread to avoid blocking
                // your app's UI thread. (For simplicity of the sample, this code doesn't do that.)
                // Consider using CursorLoader to perform the query.
                Cursor cursor = getContentResolver()
                        .query(contactUri, projection, null, null, null);
                cursor.moveToFirst();

                // Retrieve the phone number from the NUMBER column
                int column = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                final String number = cursor.getString(column);
                column = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                final String whom = cursor.getString(column);

                if(gpsTracker.canGetLocation()) {
                    final Context This = this;
                    final EditSmsDialog dlg = new EditSmsDialog(
                            this,
                            new ContextThemeWrapper(this, android.R.style.Theme_Dialog));
                    final SmsTracker.Sms sms = smsTracker.createSms("", "", "Place", new Date().getTime(),
                            gpsTracker  );

                    dlg.setPositiveButton("OK", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            List<EditText> edits = dlg.getTextEdits();
                            SmsTracker.ParsedBody parsedBody = sms.parse();
                            parsedBody.place = edits.get(0).getText().toString();
                            parsedBody.lat = Double.parseDouble(edits.get(1).getText().toString());
                            parsedBody.lon = Double.parseDouble(edits.get(2).getText().toString());
                            sms.updateFrom(parsedBody);
                            SmsTracker.SendSms(This, number, sms.getBody());
                            dialog.dismiss();
                        }
                    });

                    dlg.show(sms, "Отправить: " + whom);
                }
            }
        }
        else if(requestCode == PICK_FROMFILE_REQUEST) {
            if (resultCode == RESULT_OK) {
                String smsBody = data.getExtras().getString("smsbody");
                SmsTracker.ParsedBody parsedBody = new SmsTracker.ParsedBody(smsBody);
                setDestination(parsedBody.lat, parsedBody.lon);
            }
        }
    }
    @Override
    public void onBackPressed() {
        if(vewMode == ViewMode.Table) {
            this.setWatchView();
        }
        else {
            super.onBackPressed();
        }
    }
    @Override
    public void onDestroy() {
        EndMainThread();
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case ALL_PERMISSIONS_RESULT:
                for (String perms : permissionsToRequest) {
                    if (!hasPermission(perms)) {
                        permissionsRejected.add(perms);
                    }
                }

                if(permissionsRejected.size() > 0) {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if(shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                            showMessageOKCancel(
                                    "\"These permissions are mandatory for the application. Please allow access.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(permissionsRejected.toArray(
                                                        new String[permissionsRejected.size()]),
                                                        ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    });
                            return;
                        }
                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();

    }
}
