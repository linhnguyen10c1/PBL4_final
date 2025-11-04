// client/ui/interfaces/LoginViewCallbacks.java
package client.ui.auth.interfaces;

public interface LoginViewCallbacks {
    void onConnectRequested(String host, String port);
    void onLoginRequested(String username, String password);
    void onExitRequested();
}