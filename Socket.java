import java.io.IOException;
import java.util.TimerTask;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

@WebSocket
public class Socket {

    MainInitiator mainInitiator;

    public Socket(MainInitiator mainInitiator) {
        this.mainInitiator = mainInitiator;
    }

    public volatile TimerTask timerTask;

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        System.out.println("Closed: " + reason);
        System.out.println("Trying to restart");
        try {
            timerTask.cancel();
            this.timerTask = null;
            mainInitiator.getClient().stopClient();
            mainInitiator.getClient().socket = null;
            mainInitiator.getClient().init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnWebSocketError
    public void onError(Throwable t) {
        if (!(t.getMessage() == null) && !t.getMessage().equals("null")) {
            System.out.println("Error: " +t.getMessage());
        }
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("Connect: " + session.getRemoteAddress().getAddress());
        try {
            createNewKeepAlive(session);
            String[] arr = mainInitiator.subscribeEndpoints;
            for(int i = 0; i < arr.length; i++) {
                session.getRemote().sendString("[5,\""+arr[i]+"\"]");
            }
        } catch (IOException e) {
            System.out.println("IO Exception");
        }
    }

    @OnWebSocketMessage
    public void onMessage(String message) {
        if (!(message == null) && message != "") {
            mainInitiator.fireParameterChanged(message);
        }
    }

    public void createNewKeepAlive(Session s) {
        System.out.println("Created new Keep alive!");
        new java.util.Timer().schedule(
                this.timerTask = new java.util.TimerTask() {
                                           @Override
                                           public void run() {
                                               try {
                                                   s.getRemote().sendString(" ");
                                               } catch (Exception e) {
                                                   e.printStackTrace();
                                               }
                                               createNewKeepAlive(s);
                                           }
                                       }
                ,
                290000
        );
    }
}
