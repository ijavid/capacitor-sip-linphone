package eu.ijavid.capacitor.sip;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.collection.ArraySet;

public class PushBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "SipPhonePushBroadcastReceiver";
    private final ArraySet<SipPhoneEventListener> listeners = new ArraySet<>();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "onReceive: action=" + intent.getAction() + " package=" + context.getPackageName());

        Toast.makeText(context, "Push received with app shut down", Toast.LENGTH_LONG).show();

        // A push have been received but there was no Core alive, you should create it again
        // This way the core will register and it will handle the message or call event like if the app was started
        for (var listener: listeners) {
            listener.onSipPushReceived(context, intent);
        }
    }

    public void addListener(SipPhoneEventListener listener) {
        listeners.add(listener);
    }
}
