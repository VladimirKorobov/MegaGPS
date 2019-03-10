package com.mega.megagps.Views;

import android.content.Context;
import android.view.MotionEvent;
import com.mega.graphics.DrawObjects.DrawingModel;
import com.mega.graphics.Views.ViewSurface2D;
import com.mega.megagps.MainActivity;

/**
 * Created by Vladimir on 31.03.2016.
 */
public class View2D extends ViewSurface2D{
    public View2D(Context context, DrawingModel model) {
        super(context, model);
    }

    @Override
    public boolean onTouchEvent(MotionEvent m) {
        float x = m.getX();
        float y = m.getY();
        int height = getHeight();
        int width = getWidth();
        int radius = Math.min(width, height) / 2;
        float dist = (radius - x) * (radius - x) + (radius - y) * (radius - y);
        if(dist < radius * radius / 4) {
            ((MainActivity)this.getContext()).openOptionsMenu();
        }
        return true;
    }
}
