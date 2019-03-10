package com.mega.megagps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.util.TypedValue;
import com.mega.graphics.DrawObjects.*;
import com.mega.graphics.Renderers.IRenderer;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Vladimir on 31.03.2016.
 */
public class MegaModel extends DrawingModel{
    private Context context;
    private GpsTracker mGpsTracker;
    private TransformObject Arrow;
    private TransformObject directionArrow;
    private TransformObject directionGpsArrow;
    private OffsetObject latitudeOffset;
    private OffsetObject longitudeOffset;
    private OffsetObject altitudeOffset;
    private OffsetObject speedOffset;
    private OffsetObject directionOffset;

    private OffsetObject destDirectionOffset;
    private OffsetObject destDistanceOffset;


    private float centerX;
    private float centerY;
    private double Angle = 0;
    private double Azimuth = 0;
    private double GpsAzimuth = 0;

    private double destLat = 0;
    private double destLon = 0;

    private boolean useDest = false;

    private float screenWidth;
    private float screenHeignt;
    private float fontSize;
    ReentrantLock lock = new ReentrantLock();

    public MegaModel(Context context, GpsTracker gps) {
        this.context = context;
        mGpsTracker = gps;

        if(!mGpsTracker.canGetLocation()) {
            mGpsTracker.showSettingsAlert();
        }
    }

    public void setAngle(double angle) {
        lock.lock();
        try {
            Angle = angle;
        }
        finally {
            lock.unlock();
        }
    }

    public double getAngle() {
        lock.lock();
        try {
            /*
            int orient = this.context.getResources().getConfiguration().orientation;
            if(orient == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                return Angle + 90;
            }
            else {
                return Angle;
            }
            */
            return Angle;
        }
        finally {
            lock.unlock();
        }
    }

    public void setDestination(double distLat, double distLon) {
        this.destLat = distLat;
        this.destLon = distLon;
        this.useDest = true;
    }

    public void eraseDestination() {
        this.useDest = false;
    }

    private void AddArrow(float width, float height, float factor) {
        // Add arrow
        BitmapObject bitmapSecondArrow = new BitmapObject(context, R.drawable.compass_arrow);
        TransformObject transformObjectSecondArrow = new TransformObject(bitmapSecondArrow);
        transformObjectSecondArrow.getMatrix().postScale(factor, factor);
        RectF rect = transformObjectSecondArrow.getRect();
        float objectXCorner = centerX - rect.width() / 2;
        float objectYCorner = centerY - rect.height() / 2;
        transformObjectSecondArrow.getMatrix().postTranslate(objectXCorner, objectYCorner);
        Arrow = new TransformObject(transformObjectSecondArrow);
        this.model.add(Arrow);

        BitmapObject bitmapDirArrow = new BitmapObject(context, R.drawable.direction_arrow);
        TransformObject transformObjectDirArrow = new TransformObject(bitmapDirArrow);
        transformObjectDirArrow.getMatrix().postScale(factor, factor);
        rect = transformObjectDirArrow.getRect();
        objectXCorner = centerX - rect.width() / 2;
        objectYCorner = centerY - rect.height() / 2;
        transformObjectDirArrow.getMatrix().postTranslate(objectXCorner, objectYCorner);
        directionArrow = new TransformObject(transformObjectDirArrow);
        this.model.add(directionArrow);
        directionArrow.setVisible(false);

        BitmapObject bitmapDirGps = new BitmapObject(context, R.drawable.direction_gps);
        TransformObject transformObjectDirGps = new TransformObject(bitmapDirGps);
        transformObjectDirGps.getMatrix().postScale(factor, factor);
        rect = transformObjectDirGps.getRect();
        objectXCorner = centerX - rect.width() / 2;
        objectYCorner = centerY - rect.height();
        transformObjectDirGps.getMatrix().postTranslate(objectXCorner, objectYCorner);
        directionGpsArrow = new TransformObject(transformObjectDirGps);

        this.model.add(directionGpsArrow);
        directionGpsArrow.setVisible(false);
    }
    private float AddWatch(float width, float height, DrawingModel model) {
        // Add case
        BitmapObject bitmap = new BitmapObject(context, R.drawable.compass_case);

        double kx = width / bitmap.getBitmap().getWidth();
        double ky = height / bitmap.getBitmap().getHeight();
        float k = (float) ((kx < ky) ? kx : ky);

        //k *= 0.5f;
        //k *= 0.8f;

        TransformObject transformObject = new TransformObject(bitmap);
        transformObject.getMatrix().postScale(k, k);

        RectF rect = transformObject.getRect();

        centerX = (width < height) ? width / 2 : height / 2;
        //centerX *= 0.8f;
        centerY = centerX;

        float maxDim = (width < height) ? height : width;
        fontSize = (maxDim - 2 * centerX) / 10;

        transformObject.getMatrix().postTranslate(centerX - rect.width() / 2, centerY - rect.height() / 2);

        model.Add(transformObject);

        return k;
    }

    private void AddGPSLocation() {

        TextObject latitude = new TextObject();
        latitude.setSize(fontSize);
        latitude.setColor(0xFFFFFF00);

        float startX = 20;
        float startY = 2.5f * centerY + fontSize * 1.1f;

        latitudeOffset = new OffsetObject(startX, startY, latitude);
        this.Add(latitudeOffset);

        TextObject longitude = new TextObject();
        longitude.setSize(fontSize);
        longitude.setColor(0xFFFFFF00);

        startY += fontSize * 1.1f;

        longitudeOffset = new OffsetObject(startX, startY, longitude);
        this.Add(longitudeOffset);

        TextObject altitude = new TextObject();
        altitude.setSize(fontSize);
        altitude.setColor(0xFFFFFF00);

        startY += fontSize * 1.1f;

        altitudeOffset = new OffsetObject(startX, startY, altitude);
        this.Add(altitudeOffset);

        TextObject speed = new TextObject();
        speed.setSize(fontSize);
        speed.setColor(0xFFFFFF00);

        startY += fontSize * 1.1f;

        speedOffset = new OffsetObject(startX, startY, speed);
        this.Add(speedOffset);

        // Add direction
        TextObject direction = new TextObject();
        direction.setSize(fontSize);
        direction.setColor(0xFFFFFF00);

        startX = 20;
        startY = fontSize * 1.1f;

        directionOffset = new OffsetObject(startX, startY, direction);
        this.Add(directionOffset);

        // Add destination direction
        TextObject destDirection = new TextObject();
        destDirection.setSize(fontSize);
        destDirection.setColor(0xFFFFFF00);

        startX = screenWidth - fontSize * 2;
        destDirectionOffset = new OffsetObject(startX, startY, destDirection);
        destDirectionOffset.setVisible(false);
        this.Add(destDirectionOffset);

        // Add destination distance
        TextObject destDistance = new TextObject();
        destDistance.setSize(fontSize);
        destDistance.setColor(0xFFFFFF00);

        startX = 20;
        startY = 2.1f * centerY + fontSize * 1.1f;
        destDistanceOffset = new OffsetObject(startX, startY, destDistance);
        destDistanceOffset.setVisible(false);
        this.Add(destDistanceOffset);
    }

    private void UpdateGPSLocation() {
        destDistanceOffset.setVisible(false);
        destDirectionOffset.setVisible(false);
        directionArrow.setVisible(false);
        directionGpsArrow.setVisible(false);

        mGpsTracker.getLocation();
        if(mGpsTracker.canGetLocation()) {
            double lat = mGpsTracker.getLatitude();
            double lon = mGpsTracker.getLongitude();

            TextObject latitude = (TextObject) latitudeOffset.getDrawingObject();
            latitude.setText("Шир:  " + Double.toString(lat));

            TextObject longitude = (TextObject) longitudeOffset.getDrawingObject();
            longitude.setText("Долг: " + Double.toString(lon));

            TextObject altitude = (TextObject) altitudeOffset.getDrawingObject();
            altitude.setText("Выс:  " + Double.toString(mGpsTracker.getAltitude()));

            TextObject speed = (TextObject) speedOffset.getDrawingObject();
            speed.setText("Скор: " + Double.toString(mGpsTracker.getSpeed() * 3.6));

            latitudeOffset.setVisible(true);
            longitudeOffset.setVisible(true);
            altitudeOffset.setVisible(true);
            speedOffset.setVisible(true);

            if (useDest) {
                Azimuth = mGpsTracker.getDestDirection();
                GpsAzimuth = Azimuth - mGpsTracker.getMotionDirection();

                if(GpsAzimuth < 0) GpsAzimuth += 360;

                TextObject distance = (TextObject) destDistanceOffset.getDrawingObject();
                int dist = (int)Math.round(mGpsTracker.getDestDistance());
                distance.setText("Дист: " + Integer.toString(dist));

                TextObject direction = (TextObject) destDirectionOffset.getDrawingObject();

                direction.setText(Integer.toString((int) Azimuth));

                destDistanceOffset.setVisible(true);
                destDirectionOffset.setVisible(true);
                directionArrow.setVisible(true);
                directionGpsArrow.setVisible(true);
            }
        }
        else {
            latitudeOffset.setVisible(false);
            longitudeOffset.setVisible(false);
            altitudeOffset.setVisible(false);
            speedOffset.setVisible(false);
        }

    }

    @Override
    public void Create(float width, float height) {
        screenWidth = width;
        screenHeignt = height;
        this.model.clear();
        BitmapObject bitmapObject = new BitmapObject(width, height, Bitmap.Config.ARGB_8888);
        IRenderer bitmapRenderer = bitmapObject.CreateRenderer(context);
        DrawingModel bitmapModel = new DrawingModel();

        RectObject background = new RectObject();
        background.setLeft(0);
        background.setTop(0);
        background.setRight(width);
        background.setBottom(height);
        background.setColor(0xFF000000);
        bitmapModel.Add(background);

        //AddKnob(width, height, bitmapModel);
        float factor = AddWatch(width, height, bitmapModel);
        bitmapModel.Draw(bitmapRenderer);

        this.Add(bitmapObject);
        bitmapModel.Dispose();

        AddArrow(width, height, factor);
        AddGPSLocation();
    }
    @Override
    public void Draw(IRenderer renderer) {
        float angle = (float)((MainActivity)context).getMegaModel().getAngle();
        Arrow.getMatrix().reset();
        Arrow.getMatrix().postRotate(angle, centerX, centerY);

        TextObject direction = (TextObject)directionOffset.getDrawingObject();
        direction.setText(Integer.toString(360 - Math.round(angle < 0 ? angle + 360 : angle)));

        UpdateGPSLocation();

        if(useDest) {
            //Draw compass direction arrow
            float az = (float)((angle + 360) + Azimuth);
            while(az > 360) az -= 360;
            if(az >180) az -= 360;
            directionArrow.getMatrix().reset();
            directionArrow.getMatrix().postRotate(az, centerX, centerY);

            //Draw GPS direction arrow
            az = (float)GpsAzimuth;
            while(az > 360) az -= 360;
            if(az >180) az -= 360;
            directionGpsArrow.getMatrix().reset();
            directionGpsArrow.getMatrix().postRotate(az, centerX, centerY);
        }

        super.Draw(renderer);
    }
}
