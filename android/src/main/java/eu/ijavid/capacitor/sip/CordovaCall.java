package eu.ijavid.capacitor.sip;

import static android.content.Context.TELECOM_SERVICE;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Icon;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.getcapacitor.Plugin;

public class CordovaCall {

    public static final int CALL_PHONE_REQ_CODE = 0;
    public static final int REAL_PHONE_CALL = 1;
    private static final String TAG = "CordovaCall";
    private static Icon icon;
    private static SipPhoneControlPlugin plugin;
    protected String from;
    protected int permissionCounter = 0;
    private String pendingAction;
    private TelecomManager tm;
    private PhoneAccountHandle handle;
    private PhoneAccount phoneAccount;
    private String appName;
    private String to;
    private String realCallTo;

    public static SipPhoneControlPlugin getCapacitorPlugin() {
        return plugin;
    }


    public static Icon getIcon() {
        return icon;
    }

    public static String getApplicationName(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }

    public void initialize(SipPhoneControlPlugin plugin) {
        CordovaCall.plugin = plugin;
        tm = (TelecomManager) plugin.getActivity().getApplicationContext().getSystemService(TELECOM_SERVICE);
        appName = getApplicationName(plugin.getActivity().getApplicationContext());

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            var a = tm.getOwnSelfManagedPhoneAccounts();
            Log.e(TAG, "getOwnSelfManagedPhoneAccounts XX=" + a.size());

            for (var b : a) {
                Log.e(TAG, "getOwnSelfManagedPhoneAccounts XX= " + b.getId() + " " + b.getUserHandle().toString() + " " +  b.getComponentName());
                tm.unregisterPhoneAccount(b);
            }
        }

        var phoneAccountId = "app.livingservices.hu";
        var phoneAccountName = "LIVING App Intercom";

        handle = new PhoneAccountHandle(new ComponentName(plugin.getActivity().getApplicationContext(), XXXXMyConnectionService.class), phoneAccountId);
//        if (android.os.Build.VERSION.SDK_INT >= 26) {
        phoneAccount = new PhoneAccount.Builder(handle, phoneAccountName)
//                .setCapabilities(PhoneAccount.CAPABILITY_SELF_MANAGED)
                .setCapabilities(PhoneAccount.CAPABILITY_CALL_PROVIDER)
                .build();

        tm.registerPhoneAccount(phoneAccount);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            var a = tm.getOwnSelfManagedPhoneAccounts();
            Log.e(TAG, "getOwnSelfManagedPhoneAccounts NN=" + a.size());

            for (var b : a) {
                Log.e(TAG, "getOwnSelfManagedPhoneAccounts NN = " + b.getId() + " " + b.getUserHandle().toString() + " " +  b.getComponentName());
            }
        }



//        var a = tm.getPhoneAccount(handle);
//        Log.e(TAG, "" + a.toString() + " " + a.getCapabilities());

//        }
//        if (android.os.Build.VERSION.SDK_INT >= 23) {
//            phoneAccount = new PhoneAccount.Builder(handle, appName  + ":ABCD1234")
//                    .setCapabilities(PhoneAccount.CAPABILITY_CALL_PROVIDER)
//                    .build();
//            tm.registerPhoneAccount(phoneAccount);
//        }

    }

    public void onResume(boolean multitasking) {
        this.checkCallPermission();
    }

    public void checkCallPermission() {


        var self = phoneAccount.hasCapabilities(PhoneAccount.CAPABILITY_SELF_MANAGED) ? "CAPABILITY_SELF_MANAGED" : "";
        var prov = phoneAccount.hasCapabilities(PhoneAccount.CAPABILITY_CALL_PROVIDER) ? "CAPABILITY_CALL_PROVIDER" : "";

        Log.e(TAG, "checkCallPermission phoneaccount: " + phoneAccount.getLabel() + " " + phoneAccount.getAccountHandle().getId() + " " + self + " " + prov + " " + (phoneAccount.isEnabled() ? "enabled" : "disabled" ));

        if(phoneAccount.isEnabled()) {
            Log.e(TAG, "checkCallPermission phoneaccount is enabled");
        } else {
            Log.e(TAG, "checkCallPermission phoneaccount is NOT enabled");
            Intent phoneIntent = new Intent(TelecomManager.ACTION_CHANGE_PHONE_ACCOUNTS);
            phoneIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            plugin.getActivity().getApplicationContext().startActivity(phoneIntent);
        }



//        if (permissionCounter >= 1) {
//            PhoneAccount currentPhoneAccount = tm.getPhoneAccount(handle);
//            if (currentPhoneAccount.isEnabled()) {
//                if (pendingAction == "receiveCall") {
//                    this.receiveCall();
//                } else if (pendingAction == "sendCall") {
//                    this.sendCall();
//                }
//            } else {
//                if (permissionCounter == 2) {
//                    Intent phoneIntent = new Intent(TelecomManager.ACTION_CHANGE_PHONE_ACCOUNTS);
//                    phoneIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                    plugin.getActivity().getApplicationContext().startActivity(phoneIntent);
//                } else {
//                    Log.e(TAG, "You need to accept phone account permissions in order to send and receive calls");
//                }
//            }
//        }
//        permissionCounter--;
    }

    public void receiveCall() {
        Bundle callInfo = new Bundle();
        callInfo.putString("from", from);
        tm.addNewIncomingCall(handle, callInfo);
        permissionCounter = 0;
        Log.i(TAG, "Incoming call successful");
    }

    public void sendCall() {
        Uri uri = Uri.fromParts("tel", to, null);
        Bundle callInfoBundle = new Bundle();
        callInfoBundle.putString("to", to);
        Bundle callInfo = new Bundle();
        callInfo.putParcelable(TelecomManager.EXTRA_OUTGOING_CALL_EXTRAS, callInfoBundle);
        callInfo.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, handle);
        callInfo.putBoolean(TelecomManager.EXTRA_START_CALL_WITH_VIDEO_STATE, true);
        if (ActivityCompat.checkSelfPermission(plugin.getContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.e(TAG, "Outgoing call checkSelfPermission FAILED");
            return;
        }
        tm.placeCall(uri, callInfo);
        permissionCounter = 0;
        Log.i(TAG, "Outgoing call successful");
    }

    public void mute() {
        AudioManager audioManager = (AudioManager) plugin.getActivity().getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMicrophoneMute(true);
    }

    public void unmute() {
        AudioManager audioManager = (AudioManager) plugin.getActivity().getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMicrophoneMute(false);
    }

    public void speakerOn() {
        AudioManager audioManager = (AudioManager) plugin.getActivity().getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        audioManager.setSpeakerphoneOn(true);
    }

    public void speakerOff() {
        AudioManager audioManager = (AudioManager) plugin.getActivity().getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        audioManager.setSpeakerphoneOn(false);
    }

    public void callNumber() {
        try {
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.fromParts("tel", realCallTo, null));
            plugin.getActivity().getApplicationContext().startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Call Failed");
        }
        Log.i(TAG, "Call Successful");
    }


}
