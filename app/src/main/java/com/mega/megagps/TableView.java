package com.mega.megagps;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;

public class TableView extends ScrollView {//} implements View.OnClickListener{
    private Context mContext;
    final int headerColor = Color.GRAY;
    final int headerStyle = Typeface.BOLD;
    private int textColor = Color.BLACK;
    final int bkColor = Color.WHITE;
    final int textColorSel = Color.WHITE;
    final int bkColorSel = Color.GRAY;

    final int textStyle = Typeface.NORMAL;
    private float textSize;

    private float width;
    private float height;

    private float downX = 0;
    private float downY = 0;
    //private List<SmsTracker.Sms> smsList;

    private float moveX = 0;
    private float moveY = 0;
    boolean bChanged = false;

    float scrollPos = 0;
    private int selectedItemNum = -1;

    private TableLayout tableLayout = null;

    private void addData(String[] headers, float[] weights, boolean bHeader) {
        TableRow tr = new TableRow(mContext);
        tr.setBackgroundColor(Color.WHITE);
        tr.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.MATCH_PARENT, 1));

        int color = bHeader ? headerColor : textColor;
        int style = bHeader ? headerStyle : textStyle;

        for(int i = 0; i < headers.length; i ++) {

            TextView cell = new TextView(mContext);
            cell.setText(headers[i]);
            cell.setTextColor(color);
            cell.setTextSize(TypedValue.COMPLEX_UNIT_PX, this.textSize);
            cell.setLineSpacing(0, 2);
            cell.setTypeface(Typeface.DEFAULT, style);
            //cell.setBackgroundColor(Color.WHITE);
            //cell.setOnClickListener(this);

            TableRow.LayoutParams params = new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.MATCH_PARENT, 1);

            //params.setMargins(0,0,2,2);
            cell.setMaxLines(1);

            if(weights != null) {
                params.width = 0;
                params.weight = weights[i];
            }
            cell.setLayoutParams(params);

            cell.setPadding(5, 0, 5, 0);
            tr.addView(cell);
        }

        tableLayout.addView(tr);
    }

    public TableView(Context context) {
        super(context);
        mContext = context;
        width = ((Activity)mContext).findViewById(R.id.mainLayout).getWidth();
        height = ((Activity)mContext).findViewById(R.id.mainLayout).getHeight();

        tableLayout = new TableLayout(mContext);
        this.addView(tableLayout);
        textSize = height / 30;

        tableLayout.setOnTouchListener((View.OnTouchListener)mContext);
    }

    public TableView(Context context, float width, float height) {
        super(context);
        mContext = context;
        this.width = width;
        this.height = height;

        tableLayout = new TableLayout(mContext);
        try {
            //tableLayout.removeAllViews();
            this.addView(tableLayout);
            textSize = height / 30;
        }
        catch(Exception ex)
        {
            String s = ex.getMessage();
            s = "";
        }

        //tableLayout.setOnTouchListener((View.OnTouchListener)mContext);
    }

    private void selectItem(int item) {
        TableRow tr;
        if(selectedItemNum >= 0) {
            tr = (TableRow)tableLayout.getChildAt(selectedItemNum);
            tr.setBackgroundColor(bkColor);
            for(int i = 0; i < tr.getChildCount(); i ++) {
                TextView tv = (TextView) tr.getChildAt(i);
                tv.setTextColor(textColor);
            }
        }
        if(selectedItemNum != item) {
            tr = (TableRow) tableLayout.getChildAt(item);
            tr.setBackgroundColor(bkColorSel);
            for (int i = 0; i < tr.getChildCount(); i++) {
                TextView tv = (TextView) tr.getChildAt(i);
                tv.setTextColor(textColorSel);
            }
            selectedItemNum = item;
        }
        else {
            selectedItemNum = -1;
        }
    }

    public void addHeaders(String[] headers) {
        addData(headers, null, true);
    }
    public void addRow(String[] data, float[] weights) {
        addData(data, weights, false);
    }

    /*
    public void setList(List<SmsTracker.Sms> list) {
        smsList = list;
    }
    */

    public ScrollView getScrollView() {
        return this;
    }

    public TableLayout getLayout() {
        return tableLayout;
    }

    public int getSelectedItem() {
        return selectedItemNum;
    }

    //@RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @TargetApi(Build.VERSION_CODES.FROYO)
    @Override
    public boolean onTouchEvent(MotionEvent m) {
        float x = m.getX();
        float y = m.getY();

        switch(m.getActionMasked())
        {
            case MotionEvent.ACTION_DOWN:
                downX = x;
                downY = y;
                moveX = x;
                moveY = y;
                scrollPos = this.getScrollY();
                break;

            case MotionEvent.ACTION_UP:
                if(Math.abs(y - downY) < 10) {
                    if(tableLayout.getChildCount() > 0)
                    {
                        float lineHeight = tableLayout.getHeight() / tableLayout.getChildCount();
                        int row = (int)((downY + scrollPos) / lineHeight);
                        if(row >= 0 && row < tableLayout.getChildCount()) {
                            selectItem(row);
                            if(selectedItemNum >= 0) {
                                ((TableActivity) this.getContext()).openOptionsMenu();
                            }
                        }
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE:

                int curPos = (int)(scrollPos + downY - y + 0.5f);
                this.scrollTo(0, curPos);

                //this.setVerticalScrollbarPosition(scrollPos);
                break;
        }
        return true;
    }

    public TableRow getSelectedRow() {
        if(selectedItemNum >= 0) {
            return (TableRow)tableLayout.getChildAt(selectedItemNum);
        }
        else {
            return null;
        }
    }
    /*
    public SmsTracker.Sms getSelectedSms() {
        if(selectedItemNum >= 0) {
            return smsList.get(selectedItemNum);
        }
        else {
            return null;
        }
    }
    */

    public void deleteSelectedItem() {
        if(selectedItemNum >= 0) {
            //smsList.remove(selectedItemNum);
            tableLayout.removeViewAt(selectedItemNum);
            //SmsTracker.writeListToFile((Activity)mContext, smsList);
            selectedItemNum = -1;
            bChanged = true;
        }
    }

    public void updateSelectedItem(int column, String data) {
        if(selectedItemNum >= 0) {
            TableRow row = (TableRow) tableLayout.getChildAt(selectedItemNum);
            ((TextView) row.getChildAt(column)).setText(data);
        }
    }

    public void updateSelectedItem(String[] data) {
        if(selectedItemNum >= 0) {
            TableRow row = (TableRow)tableLayout.getChildAt(selectedItemNum);
            for(int i = 0; i < data.length; i ++) {
                ((TextView)row.getChildAt(i)).setText(data[i]);
            }
        }
    }

    /*
    @Override
    public void onClick(View v) {
        float y = v.getTop() + v.getHeight() / 2;
    }
    */
}
