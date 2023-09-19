package eu.ijavid.capacitor.sip;

import android.content.Intent;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telecom.Connection;
import android.telecom.ConnectionRequest;
import android.telecom.ConnectionService;
import android.telecom.DisconnectCause;
import android.telecom.PhoneAccountHandle;
import android.telecom.StatusHints;
import android.telecom.TelecomManager;


public class XXXXMyConnectionService extends ConnectionService {

    private static final String TAG = "MyConnectionService";
    private static Connection conn;

    public static Connection getConnection() {
        return conn;
    }


    public static void clear() {
        if (conn == null) {
            return;
        }
        DisconnectCause cause = new DisconnectCause(DisconnectCause.OTHER);
        conn.setDisconnected(cause);
        conn.destroy();
        conn.onAbort();
        conn.destroy();
        conn = null;
    }

    @Override
    public Connection onCreateIncomingConnection(final PhoneAccountHandle connectionManagerPhoneAccount, final ConnectionRequest request) {
        final Connection connection = new Connection() {
            @Override
            public void onAnswer() {
                XXXXMyConnectionService.clear();
                CordovaCall.getCapacitorPlugin().implementation.acceptCurrentCall();

                // TODO if background ->
//                Intent intent = new Intent(CordovaCall.getCapacitorPlugin().getActivity().getApplicationContext(), CordovaCall.getCapacitorPlugin().getActivity().getClass());
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                CordovaCall.getCapacitorPlugin().getActivity().getApplicationContext().startActivity(intent);

//                ArrayList<CallbackContext> callbackContexts = CordovaCall.getCallbackContexts().get("answer");
//                for (final CallbackContext callbackContext : callbackContexts) {
//                    CordovaCall.getCordova().getThreadPool().execute(new Runnable() {
//                        public void run() {
//                            PluginResult result = new PluginResult(PluginResult.Status.OK, "answer event called successfully");
//                            result.setKeepCallback(true);
//                            callbackContext.sendPluginResult(result);
//                        }
//                    });
//                }


            }

            @Override
            public void onReject() {
                DisconnectCause cause = new DisconnectCause(DisconnectCause.REJECTED);
                this.setDisconnected(cause);
                this.destroy();
                conn = null;
//                ArrayList<CallbackContext> callbackContexts = CordovaCall.getCallbackContexts().get("reject");
//                for (final CallbackContext callbackContext : callbackContexts) {
//                    CordovaCall.getCordova().getThreadPool().execute(new Runnable() {
//                        public void run() {
//                            PluginResult result = new PluginResult(PluginResult.Status.OK, "reject event called successfully");
//                            result.setKeepCallback(true);
//                            callbackContext.sendPluginResult(result);
//                        }
//                    });
//                }

                CordovaCall.getCapacitorPlugin().implementation.terminateCurrentCall();
            }

            @Override
            public void onAbort() {
                super.onAbort();
            }

            @Override
            public void onDisconnect() {
                DisconnectCause cause = new DisconnectCause(DisconnectCause.LOCAL);
                this.setDisconnected(cause);
                this.destroy();
                conn = null;
//                ArrayList<CallbackContext> callbackContexts = CordovaCall.getCallbackContexts().get("hangup");
//                for (final CallbackContext callbackContext : callbackContexts) {
//                    CordovaCall.getCordova().getThreadPool().execute(new Runnable() {
//                        public void run() {
//                            PluginResult result = new PluginResult(PluginResult.Status.OK, "hangup event called successfully");
//                            result.setKeepCallback(true);
//                            callbackContext.sendPluginResult(result);
//                        }
//                    });
//                }
            }
        };
        connection.setAddress(Uri.parse(request.getExtras().getString("from")), TelecomManager.PRESENTATION_ALLOWED);
        Icon icon = CordovaCall.getIcon();
        if (icon != null) {
            StatusHints statusHints = new StatusHints((CharSequence) "", icon, new Bundle());
            connection.setStatusHints(statusHints);
        }
        conn = connection;
//        ArrayList<CallbackContext> callbackContexts = CordovaCall.getCallbackContexts().get("receiveCall");
//        for (final CallbackContext callbackContext : callbackContexts) {
//            CordovaCall.getCordova().getThreadPool().execute(new Runnable() {
//                public void run() {
//                    PluginResult result = new PluginResult(PluginResult.Status.OK, "receiveCall event called successfully");
//                    result.setKeepCallback(true);
//                    callbackContext.sendPluginResult(result);
//                }
//            });
//        }
        return connection;
    }

    @Override
    public Connection onCreateOutgoingConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        final Connection connection = new Connection() {
            @Override
            public void onAnswer() {
                super.onAnswer();
            }

            @Override
            public void onReject() {
                super.onReject();
            }

            @Override
            public void onAbort() {
                super.onAbort();
            }

            @Override
            public void onDisconnect() {
                DisconnectCause cause = new DisconnectCause(DisconnectCause.LOCAL);
                this.setDisconnected(cause);
                this.destroy();
                conn = null;
//                ArrayList<CallbackContext> callbackContexts = CordovaCall.getCallbackContexts().get("hangup");
//                for (final CallbackContext callbackContext : callbackContexts) {
//                    CordovaCall.getCordova().getThreadPool().execute(new Runnable() {
//                        public void run() {
//                            PluginResult result = new PluginResult(PluginResult.Status.OK, "hangup event called successfully");
//                            result.setKeepCallback(true);
//                            callbackContext.sendPluginResult(result);
//                        }
//                    });
//                }
            }

            @Override
            public void onStateChanged(int state) {
                if (state == Connection.STATE_DIALING) {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(CordovaCall.getCapacitorPlugin().getActivity().getApplicationContext(), CordovaCall.getCapacitorPlugin().getActivity().getClass());
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            CordovaCall.getCapacitorPlugin().getActivity().getApplicationContext().startActivity(intent);
                        }
                    }, 500);
                }
            }
        };
        connection.setAddress(Uri.parse(request.getExtras().getString("to")), TelecomManager.PRESENTATION_ALLOWED);
        Icon icon = CordovaCall.getIcon();
        if (icon != null) {
            StatusHints statusHints = new StatusHints((CharSequence) "", icon, new Bundle());
            connection.setStatusHints(statusHints);
        }
        connection.setDialing();
        conn = connection;
//        ArrayList<CallbackContext> callbackContexts = CordovaCall.getCallbackContexts().get("sendCall");
//        if(callbackContexts != null) {
//            for (final CallbackContext callbackContext : callbackContexts) {
//                CordovaCall.getCordova().getThreadPool().execute(new Runnable() {
//                    public void run() {
//                        PluginResult result = new PluginResult(PluginResult.Status.OK, "sendCall event called successfully");
//                        result.setKeepCallback(true);
//                        callbackContext.sendPluginResult(result);
//                    }
//                });
//            }
//        }
        return connection;
    }
}
