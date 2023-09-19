import Foundation
import Capacitor
import AVFoundation
import Combine

enum SipEvent: String {
    case AccountStateChanged = "SIPAccountStateChanged", CallStateChanged = "SIPCallStateChanged"
}

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(SipPhoneControlPlugin)
public class SipPhoneControlPlugin: CAPPlugin {
    private let implementation = SipPhoneControl()
    let session: AVAudioSession = AVAudioSession.sharedInstance()
    var cancellableBag = Set<AnyCancellable>()

    override public func load() {
        let registerStateChangedListener = { [self] in
            NSLog("[SipPhoneControlPlugin] AccountStateChanged isLoggedIn=\(self.implementation.loggedIn)")
            self.notifyListeners(SipEvent.AccountStateChanged.rawValue, data: [
                "isLoggedIn": self.implementation.loggedIn,
                "username": self.implementation.currentAccount,
                "voipToken": self.implementation.tokenVOIP,
                "remoteToken": self.implementation.tokenPUSH,
            ], retainUntilConsumed: true)
        }

        let callStateChangedListener = { [self] in
            NSLog("[SipPhoneControlPlugin] CallStateChanged")
            self.notifyListeners(SipEvent.CallStateChanged.rawValue, data: [
                "isCallRunning": self.implementation.isCallRunning,
                "isCallIncoming": self.implementation.isCallIncoming,
                "isCallOutgoing": self.implementation.isCallOutgoing,
                "remoteAddress": self.implementation.remoteAddress,
                "incomingCallName": self.implementation.incomingCallName,
            ], retainUntilConsumed: true)
        }

//        self.bridge?.notificationRouter.pushNotificationHandler = self.notificationDelegateHandler
//        self.notificationDelegateHandler.plugin = self

        implementation.registrationStateListener = registerStateChangedListener
        implementation.callStateListener = callStateChangedListener

        implementation.$loggedIn.sink { value in
            registerStateChangedListener()
        }.store(in: &cancellableBag)

        implementation.objectWillChange.sink {
            callStateChangedListener()
        }.store(in: &cancellableBag)

        NotificationCenter.default.addObserver(self,
                                               selector: #selector(self.didRegisterForRemoteNotificationsWithDeviceToken(notification:)),
                                               name: .capacitorDidRegisterForRemoteNotifications,
                                               object: nil)

        NotificationCenter.default.addObserver(self,
                                               selector: #selector(self.didFailToRegisterForRemoteNotificationsWithError(notification:)),
                                               name: .capacitorDidFailToRegisterForRemoteNotifications,
                                               object: nil)
    }

  deinit {
      NotificationCenter.default.removeObserver(self)
  }


    @objc override public func checkPermissions(_ call: CAPPluginCall) {
        // TODO
        call.unimplemented("TODO: implement this method")
    }

    @objc override public func requestPermissions(_ call: CAPPluginCall) {
        if (session.responds(to: #selector(AVAudioSession.requestRecordPermission(_:)))) {
            AVAudioSession.sharedInstance().requestRecordPermission({(granted: Bool)-> Void in
                if granted {
                    print("granted")

                    do {
                        try self.session.setCategory(AVAudioSession.Category.playAndRecord)
                        try self.session.setActive(true)
                    }
                    catch {

                        print("Couldn't set Audio session category")
                    }
                } else{
                    print("not granted")
                }
            })
        }
    }

    @objc func initialize(_ call: CAPPluginCall) {
        if let cb = implementation.registrationStateListener {
            cb()
        }
        call.resolve()
//        do {
//            call.resolve()
//        } catch {
//            call.reject(error.localizedDescription)
//        }
    }

    @objc func login(_ call: CAPPluginCall) {
        if let cb = implementation.registrationStateListener {
            cb()
        }

        let transport = call.getString("transport", "UDP")

        let domain = call.getString("domain")
        let username = call.getString("username")
        let password = call.getString("password")

        if (domain == nil || username == nil || password == nil) {
            call.reject("[SipPhoneControl] domain, username, password are required fields!")

            return
        }

        implementation.transportType = transport

        implementation.domain = domain ?? ""
        implementation.username = username  ?? ""
        implementation.passwd = password  ?? ""

        do {
            try implementation.login()
            call.resolve()
        } catch {
            call.reject(error.localizedDescription)
        }
    }

    @objc func logout(_ call: CAPPluginCall) {
        do {
            try implementation.unregister()
            try implementation.delete()
            call.resolve()
        } catch {
            call.reject(error.localizedDescription)
        }
    }

    @objc func call(_ call: CAPPluginCall) {
        let address = call.getString("address") ?? ""

        if (address.isEmpty) {
            call.reject("[SipPhoneControl] address is required")

            return
        }

        implementation.remoteAddress = address

        do {
            try implementation.outgoingCall()
        } catch {
            call.reject(error.localizedDescription)
        }
    }

    @objc func acceptCall(_ call: CAPPluginCall) {
        implementation.acceptCallWithDelegate()
    }

    @objc func hangUp(_ call: CAPPluginCall) {
        implementation.terminateCall()
    }

    @objc public func didRegisterForRemoteNotificationsWithDeviceToken(notification: NSNotification) {
          if let deviceToken = notification.object as? Data {
              NSLog("[SIP PLUGIN] didRegisterForRemoteNotificationsWithDeviceToken - deviceToken")
              implementation.registerRemotePush(deviceToken: deviceToken)
              let deviceTokenString = deviceToken.reduce("", {$0 + String(format: "%02X", $1)})
              notifyListeners("registration", data: [
                  "value": deviceTokenString
              ])
          } else if let stringToken = notification.object as? String {
              NSLog("[SIP PLUGIN] didRegisterForRemoteNotificationsWithDeviceToken - String")
              implementation.mCore.didRegisterForRemotePushWithStringifiedToken(deviceTokenStr: stringToken)
              notifyListeners("registration", data: [
                  "value": stringToken
              ])
          } else {
              NSLog("[SIP PLUGIN] didRegisterForRemoteNotificationsWithDeviceToken - error")
              notifyListeners("registrationError", data: [
                  "error": "error ????"
              ])
          }

      }

      @objc public func didFailToRegisterForRemoteNotificationsWithError(notification: NSNotification) {

          NSLog("[SIP PLUGIN] didFailToRegisterForRemoteNotificationsWithError - error")

          guard let error = notification.object as? Error else {
              return
          }
          notifyListeners("registrationError", data: [
              "error": error.localizedDescription
          ])
      }
}


//    private val coreListener = object: CoreListenerStub() {
//        override fun onAccountRegistrationStateChanged(core: Core, account: Account, state: RegistrationState?, message: String) {
//            findViewById<TextView>(R.id.registration_status).text = message
//
//            if (state == RegistrationState.Failed) {
//                findViewById<Button>(R.id.connect).isEnabled = true
//            } else if (state == RegistrationState.Ok) {
//                findViewById<LinearLayout>(R.id.register_layout).visibility =
//                    View.GONE
//
//                // This will display the push information stored in the contact URI parameters
//                findViewById<TextView>(R.id.push_info).text = account.params.contactUriParameters
//            }
//        }
//    }
