import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;

public class MainInitiator {

    public SocketClient client;
    public GUIManager guiManager;
    public ConnectionManager connectionManager;
    public TaskManager taskManager;
    public UpdateAgent updateAgent;

    public static String[] subscribeEndpoints = {"OnJsonApiEvent_lol-gameflow_v1_gameflow-phase","OnJsonApiEvent_chat_v3_friends","OnJsonApiEvent_lol-lobby_v2_lobby", "OnJsonApiEvent_lol-champ-select_v1_session"};

    public static void main(String [] args) {
        MainInitiator mainInit = new MainInitiator();
            mainInit.init();
    }

    public void init() {
        client = new SocketClient(this);
        guiManager = new GUIManager(this);
        connectionManager = new ConnectionManager(this);
        taskManager = new TaskManager(this);
        updateAgent = new UpdateAgent(this);
        connectionManager.init();
        client.init();
        guiManager.init();
    }

    public synchronized  void fireParameterChanged(String s) {
        updateAgent.updateMessage(s);
        updateAgent.run();
    }

    public SocketClient getClient() {
        return client;
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }

    public UpdateAgent getUpdateAgent() {
        return updateAgent;
    }

    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public TaskManager getTaskManager() {
        return taskManager;
    }

}
