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
        if(checkForLocationFile()) {
            mainInit.init();
        } else {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    createInputField();
                }
            });
        }
    }

    public static void createInputField() {
        JFrame frame;
        JTextField inputLockfileLocation;
        JButton btnSave;

        boolean alreadyCreated = false;
        frame = new JFrame();
        frame.setBounds(100, 100, 600, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Keine Locationfile");
        frame.setLayout(null);

        inputLockfileLocation = new JTextField();
        inputLockfileLocation.setBounds(0,0,600,30);
        btnSave = new JButton("Pfad speichern");
        btnSave.setBounds(0,50,200,30);

        JLabel lbl = new JLabel("Noch kein Pfad gegeben");
        lbl.setBounds(0,90,600,30);

        JLabel lbl2 = new JLabel();
        lbl2.setBounds(0,130,400,30);

        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String examplePath = inputLockfileLocation.getText();
                    File fileAbsolutePath = new File("");
                    String absolutePath = fileAbsolutePath.getAbsolutePath();
                    File locationFile = new File(absolutePath + "\\locationfile");
                    locationFile.createNewFile();
                    FileWriter writeToLocationFile = new FileWriter(locationFile);
                    writeToLocationFile.write(examplePath);
                    writeToLocationFile.close();
                    lbl.setText("Pfad auf: " + examplePath+ " gesetzt");
                    lbl2.setText("Du kannst das Programm nun neustarten");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                frame.repaint();
                frame.revalidate();
            }
        });

        frame.add(btnSave);
        frame.add(inputLockfileLocation);
        frame.add(lbl);
        frame.add(lbl2);



        frame.setVisible(true);
    }

    public MainInitiator getInstance() {
        return this;
    }
    
    public static boolean checkForLocationFile() {
        File fileAbsolutePath = new File("");
        String absolutePath = fileAbsolutePath.getAbsolutePath();
        File locationFile = new File(absolutePath + "\\locationfile");
        if (locationFile.exists() && !locationFile.isDirectory()) {
            return true;
        } else return false;
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
