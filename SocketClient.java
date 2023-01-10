import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import java.net.URI;

public class SocketClient {

    MainInitiator mainInitiator = null;
    WebSocketClient client = null;
    Socket socket = null;

    public SocketClient(MainInitiator mainInitiator) {
        this.mainInitiator = mainInitiator;
    }

    public void init() {
        ConnectionManager cm = mainInitiator.getConnectionManager();
        if(cm.lockfileContents == null) {
            return;
        }
        String sUri = "wss://127.0.0.1:"+cm.lockfileContents[2]+"/";
        this.client = new WebSocketClient();
        Socket socket = new Socket(mainInitiator);
        try {
            client.start();
            URI uri = new URI(sUri);
            ClientUpgradeRequest request = new ClientUpgradeRequest();
            request.setHeader("Authorization",cm.authString);
            client.connect(socket, uri, request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopClient() {
        try {
            client.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
