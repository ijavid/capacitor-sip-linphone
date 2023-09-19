package eu.ijavid.capacitor.sip;

import android.telecom.Connection;
import android.telecom.DisconnectCause;
import android.util.Log;

public class MyConnection extends Connection {

    private static final String TAG = "SipPhoneConnection";

    private final SipPhoneConnectionService context;

    public MyConnection(SipPhoneConnectionService context) {
        super();
        this.context = context;
    }

    @Override
    public void onShowIncomingCallUi() {
        Log.v(TAG, "onShowIncomingCallUi");
//        context.sendNotification();
        super.onShowIncomingCallUi();
    }

    @Override
    public void onAnswer() {
        Log.v(TAG, "onAnswer");
//                this.setInitialized();
        super.onAnswer();
        setActive();
//                this.setActive();
//                CallKitVoipPlugin plugin = CallKitVoipPlugin.getInstance();
//                if(plugin != null)
//                    plugin.notifyEvent("callAnswered",
//                            request.getExtras().getString("username"),
//                            request.getExtras().getString("connectionId")
//                    );

//                Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.livingservices.app");
//                if (launchIntent != null) {
//                    startActivity(launchIntent);
//                } else {
//                    Log.e(TAG, "onCreateIncomingConnection - onAnswer: intent null");
//                }

        // this.setDisconnected(new DisconnectCause(DisconnectCause.LOCAL));

    }

    @Override
    public void onReject() {
        Log.v(TAG, "onReject");
        DisconnectCause cause = new DisconnectCause(DisconnectCause.REJECTED);
        this.setDisconnected(cause);
        this.destroy();
    }

    @Override
    public void onAbort() {
        Log.v(TAG, "onAbort");
        super.onAbort();
    }

    @Override
    public void onDisconnect() {
        Log.v(TAG, "onDisconnect");
        DisconnectCause cause = new DisconnectCause(DisconnectCause.LOCAL);
        this.setDisconnected(cause);
        this.destroy();
    }
}
