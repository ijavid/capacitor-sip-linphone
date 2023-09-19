package eu.ijavid.capacitor.sip;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.PowerManager;
import android.os.Build;

import android.os.Vibrator;
import android.provider.Settings;
import android.telecom.Call;
import android.util.Log;

import android.view.WindowManager;

import androidx.core.app.NotificationCompat;

import com.getcapacitor.Plugin;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.UUID;

public class CallKit {



    public static Activity getContextActivity() {
        return plugin.getActivity();
    }

    private static Plugin plugin;


    public static final String TAG = "CallKit";

    public static PowerManager powerManager;
    public static PowerManager.WakeLock wakeLock;
    private static Ringtone ringtone;
    private static Vibrator vibrator;
    private static String callName;


    public static void initialize(Plugin plugin) {
        Log.v(TAG, "Init CallKit");
        CallKit.plugin = plugin;
        powerManager = (PowerManager) getContextActivity().getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock((PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE), getContextActivity().getPackageName() + ":" + TAG);
        Log.v(TAG, "Init CallKit OK");
    }
//
//    public boolean execute(String action, final JSONArray args) throws JSONException {
//
//        if (action == null) {
//            return false;
//        }
//
//        if (action.equals("register")) {
//            try {
//                this.register(args, callbackContext);
//            }
//            catch (Exception exception) {
//                callbackContext.error("CallKit uncaught exception: " + exception.getMessage());
//            }
//
//            return true;
//        }
//        else if (action.equals("reportIncomingCall")) {
//            try {
//                this.reportIncomingCall(args, callbackContext);
//            }
//            catch (Exception exception) {
//                callbackContext.error("CallKit uncaught exception: " + exception.getMessage());
//            }
//
//            return true;
//        }
//        else if (action.equals("startCall")) {
//            try {
//                this.startCall(args, callbackContext);
//            }
//            catch (Exception exception) {
//                callbackContext.error("CallKit uncaught exception: " + exception.getMessage());
//            }
//
//            return true;
//        }
//        else if (action.equals("callConnected")) {
//            try {
//                this.callConnected(args, callbackContext);
//            }
//            catch (Exception exception) {
//                callbackContext.error("CallKit uncaught exception: " + exception.getMessage());
//            }
//
//            return true;
//        }
//        else if (action.equals("endCall")) {
//            try {
//                this.endCall(args, callbackContext);
//            }
//            catch (Exception exception) {
//                callbackContext.error("CallKit uncaught exception: " + exception.getMessage());
//            }
//
//            return true;
//        }
//        else if (action.equals("finishRing")) {
//            try {
//                this.finishRing(args, callbackContext);
//            }
//            catch (Exception exception) {
//                callbackContext.error("CallKit uncaught exception: " + exception.getMessage());
//            }
//
//            return true;
//        }
//
//        return false;
//
//    }

    public static void register() throws JSONException {
        /* initialize the ringtone */
        Context ctx = getContextActivity().getBaseContext();
        Uri ringtoneUri;

        int ringtoneID = ctx.getResources().getIdentifier("ringtone","raw", ctx.getPackageName());
        if (ringtoneID != 0 ) {
            ringtoneUri = Uri.parse("android.resource://" + ctx.getPackageName() + "/" + ringtoneID);
        } else {
            ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        }

        ringtone = RingtoneManager.getRingtone(ctx, ringtoneUri);
        if (Build.VERSION.SDK_INT >= 21) {
            AudioAttributes aa = new AudioAttributes.Builder()
                    .setFlags(AudioAttributes.USAGE_NOTIFICATION_RINGTONE | AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_REQUEST)
                    .build();
            ringtone.setAudioAttributes(aa);
        } else {
            ringtone.setStreamType(RingtoneManager.TYPE_RINGTONE);
        }
        ringtone.stop();

//
    }

    public static void reportIncomingCall(String callName, boolean hasVideo) throws JSONException {
        CallKit.callName = callName;
//        callName = args.getString(0);
//        boolean hasVideo = args.getBoolean(1);

        final String uuid = UUID.randomUUID().toString();

        getContextActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    String packageName = getContextActivity().getApplicationContext().getPackageName();

                    Intent intent = new Intent("android.intent.action.MAIN");
                    intent.setComponent(new ComponentName(packageName, packageName + ".MainActivity"));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    getContextActivity().getApplicationContext().startActivity(intent);
                } catch (Exception e)  {
                    Log.v(TAG, "CallKit error: " + e.getMessage());
                }
                getContextActivity().getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                );
            }
        });

        if(wakeLock.isHeld()) {
            wakeLock.release();
        }
        wakeLock.acquire();
        try {
            boolean vibrate = false;
            Uri ringtoneUri;

            AudioManager audioManager = (AudioManager) getContextActivity().getApplication().getSystemService(Context.AUDIO_SERVICE);

            Context ctx = getContextActivity().getBaseContext();

            if(ringtone.isPlaying()) {
                ringtone.stop();
            }
            ringtone.play();

            if(audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE){
                vibrate = true;
            } else if (1 == Settings.System.getInt(ctx.getContentResolver(), "vibrate_when_ringing", 0)) //vibrate on
                vibrate = true;

            vibrator = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrate) {
                vibrator.vibrate(new long[] {0, 1000, 1000}, 0);
            }

        } catch (Exception e) {
            Log.v(TAG, "CallKit error: " + e.getMessage());
        }


    }

    public static void callConnected(final JSONArray args) throws JSONException {
        String uuid = args.getString(0);

        /* do nothing... */


    }

    public static void startCall(final JSONArray args) throws JSONException {
        callName = args.getString(0);
        boolean isVideo = args.getBoolean(1);

        final String uuid = UUID.randomUUID().toString();

        /* do nothing... */


    }

    public static void notifyUser(String uuid) {
        String appName;
        ApplicationInfo app = null;

        Context context = getContextActivity().getApplicationContext();
        PackageManager packageManager = getContextActivity().getPackageManager();

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        try {
            app = packageManager.getApplicationInfo(getContextActivity().getPackageName(), 0);
            appName = (String)packageManager.getApplicationLabel(app);
        } catch (PackageManager.NameNotFoundException e) {
            appName = "Incoming";
            e.printStackTrace();
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setContentTitle( appName + " call missed" )
                .setContentText( callName )
                .setSound( defaultSoundUri );

//        int resID = context.getResources().getIdentifier("callkit_missed_call", "drawable", getContextActivity().getPackageName());
//        if (resID != 0) {
//            notificationBuilder.setSmallIcon(resID);
//        } else {
//            notificationBuilder.setSmallIcon(app.icon);
//        }
//        notificationBuilder.setLargeIcon( BitmapFactory.decodeResource( context.getResources(), app.icon ) );

        PendingIntent contentIntent = PendingIntent.getBroadcast(context, 0, new Intent(context, CallKitReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(contentIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(uuid.hashCode(), notificationBuilder.build());
    }

    public static void finishRing(final JSONArray args) throws JSONException {
        String uuid = args.getString(0);

        if(ringtone.isPlaying()) {
            ringtone.stop();
        }
        vibrator.cancel();


    }

    public static void endCall(final JSONArray args) throws JSONException {
        String uuid = args.getString(0);
        boolean notify = args.getBoolean(1);

        getContextActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getContextActivity().getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        });

        if(wakeLock.isHeld()) {
            wakeLock.release();
        }

        finishRing(args);

        if (notify) {
            CallKit.notifyUser(uuid);
        }


    }

}
