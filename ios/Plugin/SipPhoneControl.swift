import Foundation
import linphonesw

class SipPhoneControl: ObservableObject {
    var mCore: Core!
    var mAccount: Account?
    var mCoreDelegate : CoreDelegate!

    @Published var username : String = ""
    @Published var passwd : String = ""
    @Published var domain : String = ""
    @Published var loggedIn: Bool = false
    @Published var transportType : String = "TCP"

    @Published var currentAccount : String = ""

    // Outgoing call related variables
    @Published var callMsg : String = ""
    @Published var isCallRunning : Bool = false
    @Published var isVideoEnabled : Bool = false
    @Published var canChangeCamera : Bool = false
    @Published var remoteAddress : String = ""

    // Callkit related variables
    @Published var isCallIncoming: Bool = false
    @Published var isCallOutgoing: Bool = false
    @Published var incomingCallName = ""
    @Published var mCall : Call?
    @Published var mProviderDelegate : SipPhoneCallKitProviderDelegate!
    @Published var mCallAlreadyStopped : Bool = false

    // registration tokens
    @Published var tokenVOIP : String = ""
    @Published var tokenPUSH : String = ""

    // event handlers
    @Published var registrationStateListener: (() -> Void)?
    @Published var callStateListener: (() -> Void)?

    init()
    {
        LoggingService.Instance.logLevel = LogLevel.Debug

        let factory = Factory.Instance
        // IMPORTANT : In this tutorial, we require the use of a core configuration file.
        // This way, once the registration is done, and until it is cleared, it will return to the LoggedIn state on launch.
        // This allows us to have a functional call when the app was closed and is started by a VOIP push notification (incoming call
        // We also need to enable "Push Notitifications" and "Background Mode - Voice Over IP"
        let configDir = factory.getConfigDir(context: nil)
        try? mCore = factory.createCore(configPath: "\(configDir)/SipConfig", factoryConfigPath: "", systemContext: nil)

//        mCore.limeX3DhEnabled = false;

        mProviderDelegate = SipPhoneCallKitProviderDelegate(context: self)

        // enabling push notifications management in the core

        /// Special function to enable the callkit.
        mCore.callkitEnabled = false
        /// Enable or disable push notifications on Android & iOS.
        /// If enabled, it will try to get the push token add configure each account with
        /// push_notification_allowed set to true with push parameters. IOS: will also
        /// instanciate a PushRegistry, so make sure that your app does not instanciate one
        /// too or there will be a conflict.
        mCore.pushNotificationEnabled = true

        // call this when pushes are handled manually
//        mCore.processPushNotification(callId: "")

        mCore.pushNotificationConfig?.teamId = "7485TYL3QC";

        NSLog("[SipPhoneControl] init teamId=" + (mCore.pushNotificationConfig?.teamId ?? "nil"))
        NSLog("[SipPhoneControl] init bundleIdentifier=" + ( mCore.pushNotificationConfig?.bundleIdentifier ?? "nil"))
        NSLog("[SipPhoneControl] init remoteToken=" + (mCore.pushNotificationConfig?.remoteToken ?? "nil"))
        NSLog("[SipPhoneControl] init voipToken=" + (mCore.pushNotificationConfig?.voipToken ?? "nil"))

        // Here we enable the video capture & display at Core level
        // It doesn't mean calls will be made with video automatically,
        // But it allows to use it later
//        mCore.videoCaptureEnabled = false
//        mCore.videoDisplayEnabled = true

        // When enabling the video, the remote will either automatically answer the update request
        // or it will ask it's user depending on it's policy.
        // Here we have configured the policy to always automatically accept video requests
//        mCore.videoActivationPolicy!.automaticallyAccept = true

        // If the following property is enabled, it will automatically configure created call params with video enabled
//        mCore.videoActivationPolicy!.automaticallyInitiate = true

        // ???
        // mCore.videoEnabled = true;

        
        mCore.callToneIndicationsEnabled = false; // try this don't crash audio devices when using callkit
        
        NSLog("[SipPhoneControl] init before start")

        try? mCore.start()

        NSLog("[SipPhoneControl] init after start")

        mCoreDelegate = CoreDelegateStub( onCallStateChanged: { (core: Core, call: Call, state: Call.State, message: String) in
            // This function will be called each time a call state changes,
            // which includes new incoming/outgoing calls
            self.callMsg = message

            NSLog("[SipPhoneControl] onCallStateChanged callMsg \(message)  state \(state) remoteContact " + call.remoteContact)

            if (state == .PushIncomingReceived){
                // We're being called by someone (and app is in background)
                self.mCall = call
                self.isCallIncoming = true

                self.incomingCallName = call.remoteAddress?.displayName ?? "Unknown"
                self.remoteAddress = call.remoteAddress?.asStringUriOnly() ?? ""

                self.mProviderDelegate.incomingCall()
            } else if (state == .IncomingReceived) {
                // If app is in foreground, it's likely that we will receive the SIP invite before the Push notification
                if (!self.isCallIncoming) {
                    self.mCall = call
                    self.isCallIncoming = true
                    self.mProviderDelegate.incomingCall()
                }
        
                self.incomingCallName = call.remoteAddress?.displayName ?? "Unknown"
                self.remoteAddress = call.remoteAddress!.asStringUriOnly()
            } else if (state == .OutgoingInit) {
                // When the 200 OK has been received
                self.isCallIncoming = false
                self.isCallOutgoing = true

                // repot outgoing call
                self.mProviderDelegate.outgoingCallStarted()
            } else if (state == .OutgoingRinging) {
                // This state will be reached upon reception of the 180 RINGING
            } else if (state == .Connected) {
                // When the 200 OK has been received
                self.isCallIncoming = false
                self.isCallOutgoing = false
                self.isCallRunning = true

              //  self.mProviderDelegate.stopCall(_callHandledInApp: true)

            } else if (state == .StreamsRunning) {
                // This state indicates the call is active.
                // You may reach this state multiple times, for example after a pause/resume
                // or after the ICE negotiation completes

                // Only enable toggle camera button if there is more than 1 camera
                // We check if core.videoDevicesList.size > 2 because of the fake camera with static image created by our SDK (see below)
                self.canChangeCamera = core.videoDevicesList.count > 2
                
                self.isCallIncoming = false
                self.isCallOutgoing = false
                self.isCallRunning = true

                // report like connected
                if (self.isCallOutgoing) {
                    self.mProviderDelegate.outgoingCallConnected();
                }
            } else if (state == .Paused) {
                // When you put a call in pause, it will became Paused
                self.canChangeCamera = false
            } else if (state == .PausedByRemote) {
                // When the remote end of the call pauses it, it will be PausedByRemote
            } else if (state == .Updating) {
                // When we request a call update, for example when toggling video
            } else if (state == .UpdatedByRemote) {
                // When the remote requests a call update
            } else if (state == .Released || state == .End || state == .Error) {
                // Call has been terminated by any side

                // Report to CallKit that the call is over, if the terminate action was initiated by other end of the call
                // if (self.isCallRunning) {
                self.mProviderDelegate.stopCall(_callHandledInApp: false)
                //}

                self.remoteAddress = ""

                self.isCallRunning = false
                self.isCallOutgoing = false
                self.isCallIncoming = false
            }

            NSLog("[SipPhoneControl] callStateListener")
            // notify changed call state
            if let callback = self.callStateListener {
                NSLog("[SipPhoneControl] callStateListener - callback")
                callback()
            }
        }, onAccountRegistrationStateChanged: { (core: Core, account: Account, state: RegistrationState, message: String) in
            NSLog("[SipPhoneControl] New registration state is \(state) for user id \( String(describing: account.params?.identityAddress?.asString()))\n")


            if (state == .Ok) {
                self.loggedIn = true
                if(self.mAccount != nil) {
                    self.mAccount = account
                }
                self.currentAccount = account.contactAddress?.username ?? ""
            } else if (state == .Cleared) {
                self.loggedIn = false
            }

            if let callback = self.registrationStateListener {
                callback()
            } else {
                NSLog("[SipPhoneControl] onAccountRegistrationStateChanged - callback not found")
                // TODO schedule later
            }
        })

        mCore.addDelegate(delegate: mCoreDelegate)
    }

    func login() throws {
        do {
            if (self.loggedIn) {
                NSLog("[SipPhoneControl] Login - already logged in")
                if let callback = self.registrationStateListener {
                    callback()
                } else {
                    NSLog("[SipPhoneControl] Login - callback not found")
                }
                return
            }

            var transport : TransportType
            if (transportType == "TLS") { transport = TransportType.Tls }
            else if (transportType == "TCP") { transport = TransportType.Tcp }
            else  { transport = TransportType.Udp }

            transport = TransportType.Tcp

            NSLog("[SipPhoneControl] Login \(transport):\\\\\(username)@\(domain)")

            let authInfo = try Factory.Instance.createAuthInfo(username: username, userid: "", passwd: passwd, ha1: "", realm: "", domain: domain)
            let accountParams = try mCore.createAccountParams()
            let identity = try Factory.Instance.createAddress(addr: String("sip:" + username + "@" + domain))
            try! accountParams.setIdentityaddress(newValue: identity)
            let address = try Factory.Instance.createAddress(addr: String("sip:" + domain))
            try address.setTransport(newValue: transport)
            try accountParams.setServeraddress(newValue: address)
            accountParams.registerEnabled = true

            NSLog("[SipPhoneControl] Login teamId=" + (accountParams.pushNotificationConfig?.teamId ?? "nil"))
            NSLog("[SipPhoneControl] Login bundleIdentifier=" + ( accountParams.pushNotificationConfig?.bundleIdentifier ?? "nil"))
            NSLog("[SipPhoneControl] Login remoteToken=" + (accountParams.pushNotificationConfig?.remoteToken ?? "nil"))
            NSLog("[SipPhoneControl] Login voipToken=" + (accountParams.pushNotificationConfig?.voipToken ?? "nil"))
            NSLog("[SipPhoneControl] Login pushNotificationAllowed=" + (accountParams.pushNotificationAllowed ? "Y" : "N" ))
            NSLog("[SipPhoneControl] Login remotePushNotificationAllowed=" + (accountParams.remotePushNotificationAllowed  ? "Y" : "N"))

            tokenPUSH = accountParams.pushNotificationConfig?.remoteToken ?? ""
            tokenVOIP = accountParams.pushNotificationConfig?.voipToken ?? ""

            NSLog("[SipPhoneControl] Login update account params...")
            accountParams.pushNotificationAllowed = false;
            accountParams.remotePushNotificationAllowed = false;

            NSLog("[SipPhoneControl] Login pushNotificationAllowed=" + (accountParams.pushNotificationAllowed ? "Y" : "N" ))
            NSLog("[SipPhoneControl] Login remotePushNotificationAllowed=" + (accountParams.remotePushNotificationAllowed  ? "Y" : "N"))

            // Enable push notifications on this account
            // https://wiki.linphone.org/xwiki/wiki/public/view/Lib/Features/Push%20notifications/IOS%20push%20notification%20management
//            accountParams.pushNotificationAllowed = false /// Indicates whether to add to the contact parameters the push notification information.  For IOS, it indicates for VOIP push notification.
//            accountParams.remotePushNotificationAllowed = false /// Indicates whether to add to the contact parameters the push notification information.
            // NOTE: enable only one to support FreePBX calls (fails due to & mark)


            // We're in a sandbox application, so we must set the provider to "apns.dev" since it will be "apns" by default, which is used only for production apps
//            accountParams.pushNotificationConfig?.provider = "apns.dev";


            NSLog("[SipPhoneControl] Login create account...")

            mAccount = try mCore.createAccount(params: accountParams)
            mCore.addAuthInfo(info: authInfo)
            try mCore.addAccount(account: mAccount!)
            mCore.defaultAccount = mAccount

        } catch {
            NSLog("[SipPhoneControl] Login error \(error.localizedDescription)")
            throw error
        }
    }

    func unregister() throws
    {
        if let account = mCore.defaultAccount {
            NSLog("[SipPhoneControl] unregister: " + (account.contactAddress?.username ?? "<contactAddress>"))
            let params = account.params
            let clonedParams = params?.clone()
            clonedParams?.registerEnabled = false
            account.params = clonedParams
        } else {
            NSLog("[SipPhoneControl] unregister - no default account")
        }
        loggedIn = false
    }

    func delete()  throws {
        if let account = mCore.defaultAccount {
            NSLog("[SipPhoneControl] delete: " + (account.contactAddress?.username ?? "<contactAddress>"))
            mCore.removeAccount(account: account)
        }
        NSLog("[SipPhoneControl] delete - clearAccounts")
        mCore.clearAccounts()
        mCore.clearAllAuthInfo()
        loggedIn = false
    }


    func outgoingCall() throws {
        do {
            // As for everything we need to get the SIP URI of the remote and convert it to an Address
            let remoteAddress = try Factory.Instance.createAddress(addr: remoteAddress)

            // We also need a CallParams object
            // Create call params expects a Call object for incoming calls, but for outgoing we must use null safely
            let params = try mCore.createCallParams(call: nil)

            // We can now configure it
            // Here we ask for no encryption but we could ask for ZRTP/SRTP/DTLS
            params.mediaEncryption = MediaEncryption.None
            // If we wanted to start the call with video directly
            //params.videoEnabled = true

            // Finally we start the call
            let _ = mCore.inviteAddressWithParams(addr: remoteAddress, params: params)
            // Call process can be followed in onCallStateChanged callback from core listener
        } catch {
            throw error
        }

    }

    func terminateCall() {
        NSLog("[SIP] terminateCall - try to remove callkit first")

        self.mProviderDelegate.stopCall(_callHandledInApp: false)

        if (self.isCallRunning || self.isCallIncoming || self.isCallOutgoing) {
            do {
                if (mCore.callsNb == 0) {
                    NSLog("[SIP] terminateCall - no call 000")
                    return
                }

                // If the call state isn't paused, we can get it using core.currentCall
                let coreCall = (mCore.currentCall != nil) ? mCore.currentCall : mCore.calls[0]
                
                if(coreCall == nil) {
                    NSLog("[SIP] terminateCall - no call  nil")
                }
                // Terminating a call is quite simple
                if let call = coreCall {
                    NSLog("[SIP] terminateCall - try")
                    try call.terminate()
                }
                
            
            } catch { NSLog(error.localizedDescription) }
        } else {
            NSLog("[SIP] terminateCall - no call")
        }
    }
    
    func acceptCallWithDelegate() {
        self.mProviderDelegate.acceptCall()
    }
    
    func acceptCall() {
        NSLog("[SIP] acceptCall")
        
        if(mCore.currentCall == nil) {
            NSLog("[SIP] acceptCall there is no call yet in system")
        }
            
//        self.mProviderDelegate.stopCall(_callHandledInApp: true)
        
        do {
            
            for audioDevice in mCore.audioDevices {
                NSLog("[SIP AUDIO] \(audioDevice.deviceName) \(audioDevice.type)"  )
                if (audioDevice.type == AudioDeviceType.Speaker) {
                    NSLog("[SIP AUDIO] Speaker found"  )
                    mCore.currentCall?.outputAudioDevice = audioDevice
                }
            }
            
            mCore.configureAudioSession()
            try mCore.currentCall?.accept()
            
            // mCore.configureAudioSession()
            //            // if we wanted, we could create a CallParams object
            //            // and answer using this object to make changes to the call configuration
            //            // (see OutgoingCall tutorial)
//            try mCore.currentCall?.accept()
            
            
            // Get the currently used audio device
//            let currentAudioDevice = mCore.currentCall?.outputAudioDevice
//            let speakerEnabled = currentAudioDevice?.type == AudioDeviceType.Speaker
            // We can get a list of all available audio devices using
            // Note that on tablets for example, there may be no Earpiece device
//            if(!speakerEnabled) {

                    // For IOS, the Speaker is an exception, Linphone cannot differentiate Input and Output.
                    // This means that the default output device, the earpiece, is paired with the default phone microphone.
                    // Setting the output audio device to the microphone will redirect the sound to the earpiece.
                    //                    if (speakerEnabled && audioDevice.type == AudioDeviceType.Microphone) {
                    //                        mCore.currentCall?.outputAudioDevice = audioDevice
                    //                        isSpeakerEnabled = false
                    //                        return
                    //                    } else if (!speakerEnabled && audioDevice.type == AudioDeviceType.Speaker) {
                    //                        mCore.currentCall?.outputAudioDevice = audioDevice
                    //                        isSpeakerEnabled = true
                    //                        return
                    //                    }
                    /* If we wanted to route the audio to a bluetooth headset
                     else if (audioDevice.type == AudioDevice.Type.Bluetooth) {
                     core.currentCall?.outputAudioDevice = audioDevice
                     }*/
//                }
//            }
            
       
            
        } catch { NSLog(error.localizedDescription) }
    }

    func registerRemotePush(deviceToken: Data) {
        var stringifiedToken = deviceToken.map{String(format: "%02X", $0)}.joined()
        stringifiedToken.append(String(":remote"))
        mCore.didRegisterForRemotePushWithStringifiedToken(deviceTokenStr: stringifiedToken)
        NSLog("[SipPhoneControl] push-token registered \(stringifiedToken)")
    }
}
