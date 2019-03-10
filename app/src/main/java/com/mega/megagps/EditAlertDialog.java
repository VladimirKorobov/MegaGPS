package com.mega.megagps;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class EditAlertDialog extends AlertDialog.Builder{
    TableLayout tableLayout = null;
    Context mContext;
    boolean result = false;
    List<EditText> list;

    public EditAlertDialog(Context context) {
        super(context);
        mContext = context;
    }

    public EditAlertDialog(Context context, ContextThemeWrapper wrapper) {
        super(wrapper);
        mContext = context;
    }

    List<EditText> getTextEdits() {
        return list;
    }
    public void show(String[] names, String[] values, float[] weights) {

        tableLayout = new TableLayout(mContext);
        list = new  ArrayList<EditText>();
        for(int i = 0; i < names.length; i ++) {
            list.add(addtext(tableLayout, names[i], values[i], weights[i]));
        }
        this.setView(tableLayout);
        /*
        this.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                res[0] = false;
                dialog.cancel();
            }
        });
        this.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for(int i = 0; i < list.size(); i ++) {
                    values[i] = list.get(i).getText().toString();
                }
                res[0] = true;
                dialog.dismiss();
            }
        });
        */

        this.show();
    }

    private EditText addtext(TableLayout layout, String name, String value, float weight) {
        TableRow tableRow = new TableRow(mContext);
        tableRow.setBackgroundColor(Color.BLACK);

        // Create row with black paddings
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.MATCH_PARENT,
                1f);
        tableRow.setPadding(0, 0, 0, 2);
        tableRow.setLayoutParams(params);

        // Create text view for field name
        TextView textView = new TextView(mContext);
        textView.setText(name);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 30f);
        textView.setBackgroundColor(Color.WHITE);
        textView.setTextColor(Color.GRAY);
        textView.setPadding(5, 0, 5, 2);
        textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        params = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.MATCH_PARENT,
                weight);
        params.setMargins(0,0, 2, 0);
        textView.setLayoutParams(params);
        tableRow.addView(textView);

        // Create text edit for field value
        EditText editText = new EditText(mContext);
        editText.setText(value);
        editText.setPadding(5, (int)(editText.getTextSize() / 2), 5, 2);
        editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, 30f);
        editText.setBackgroundColor(Color.WHITE);
        editText.setTextColor(Color.BLACK);

        params = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.MATCH_PARENT,
                1.f - weight);

        editText.setLayoutParams(params);
        tableRow.addView(editText);

        layout.addView(tableRow);

        return editText;
    }
}
