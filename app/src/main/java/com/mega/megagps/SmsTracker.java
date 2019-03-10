package com.mega.megagps;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.telephony.SmsManager;
import android.text.format.DateFormat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SmsTracker{

    public static class ParsedBody {
        public ParsedBody(String body) {
            String[] data = body.split(":");
            if(data.length >= 2 )
                lat = Double.parseDouble(data[2]);
            if(data.length >= 4 )
                lon = Double.parseDouble(data[4]);
            if(data.length >= 6 )
                place = data[6];
        }

        public ParsedBody(double lat, double lon, String place) {
            this.lat = lat;
            this.lon = lon;
            this.place = place;
        }

        public String getBodyMesage() {
            return messagePrefix + "lat:" + Double.toString(lat) + ":lon:" + Double.toString(lon) +
                    ":place:" + place;
        }

        double lat;
        double lon;
        String place;
    }
    public static class Sms{
        private class Dummy{

        }

        private String id;
        private String address;
        private String body;
        private long date;

        public Sms() {
        }

        public Sms(String _id, String _address, String _body, long _date) {
            this.id = _id;
            this.address = _address;
            this.body = _body;
            this.date = _date;
        }

        public void copyFrom(Sms sms) {
            if(sms != null) {
                this.id = sms.id;
                this.address = sms.address;
                this.body = sms.body;
                this.date = sms.date;
            }
        }

        public void updateFrom(ParsedBody parcedBody) {
            this.body = parcedBody.getBodyMesage();
        }

        public ParsedBody parse() {
            return new ParsedBody(this.getBody());
        }

        public String getAddress() {
            return address;
        }

        public String getBody() {
            return body;
        }

        public long getDate() {
            return date;
        }

        public String getDateStr() {
            return SmsTracker.getDate(date);
        }
        public String getId() {
            return id;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public void setBody(String body) {
            this.body = body;
        }

        public void setDate(long date) {
            this.date = date;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    private static final String coordFileName = "Coordinates";
    public static final String messagePrefix = "MegaGPS:";

    // Message format: MegaGPS:lat:....lon:...
    private Context mContext;

    public SmsTracker(Context context) {
        mContext = context;
    }

    public static String getDate(long date) {
        return DateFormat.format("yyyy/MM/dd :: HH:mm", new Date(date)).toString();
    }

    public static String getDate() {
        return DateFormat.format("yyyy/MM/dd :: HH:mm", new Date()).toString();
    }

    public static List<Sms> getSmsList(Activity activity) {
        Uri inboxURI = Uri.parse("content://sms/inbox");
        // List required columns
        String[] reqCols = new String[]{"_id", "address", "body", "date"};
        // Get Content Resolver object, which will deal with Content Provider
        ContentResolver cr = activity.getContentResolver();
        // Fetch Inbox SMS Message from Built-in Content Provider
        Cursor c = cr.query(inboxURI, reqCols, "body LIKE ?", new String[]{messagePrefix + "%"}, "date");
        int totalSMS = c.getCount();

        List<Sms> list = new ArrayList<Sms>();

        if (c.moveToFirst()) {
            for (int i = 0; i < totalSMS; i++) {
                Sms sms = new Sms();
                sms.id = c.getString(c.getColumnIndexOrThrow("_id"));
                sms.address = c.getString(c.getColumnIndexOrThrow("address"));
                sms.body = c.getString(c.getColumnIndexOrThrow("body"));
                sms.date = c.getLong(c.getColumnIndexOrThrow("date"));
                list.add(sms);
                c.moveToNext();
            }
        }

        return list;
    }

    public static List<Sms> getSmsListFromFile(Activity activity) {
        File privateDir = activity.getFilesDir();
        File file = new File(privateDir, coordFileName);
        List<Sms> list = new ArrayList<Sms>();

        try {
            if(!file.exists()) {
                file.createNewFile();
            }
            if(file.exists()) {
                FileInputStream is = new FileInputStream(file);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                for(;;) {
                    Sms sms = new Sms();
                    sms.id = reader.readLine();
                    if(sms.id == null) {
                        break;
                    }
                    sms.address = reader.readLine();
                    if(sms.address == null) {
                        break;
                    }
                    sms.body = reader.readLine();
                    if(sms.body == null) {
                        break;
                    }
                    try {
                        sms.date = Long.parseLong(reader.readLine());
                        list.add(sms);
                    }
                    catch(Exception ex) {

                    }
                }
                reader.close();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }


    public String getCoordinateMessage(GpsTracker gpsTracker, String place) {
        ParsedBody parsedBody = new ParsedBody(
                gpsTracker.getLatitude(),
                gpsTracker.getLongitude(),
                place);
        return parsedBody.getBodyMesage();
    }

    public Sms createSms(String id, String address, String place, long date, GpsTracker gpsTracker) {
        String body = getCoordinateMessage(gpsTracker, place);
        return new Sms(id, address, body, date);
    }

    private static void writeToFile(File file, Sms sms) {
        try {
            FileOutputStream fileStream = new FileOutputStream(file, true);
            fileStream.write((sms.id + "\n").getBytes());
            fileStream.write((sms.address + "\n").getBytes());
            fileStream.write((sms.body + "\n").getBytes());
            fileStream.write((sms.date + "\n").getBytes());
            fileStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeToFile(Context context, Sms sms) {
        File privateDir = context.getFilesDir();
        File file = new File(privateDir, coordFileName);
        try {
            if(!file.exists()) {
                file.createNewFile();
            }
            if(file.exists()) {
                writeToFile(file, sms);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeToFile(String id, String address, String place, long date, GpsTracker gpsTracker) {
        File privateDir = mContext.getFilesDir();
        File file = new File(privateDir, coordFileName);
        try {
            if(!file.exists()) {
                file.createNewFile();
            }
            if(file.exists()) {
                Sms sms = createSms(id, "", place, date, gpsTracker  );
                writeToFile(file, sms);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeListToFile(Activity activity, List<SmsTracker.Sms> list) {
        File privateDir = activity.getFilesDir();
        File file = new File(privateDir, coordFileName);
        try {
            if(!file.exists()) {
                file.createNewFile();
            }
            if(file.exists()) {
                try {
                    FileOutputStream fileStream = new FileOutputStream(file, false);
                    for(SmsTracker.Sms sms : list) {
                        fileStream.write((sms.id + "\n").getBytes());
                        fileStream.write((sms.address + "\n").getBytes());
                        fileStream.write((sms.body + "\n").getBytes());
                        fileStream.write((sms.date + "\n").getBytes());
                    }
                    fileStream.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.DONUT)
    public static void SendSms(Context context, String phoneNumber, String message) {
        PendingIntent pi = PendingIntent.getActivity(context, 0, new Intent(context, Sms.Dummy.class), 0);
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, pi, null);
    }

    public void GetLastSmsCoord(double[] latlon) {
        List<Sms> sms = getSmsList((Activity)mContext);
        if(sms.size() > 0) {
            ParsedBody body = sms.get(sms.size() - 1).parse();
            latlon[0] = body.lat;
            latlon[1] = body.lon;
        }
    }
}
