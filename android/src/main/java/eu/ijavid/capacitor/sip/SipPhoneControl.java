package eu.ijavid.capacitor.sip;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.collection.ArraySet;

import org.linphone.core.Account;
import org.linphone.core.AccountParams;
import org.linphone.core.Address;
import org.linphone.core.AudioDevice;
import org.linphone.core.Call;
import org.linphone.core.Core;
import org.linphone.core.CoreListener;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.Factory;
import org.linphone.core.RegistrationState;


public class SipPhoneControl {

    private static final String TAG = "SipPhoneControl";
    private final Context context;
    private final ArraySet<SipPhoneEventListener> listeners = new ArraySet<>();

    public boolean isLoggedIn = false;
    public String loginMessage = "";
    public boolean isCallRunning = false;
    public boolean isCallIncoming = false;
    public boolean isCallOutgoing = false;
    private final CoreListener coreListener = new CoreListenerStub() {
        @Override
        public void onAccountRegistrationStateChanged(@NonNull Core core, @NonNull Account account, RegistrationState state, @NonNull String message) {
            Log.i(TAG, "onAccountRegistrationStateChanged: state=" + state.toString() + " (" + message + ")");
            isLoggedIn = state == RegistrationState.Ok;
            loginMessage = state + " (" + message + ")";

            // DEBUG
            Log.v(TAG, "onAccountRegistrationStateChanged: isPushNotificationAvailable " + core.isPushNotificationAvailable());

            for (var listener : listeners) {
                listener.onAccountStateChanged();
            }
        }

        @Override
        public void onAudioDeviceChanged(@NonNull Core core, @NonNull AudioDevice audioDevice) {
            // This callback will be triggered when a successful audio device has been changed
            Log.i(TAG, "onAudioDeviceChanged: " + audioDevice);
        }

        @Override
        public void onAudioDevicesListUpdated(@NonNull Core core) {
            // This callback will be triggered when the available devices list has changed,
            // for example after a bluetooth headset has been connected/disconnected.
            Log.i(TAG, "onAudioDevicesListUpdated");
        }

        @Override
        public void onCallStateChanged(@NonNull Core core, Call call, Call.State state, @NonNull String message) {
            Log.i(TAG, "onCallStateChanged: " + call.getRemoteContact() + " state=" + state.toString() + " (" + message + ")");
            isCallRunning = false;
            isCallOutgoing = false;
            isCallIncoming = false;
            switch (state) {
                case Connected, StreamsRunning -> isCallRunning = true;
                case PushIncomingReceived, IncomingReceived -> isCallIncoming = true;
                case OutgoingInit, OutgoingProgress, OutgoingRinging -> isCallOutgoing = true;
            }
            for (var listener : listeners) {
                listener.onCallStateChanged();
            }
        }
    };
    private SipLoginOptions options;
    private Core core;

    public SipPhoneControl(Context context) {
        this.context = context;
    }

    public void login(SipLoginOptions options) {
        this.options = options;
        initCore();
        setSipLoginOptions();
        core.start();

        if (!core.isPushNotificationAvailable()) {
            // TODO ???
            Log.w(TAG, "linPhoneLogin: isPushNotificationAvailable IS FALSE ");
        }

        Log.i(TAG, "linPhoneLogin done.");
    }

    public void start() {
        if (core == null) {
            initCore();
        }
        core.start();
    }

    public void logout() {
//        core.stop(); // ???
        unregister();
        delete();
    }

    private void unregister() {
        if(core == null) {
            initCore();
        }

        // Here we will disable the registration of our Account
        Account account = core.getDefaultAccount();
        if (account == null) {
            Log.w(TAG, "unregister failed no default account");
            return;
        }

        AccountParams params = account.getParams();
        // Returned params object is const, so to make changes we first need to clone it
        AccountParams clonedParams = params.clone();

        // Now let's make our changes
        clonedParams.setRegisterEnabled(false);

        // And apply them
        account.setParams(clonedParams);
    }

    private void delete() {
        // To completely remove an Account
        Account account = core.getDefaultAccount();
        if (account == null) {
            Log.w(TAG, "delete - no default account");
            return;
        } else {
            core.removeAccount(account);
        }

        // To remove all accounts use
        core.clearAccounts();
        // Same for auth info
        core.clearAllAuthInfo();
    }

    private void initCore() {
        Factory.instance().setDebugMode(true, "LinPhoneCapPlugin");

//        var configPath = context.getFilesDir().getAbsolutePath() + "/.linphonerc";
        var configPath = context.getFilesDir().getAbsolutePath() + "/LinPhone-core-config";

        core = Factory.instance().createCore(configPath, null, context);
        core.addListener(coreListener);

        // For push notifications to work, you have to copy your google-services.json in the app/ folder
        // And you must declare our FirebaseMessaging service in the Manifest
        // You also have to make some changes in your build.gradle files, see the ones in this project
        // Make sure the core is configured to use push notification token from firebase
        core.setPushNotificationEnabled(true);
    }

    private void setSipLoginOptions() {
        var authInfo = Factory.instance().createAuthInfo(options.username, null, options.password, null, null, options.domain, null);

        var params = core.createAccountParams();
        var identity = Factory.instance().createAddress("sip:" + options.username + "@" + options.domain);
        params.setIdentityAddress(identity);

        var address = Factory.instance().createAddress("sip:" + options.domain);
        address.setTransport(options.transportType);

        params.setServerAddress(address);
        params.setRegisterEnabled(true);
        // Ensure push notification is enabled for this account
        params.setPushNotificationAllowed(true);

        core.addAuthInfo(authInfo);
        var account = core.createAccount(params);
        core.addAccount(account);
        core.setDefaultAccount(account);
    }


    public void toggleSpeaker() {
        // Get the currently used audio device
        var currentCall = core.getCurrentCall();
        assert currentCall != null;
        var audioDevice = currentCall.getOutputAudioDevice();
        assert audioDevice != null;
        var speakerEnabled = audioDevice.getType() == AudioDevice.Type.Speaker;

        Log.i(TAG, "toggleSpeaker audioDevice=" + audioDevice.getDeviceName() + " type=" + audioDevice.getType().name());

        // We can get a list of all available audio devices using
        // Note that on tablets for example, there may be no Earpiece device

        for (var audioDev: core.getAudioDevices()) {
            if (speakerEnabled && audioDevice.getType() == AudioDevice.Type.Earpiece) {
                currentCall.setOutputAudioDevice(audioDev);
                return;
            } else if (!speakerEnabled && audioDevice.getType() == AudioDevice.Type.Speaker) {
                currentCall.setOutputAudioDevice(audioDev);
                return;
            }/* If we wanted to route the audio to a bluetooth headset
            else if (audioDevice.type == AudioDevice.Type.Bluetooth) {
                core.currentCall?.outputAudioDevice = audioDevice
            }*/
        }
    }

    public void acceptCurrentCall() {
        var call = core.getCurrentCall();
        if (call == null) {
            Log.e(TAG, "acceptCurrentCall: no call");
            throw new RuntimeException("No call");
        }
        Log.i(TAG, "acceptCurrentCall: call state=" + call.getState());
        try {
            call.accept();
        } catch (Exception err) {
            Log.e(TAG, "acceptCurrentCall: " + err.getMessage(), err);
            throw err;
        }
    }

    public void terminateCurrentCall() {
        var call = core.getCurrentCall();
        if (call == null) {
            Log.e(TAG, "terminateCurrentCall: no call");
            throw new RuntimeException("No call");
        }
        Log.i(TAG, "terminateCurrentCall: call state=" + call.getState());
        try {
            call.terminate();
        } catch (Exception err) {
            Log.e(TAG, "terminateCurrentCall: " + err.getMessage(), err);
            throw err;
        }
    }

    public void addListener(SipPhoneEventListener listener) {
        this.listeners.add(listener);
    }

    public SipAddress getCurrentCallRemoteAddress() {
        var remoteAddress = core.getCurrentCallRemoteAddress();
        if (remoteAddress != null) {
            Log.v(TAG, "getCurrentCallRemoteAddress: " + remoteAddress.asString());
            return new SipAddress(remoteAddress);
        }
        Log.v(TAG, "getCurrentCallRemoteAddress: no active call");
        return null;
    }

    static public class SipAddress {
        public String uri;
        public String displayName;
        public String username;

        SipAddress(Address remoteAddress) {
            displayName = remoteAddress.getDisplayName();
            uri = remoteAddress.asStringUriOnly();
            username = remoteAddress.getUsername();
        }
    }
}




