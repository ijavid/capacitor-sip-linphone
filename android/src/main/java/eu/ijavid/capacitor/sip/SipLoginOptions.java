package eu.ijavid.capacitor.sip;

import com.getcapacitor.PluginCall;

import org.linphone.core.TransportType;

public class SipLoginOptions {
    /**
     * 'TLS' | 'TCP' | 'UDP',  By default "UDP"
     */
    public TransportType transportType;

    /**
     * SIP domain address
     */
    public String domain;

    /**
     * User login for authentication
     */
    public String username;

    /**
     * User password for authentication
     */
    public String password;

    public SipLoginOptions(PluginCall call) {
        var transport = call.getString("transport", "UDP");
        transportType = switch (transport) {
            case "UDP" -> TransportType.Udp;
            case "TCP" -> TransportType.Tcp;
            case "TLS" -> TransportType.Tls;
            default -> throw new IllegalStateException("Unexpected value: " + transport);
        };
        domain = call.getString("domain");
        username = call.getString("username");
        password = call.getString("password");

    }
}
