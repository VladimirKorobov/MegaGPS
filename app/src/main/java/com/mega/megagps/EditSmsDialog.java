package com.mega.megagps;

import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.ContextThemeWrapper;

public class EditSmsDialog extends EditAlertDialog{
    public EditSmsDialog(Context context) {
        super(context);
    }

    public EditSmsDialog(Context context, ContextThemeWrapper wrapper) {
        super(context);
    }

    public void show(SmsTracker.Sms sms, String title) {
        SmsTracker.ParsedBody parsedBody = sms.parse();
        String[] names = new String[]{"Место", "Широта", "Долгота"};
        String[] values = new String[]{parsedBody.place, Double.toString(parsedBody.lat),
                Double.toString(parsedBody.lon)};
        float[] weights = new float[]{0.25f, 0.25f, 0.25f};
        int[] textTypes = new int[] {
                InputType.TYPE_CLASS_TEXT,
                InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL,
                InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL};
        this.setTitle(title);

        setNegativeButton("Отмена", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        this.show(names, values, weights, textTypes);
    }
}
