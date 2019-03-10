package com.mega.megagps;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.mega.megagps.MainActivity.PICK_CONTACT_REQUEST;
import static com.mega.megagps.MainActivity.PICK_FROMFILE_REQUEST;

public class TableActivity  extends Activity{
    private TableView tableView;
    private final String coordFileName = "Coordinates";
    private String action = null;
    List<SmsTracker.Sms> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table);

        float width = getIntent().getExtras().getInt("width");
        float height = getIntent().getExtras().getInt("height");

        TableLayout layout = (TableLayout) findViewById(R.id.table_header);

        TableRow row = (TableRow)layout.getChildAt(0);
        TextView textView0 = (TextView)row.getChildAt(0);
        textView0.setTextSize(TypedValue.COMPLEX_UNIT_PX, height / 30.0f);
        TextView textView1 = (TextView)row.getChildAt(1);
        textView1.setTextSize(TypedValue.COMPLEX_UNIT_PX, height / 30.0f);

        tableView = new TableView(this, width, height);
        ScrollView.LayoutParams params = new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.MATCH_PARENT);
        tableView.setLayoutParams(params);

        LinearLayout cl = (LinearLayout) findViewById(R.id.table_content);
        cl.addView(tableView);

        action = getIntent().getExtras().getString("action");
        //list = SmsTracker.getSmsList(this);

        if(action.equals("FromFile")) {
            list = SmsTracker.getSmsListFromFile(this);
        }
        else {
            list = SmsTracker.getSmsList(this);
        }
        textView0.setText("Место");
        textView1.setText("Время");

        String[] data = new  String[2];

        float[] weights = new float[2];
        weights[0] = 0.4f;
        weights[1] = 0.6f;

        for (int i = 0; i < list.size(); i++) {
            SmsTracker.ParsedBody parsedBody = list.get(i).parse();

            data[0] = parsedBody.place;
            data[1] = list.get(i).getDateStr();
            tableView.addRow(data, weights);
        }
            //tableView.setList(list);
        /*
        else if(action.equals("FromSms"))
        {
            textView0.setText("Место");
            textView1.setText("Время");

            list = SmsTracker.getSmsList(this);
            String[] data = new  String[2];

            float[] weights = new float[2];
            weights[0] = 0.4f;
            weights[1] = 0.6f;

            for (int i = 0; i < list.size(); i++) {
                data[0] = list.get(i).getAddress();
                data[1] = list.get(i).getDateStr();
                tableView.addRow(data, weights);
            }
            //tableView.setList(list);
        }
        */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_table, menu);
        for(int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            SpannableString spanString = new SpannableString(menu.getItem(i).getTitle().toString());
            int end = spanString.length();
            spanString.setSpan(new RelativeSizeSpan(1.5f), 0, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            item.setTitle(spanString);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {

        MenuItem item = menu.findItem(R.id.action_delete);
        item.setVisible(action.equals("FromFile") ? true : false);

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        final Activity activity = this;

        switch (item.getItemId()) {
            case R.id.action_apply:
                int selectedItem = tableView.getSelectedItem();
                Intent data = new Intent();

                data.putExtra("smsbody", list.get(selectedItem).getBody());
                setResult(RESULT_OK,data);
                finish();
                break;
            case R.id.action_edit_save: {
                final SmsTracker.Sms sms = list.get(tableView.getSelectedItem());
                if (sms != null) {
                    final Context This = this;
                    final EditSmsDialog dlg = new EditSmsDialog(
                            this,
                            new ContextThemeWrapper(this, android.R.style.Theme_Dialog));

                    dlg.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            List<EditText> edits = dlg.getTextEdits();

                            SmsTracker.ParsedBody parsedBody = sms.parse();
                            parsedBody.place = edits.get(0).getText().toString();
                            parsedBody.lat = Double.parseDouble(edits.get(1).getText().toString());
                            parsedBody.lon = Double.parseDouble(edits.get(2).getText().toString());
                            sms.updateFrom(parsedBody);

                            if (action.equals("FromFile")) {
                                // re-write the file
                                tableView.updateSelectedItem(0, edits.get(0).getText().toString());
                                SmsTracker.writeListToFile(activity, list);
                            } else {
                                // Save sms - add new record
                                SmsTracker.writeToFile(This, sms);
                            }
                            dialog.dismiss();
                        }
                    });

                    dlg.show(sms, "Поменять:");
                }
            }

                break;
            case R.id.action_share:
                Intent intent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts"));
                intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE); // Show user only contacts w/ phone numbers
                startActivityForResult(intent, PICK_CONTACT_REQUEST);
                break;

            case R.id.action_delete: {
                final int itemToDelete = tableView.getSelectedItem();
                final SmsTracker.Sms sms = list.get(itemToDelete);
                SmsTracker.ParsedBody parsedBody = sms.parse();

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(this, android.R.style.Theme_Dialog));
                alertDialog.setTitle("Удалить место " + parsedBody.place + "?");
                alertDialog.setPositiveButton("ДА", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        tableView.deleteSelectedItem();
                        list.remove(itemToDelete);
                        SmsTracker.writeListToFile(activity, list);
                    }
                });

                alertDialog.setNegativeButton("НЕТ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                alertDialog.show();
            }
                break;
            case R.id.action_cancel:
                break;
        }
        return true;
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
                String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER};

                Cursor cursor = getContentResolver()
                        .query(contactUri, projection, null, null, null);
                cursor.moveToFirst();

                // Retrieve the phone number from the NUMBER column
                int column = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                final String number = cursor.getString(column);

                final Context This = this;
                final EditSmsDialog dlg = new EditSmsDialog(
                        this,
                        new ContextThemeWrapper(this, android.R.style.Theme_Dialog));

                final SmsTracker.Sms sms = list.get(tableView.getSelectedItem());
                if(sms != null) {
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

                dlg.show(sms, "Сохранить:");
                }
            }
        }
    }
}

