package eu.ijavid.capacitor.sip;

import android.content.Context;
import android.content.Intent;

public interface SipPhoneEventListener {
    void onAccountStateChanged();
    void onCallStateChanged();

    void onSipPushReceived(Context context, Intent intent);
}
