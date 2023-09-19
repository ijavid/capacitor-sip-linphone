package eu.ijavid.capacitor.sip;

import android.net.Uri;
import android.telecom.Connection;
import android.telecom.ConnectionRequest;
import android.telecom.ConnectionService;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.util.Log;

import java.util.HashMap;


public class SipPhoneConnectionService extends ConnectionService {
    private static final String TAG = "SipPhoneConnectionService";
//    private static final String YOUR_CHANNEL_ID = "YOUR_CHANNEL_ID_XXX";

    public Connection onCreateIncomingConnection(final PhoneAccountHandle connectionManagerPhoneAccount, final ConnectionRequest request) {
        Log.v(TAG, "onCreateIncomingConnection");
        var caller = (Uri) request.getExtras().get(TelecomManager.EXTRA_INCOMING_CALL_ADDRESS);
        Log.v(TAG, "onCreateIncomingConnection: " + caller.getHost() + " | " + caller.getPath() + " | " + caller);
//        var con = new Connection() {
//            @Override
//            public void onShowIncomingCallUi() {
//                Log.v(TAG, "onCreateIncomingConnection: onShowIncomingCallUi ");
//                this.setActive();
//            }
//        };
        HashMap<String, String> extrasMap = new HashMap<>();
        var con = new VoiceConnection(this, extrasMap );
//        var con = super.onCreateIncomingConnection(connectionManagerPhoneAccount, request);

        Log.v(TAG, "onCreateIncomingConnection: " + con);
        con.setConnectionProperties(Connection.PROPERTY_SELF_MANAGED);
        con.setCallerDisplayName(caller.getHost(), TelecomManager.PRESENTATION_ALLOWED);
        con.setAddress(caller, TelecomManager.PRESENTATION_ALLOWED);
//        con.setRinging();
        return con;
    }

    public void onCreateIncomingConnectionFailed(final PhoneAccountHandle connectionManagerPhoneAccount, final ConnectionRequest request) {
        Log.e(TAG, "onCreateIncomingConnectionFailed");
        var caller = request.getExtras().getString(TelecomManager.EXTRA_INCOMING_CALL_ADDRESS);
        Log.v(TAG, "onCreateIncomingConnectionFailed: " + caller);
    }


//    @RequiresApi(api = Build.VERSION_CODES.O)
//    public void sendNotification() {
//        Log.v(TAG, "sendNotification");
//        NotificationChannel channel = new NotificationChannel(YOUR_CHANNEL_ID, "Incoming Calls",
//                NotificationManager.IMPORTANCE_HIGH);
//        // other channel setup stuff goes here.
//
//        // We'll use the default system ringtone for our incoming call notification channel.  You can
//        // use your own audio resource here.
//        Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
//        channel.setSound(ringtoneUri, new AudioAttributes.Builder()
//                // Setting the AudioAttributes is important as it identifies the purpose of your
//                // notification sound.
//                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
//                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
//                .build());
//
//        NotificationManager mgr = getApplicationContext().getSystemService(NotificationManager.class);
//        mgr.createNotificationChannel(channel);
//
//
//        // Create an intent which triggers your fullscreen incoming call user interface.
////        Intent intent = new Intent(Intent.ACTION_MAIN, null);
////        intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION | Intent.FLAG_ACTIVITY_NEW_TASK);
////        intent.setClass(this, Ac.class);
//        // TODO this activity should later call connection.setActive();
////        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_MUTABLE);// .FLAG_MUTABLE_UNAUDITED);
//
//        // Build the notification as an ongoing high priority item; this ensures it will show as
//        // a heads up notification which slides down over top of the current content.
//        final Notification.Builder builder = new Notification.Builder(getApplicationContext());
//        builder.setOngoing(true);
//        builder.setPriority(Notification.PRIORITY_HIGH);
//
//        // Set notification content intent to take user to fullscreen UI if user taps on the
//        // notification body.
////        builder.setContentIntent(pendingIntent);
//        // Set full screen intent to trigger display of the fullscreen UI when the notification
//        // manager deems it appropriate.
////        builder.setFullScreenIntent(pendingIntent, true);
//
//        // Setup notification content.
////        builder.setSmallIcon( yourIconResourceId );
//        builder.setContentTitle("Your notification title");
//        builder.setContentText("Your notification content.");
//
//        // Set notification as insistent to cause your ringtone to loop.
//        Notification notification = builder.build();
//        notification.flags |= Notification.FLAG_INSISTENT;
//
//        // Use builder.addAction(..) to add buttons to answer or reject the call.
//        NotificationManager notificationManager = getApplicationContext().getSystemService(
//                NotificationManager.class);
//        notificationManager.notify(YOUR_CHANNEL_ID, 999999, notification);
//
//        // TODO this activity should later call connection.setActive();
//    }




    public void deinitConnection(String s) {
        Log.e(TAG, "deinitConnection - " + s);
    }
}
