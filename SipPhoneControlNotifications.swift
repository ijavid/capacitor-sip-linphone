extension Notification.Name {
    public static let sipPhoneControlDidReceiveRemoteNotification = Notification.Name(rawValue: "SipPhoneControlDidReceiveRemoteNotification")
}

@objc extension NSNotification {
    public static let sipPhoneControlDidReceiveRemoteNotification = Notification.Name.sipPhoneControlDidReceiveRemoteNotification
}
