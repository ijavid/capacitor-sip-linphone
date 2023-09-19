package eu.ijavid.capacitor.sip;

import static eu.ijavid.capacitor.sip.Constants.EXTRA_CALLER_NAME;
import static eu.ijavid.capacitor.sip.Constants.EXTRA_CALL_UUID;
import static eu.ijavid.capacitor.sip.Constants.EXTRA_HAS_VIDEO;
import static eu.ijavid.capacitor.sip.Constants.EXTRA_PAYLOAD;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telecom.DisconnectCause;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.util.Log;

import androidx.annotation.Nullable;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;

import java.util.HashMap;
import java.util.Map;

@CapacitorPlugin(name = "SipPhoneControl", permissions = {@Permission(strings = {Manifest.permission.READ_PHONE_STATE}),})
public class SipPhoneControlPlugin extends Plugin implements SipPhoneEventListener {

    static SipPhoneControlPlugin instance;

    private static final String TAG = "SipPhoneControlPlugin";

    protected SipPhoneControl implementation = null;
//    private CordovaCall cordovaCall = new CordovaCall();

    public static HashMap<String, String> getSettings(Context context) {
        HashMap<String, String> config = new HashMap<>();
        config.put("channelId", "incoming-call-channel-id");
        config.put("channelName", "incoming-call-channel-name");
        config.put("notificationTitle", "notificationTitle");
        config.put("notificationIcon", "ic_launcher");
//        config.put("displayCallReachabilityTimeout", "60000"); // ms = 1 min
        return config;
    }

    public static HashMap<String, String> getForegroundSettings(Context context) {
        HashMap<String, String> config = new HashMap<>();
        config.put("channelId", "incoming-call-channel-id");
        config.put("channelName", "incoming-call-channel-name");
        config.put("notificationTitle", "notificationTitle");
        config.put("notificationIcon", "ic_launcher");
//        config.put("displayCallReachabilityTimeout", "60000"); // ms = 1 min
        return config;
    }

    public void onAccountStateChanged() {
        JSObject ret = new JSObject();
        ret.put("isLoggedIn", implementation.isLoggedIn);
        notifyListeners("SIPAccountStateChanged", ret, true);
    }


    public void onCallStateChanged() {
        var remote = implementation.getCurrentCallRemoteAddress();
        if (implementation.isCallIncoming && remote != null) {
//            try {
//                CallKit.reportIncomingCall(remote.uri, false);
//            } catch (JSONException e) {
//                throw new RuntimeException(e);
//            }
//            startIncomingCall(remote.uri);
//            displayIncomingCall(remote.uri, remote.uri, remote.uri, false, null);
//            cordovaCall.from = remote.uri;
//            cordovaCall.receiveCall();
            // TODO do nothing for now
        }

        if (implementation.isCallRunning) {
            XXXXMyConnectionService.clear();
        }
        if (!implementation.isCallRunning && !implementation.isCallIncoming) {
            XXXXMyConnectionService.clear();
        }

        JSObject ret = new JSObject();
        ret.put("isCallRunning", implementation.isCallRunning);
        ret.put("isCallIncoming", implementation.isCallIncoming);
        ret.put("isCallOutgoing", implementation.isCallOutgoing);
        if (remote != null) {
            ret.put("remoteAddress", remote.uri);
//            ret.put("remoteUsername", remote.username);
            ret.put("incomingCallName", remote.displayName);
        }
        notifyListeners("SIPCallStateChanged", ret);
    }


    public void onSipPushReceived(Context context, Intent intent) {
        // A push have been received but there was no Core alive, you should create it again
        // This way the core will register and it will handle the message or call event like if the app was started
        Log.v(TAG, "onSipPushReceived: " + context);
        if (implementation == null) {
            Log.w(TAG, "onSipPushReceived: missing plugin control");
            implementation = new SipPhoneControl(getContext());
            implementation.addListener(this);
        }
        implementation.start();
    }

    @Override
    public void load() {
        Log.v(TAG, "load");
        implementation = new SipPhoneControl(getContext());
        implementation.addListener(this);
        implementation.start();

        PushBroadcastReceiver receiver = new PushBroadcastReceiver();
        receiver.addListener(this);
        IntentFilter intentFilter = new IntentFilter("org.linphone.core.action.PUSH_RECEIVED");
//        CallKit.initialize(this);
//        try {
//            CallKit.register();
//        } catch (JSONException e) {
//            throw new RuntimeException(e);
//        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getActivity().registerReceiver(receiver, intentFilter, Context.RECEIVER_EXPORTED);
        } else {
            getActivity().registerReceiver(receiver, intentFilter);
        }

        SipPhoneControlPlugin.instance = this;

//        cordovaCall.initialize(this);
//        cordovaCall.checkCallPermission();
//        cordovaCall.from = "TEST";
//        cordovaCall.receiveCall();
    }

    @PluginMethod
    @Permission(strings = {Manifest.permission.READ_PHONE_STATE})
    public void initialize(PluginCall call) {
        Log.v(TAG, "initialize");
        requestAllPermissions(call, "initializeDone");
        //        implementation = new SipPhoneControl(getContext());
        //        implementation.addListener(this);
//        call.resolve();
    }

    @PermissionCallback
    public void initializeDone(PluginCall call) {

//        cordovaCall.checkCallPermission();

        Log.v(TAG, "initializeDone");
//        JSObject ret = new JSObject();
//        notifyListeners("SIPCallInitialised", ret);
        call.resolve();
    }

    @PluginMethod
    public void checkCallPermission(PluginCall call) {
        Log.v(TAG, "checkCallPermission");
//        cordovaCall.checkCallPermission();
        call.resolve();
    }

    /**
     * Make login to the SIP
     */
    @PluginMethod
    public void login(PluginCall call) {
        Log.v(TAG, "login");
        var sipOptions = new SipLoginOptions(call);
        implementation.login(sipOptions);
        call.resolve();
    }

    /**
     * Logout & terminate account
     */
    @PluginMethod
    public void logout(PluginCall call) {
        Log.v(TAG, "logout");
        implementation.logout();
        call.resolve();
    }

    /**
     * Make outgoing call
     */
    @PluginMethod
    public void call(PluginCall call) {
        Log.v(TAG, "call");
        var address = call.getString("address");
        Log.v(TAG, "remoteAddress=" + address);
        call.unimplemented("Not implemented.");
    }

    /**
     * Accept incoming call
     */
    @PluginMethod
    public void acceptCall(PluginCall call) {
        Log.v(TAG, "acceptCall");
        try {
            implementation.acceptCurrentCall();
        } catch (Exception err) {
            call.reject(err.getMessage());
            return;
        }
        call.resolve();
    }

    /**
     * Terminate current call
     */
    @PluginMethod
    public void hangUp(PluginCall call) {
        Log.v(TAG, "hangUp");
        try {
            implementation.terminateCurrentCall();
        } catch (Exception err) {
            call.reject(err.getMessage());
            return;
        }
        call.resolve();
    }

    /**
     * Terminate current call
     */
    @PluginMethod
    public void toggleSpeaker(PluginCall call) {
        Log.v(TAG, "toggleSpeaker");
        try {
            implementation.toggleSpeaker();
        } catch (Exception err) {
            call.reject(err.getMessage());
            return;
        }
        call.resolve();
    }


    private void displayIncomingCall(String uuid, String number, String callerName, boolean hasVideo, @Nullable Bundle payload) {
//        if (!isConnectionServiceAvailable() || !hasPhoneAccount()) {
//            Log.w(TAG, "[RNCallKeepModule] displayIncomingCall ignored due to no ConnectionService or no phone account");
//            return;
//        }

        Context context = getActivity().getApplicationContext();
        if (context == null) {
            Log.w(TAG, "[RNCallKeepModule][initializeTelecomManager] no react context found.");
            return;
        }
        ComponentName cName = new ComponentName(context, SipPhoneConnectionService.class);
        String appName = getActivity().getPackageName();

        PhoneAccountHandle handle = new PhoneAccountHandle(cName, appName);
        TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);

        PhoneAccount.Builder builder = new PhoneAccount.Builder(handle, appName);
        builder.setCapabilities(PhoneAccount.CAPABILITY_SELF_MANAGED);
        PhoneAccount account = builder.build();
        telecomManager.registerPhoneAccount(account);

        Log.d(TAG, "[RNCallKeepModule] displayIncomingCall, uuid: " + uuid + ", number: " + number + ", callerName: " + callerName + ", hasVideo: " + hasVideo + ", payload: " + payload);

        Bundle extras = new Bundle();
//        Uri uri = Uri.fromParts(PhoneAccount.SCHEME_TEL, number, null);
        Uri uri = Uri.parse(number);

        extras.putParcelable(TelecomManager.EXTRA_INCOMING_CALL_ADDRESS, uri);
        extras.putString(EXTRA_CALLER_NAME, callerName);
        extras.putString(EXTRA_CALL_UUID, uuid);
        extras.putString(EXTRA_HAS_VIDEO, String.valueOf(hasVideo));
        if (payload != null) {
            extras.putBundle(EXTRA_PAYLOAD, payload);
        }


        telecomManager.addNewIncomingCall(handle, extras);
    }

    @SuppressLint("MissingPermission")
    private void startIncomingCall(String caller) {
        Log.v(TAG, "startCall");
        var id = "000"; // 002, 001, 999999999, 9999999991, 9999999951
        var label = "000";

        TelecomManager telecomManager = (TelecomManager) getActivity().getApplicationContext().getSystemService(Context.TELECOM_SERVICE);
        PhoneAccountHandle phoneAccountHandle = new PhoneAccountHandle(new ComponentName(getActivity().getApplicationContext(), SipPhoneConnectionService.class), id);


        PhoneAccount phoneAccount = PhoneAccount.builder(phoneAccountHandle, label)
//                .setCapabilities(PhoneAccount.CAPABILITY_CALL_PROVIDER)
                .setCapabilities(PhoneAccount.CAPABILITY_SELF_MANAGED)
                .build();
        telecomManager.registerPhoneAccount(phoneAccount);

        var a = telecomManager.isIncomingCallPermitted(phoneAccountHandle);
        var b = telecomManager.isOutgoingCallPermitted(phoneAccountHandle);
        var c = telecomManager.isInCall();
        Log.v(TAG, "start call " + a + " " + b + " " + c);

//        var x = telecomManager.getOwnSelfManagedPhoneAccounts();
//        for (var xx: x) {
//            Log.v(TAG, xx.getId());
//        }

        Bundle extras = new Bundle();
        var uri = Uri.parse(caller);
        extras.putParcelable(TelecomManager.EXTRA_INCOMING_CALL_ADDRESS, uri);
        extras.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, phoneAccountHandle);
        Log.v(TAG, "startCall addNewIncomingCall...");

        telecomManager.addNewIncomingCall(phoneAccountHandle, extras);
        Log.v(TAG, "startCall done");
    }

}
