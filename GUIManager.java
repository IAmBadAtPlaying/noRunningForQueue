import org.json.JSONArray;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

public class GUIManager {

    private JFrame frame;

    JPanel panelMainSelector;

    JButton btnMainLobby;
    JButton btnMainSummoner;
    JButton btnMainLoot;
    JButton btnMainTasks;

    JLayeredPane mainLayeredPage;
    JPanel panelTasks;

    JPanel panelTasksOptions;

    JPanel panelTasksOptionsTwo;
    JPanel panelTasksOptionsOne;
    JPanel panelTasksOptionsThree;

    JPanel panelTaskOverview;

    JPanel panelTaskOne;
    JPanel panelTaskTwo;
    JPanel panelTaskThree;

    JButton btnTaskOneOptions;
    JButton btnTaskTwoOptions;
    JButton btnTaskThreeOptions;

    JRadioButton rdbtnTaskOneActivate;
    JRadioButton rdbtnTaskTwoActivate;
    JRadioButton rdbtnTaskThreeActivate;

    JTextPane txtpnTaskOne;
    JTextPane txtpnTaskTwo;
    JTextPane txtpnTaskThree;

    JPanel panelLobby;

    JButton btnLobbySRBlind;
    JButton btnLobbySRDraft;
    JButton btnLobbySQ;
    JButton btnLobbyFQ;
    JButton btnLobbyAram;
    JButton btnLobbyStartQueue;

    JComboBox comboBoxLobbyPosition1;
    JComboBox comboBoxLobbyPositionTwo;
    JComboBox comboBoxPickChamp;

    JPanel panelLobbyMembers;
    JLabel currentQueue;

    JButton DebugRefresh;
    JToggleButton tglbtnQueueAutoAccept;

    JPanel panelSummoner;

    MainInitiator mainInitiator;

    boolean showSelectedRoles = false;

    String[] Positions = {"Unselected", "Top", "Jungle", "Middle", "Bottom", "Support", "Fill"};
    String[] internalPositionNames = {"UNSELECTED", "TOP", "JUNGLE", "MIDDLE", "BOTTOM", "UTILITY", "FILL"};

    int positionOneIndex = 0;
    int positionTwoIndex = 0;

    public GUIManager(MainInitiator mainInitiator) {
        this.mainInitiator = mainInitiator;
    }

    public void update(JSONArray jsonArray) {

    }

    public void lobbyUpdateMembers() {
        panelLobbyMembers.removeAll();
        try {
            HttpURLConnection con = mainInitiator.getConnectionManager().buildConnection(ConnectionManager.conOptions.GET, "/lol-lobby/v2/lobby/members", null);
            Summoner[] array = (Summoner[]) mainInitiator.getConnectionManager().getResponse(ConnectionManager.responseFormat.SUMMONER_ARRAY, con);
            switch (array.length) {
                case 1:
                    panelLobbyMembers.add(createLobbyMemberPage(null));
                    panelLobbyMembers.add(createLobbyMemberPage(null));
                    panelLobbyMembers.add(createLobbyMemberPage(array[0]));
                    panelLobbyMembers.add(createLobbyMemberPage(null));
                    panelLobbyMembers.add(createLobbyMemberPage(null));
                    break;
                case 2:
                    panelLobbyMembers.add(createLobbyMemberPage(null));
                    panelLobbyMembers.add(createLobbyMemberPage(array[1]));
                    panelLobbyMembers.add(createLobbyMemberPage(array[0]));
                    panelLobbyMembers.add(createLobbyMemberPage(null));
                    panelLobbyMembers.add(createLobbyMemberPage(null));
                    break;
                case 3:
                    panelLobbyMembers.add(createLobbyMemberPage(null));
                    panelLobbyMembers.add(createLobbyMemberPage(array[2]));
                    panelLobbyMembers.add(createLobbyMemberPage(array[1]));
                    panelLobbyMembers.add(createLobbyMemberPage(array[0]));
                    panelLobbyMembers.add(createLobbyMemberPage(null));
                    break;
                case 4:
                    panelLobbyMembers.add(createLobbyMemberPage(array[3]));
                    panelLobbyMembers.add(createLobbyMemberPage(array[2]));
                    panelLobbyMembers.add(createLobbyMemberPage(array[1]));
                    panelLobbyMembers.add(createLobbyMemberPage(array[0]));
                    panelLobbyMembers.add(createLobbyMemberPage(null));
                    break;
                case 5:
                    panelLobbyMembers.add(createLobbyMemberPage(array[4]));
                    panelLobbyMembers.add(createLobbyMemberPage(array[3]));
                    panelLobbyMembers.add(createLobbyMemberPage(array[2]));
                    panelLobbyMembers.add(createLobbyMemberPage(array[1]));
                    panelLobbyMembers.add(createLobbyMemberPage(array[0]));
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        panelLobbyMembers.repaint();
        panelLobbyMembers.revalidate();
    }

    private void refreshLobby() {
        panelLobby.removeAll();
        if (showSelectedRoles) {
            panelLobby.add(comboBoxLobbyPosition1);
            panelLobby.add(comboBoxLobbyPositionTwo);
        }
        lobbyUpdateMembers();
        panelLobby.add(btnLobbyFQ);
        panelLobby.add(btnLobbySQ);
        panelLobby.add(btnLobbyAram);
        panelLobby.add(btnLobbySRBlind);
        panelLobby.add(btnLobbySRDraft);
        panelLobby.add(panelLobbyMembers);
        panelLobby.add(btnLobbyStartQueue);
        panelLobby.add(DebugRefresh);
        panelLobby.add(currentQueue);
        panelLobby.repaint();
        panelLobby.revalidate();
    }

    public void startMatchmaking() {
        if (showSelectedRoles) {
            if (!((positionOneIndex != 0 && positionTwoIndex != 0) || positionOneIndex == 6)) {
                return;
            }
        }
            try {
                HttpURLConnection con = mainInitiator.getConnectionManager().buildConnection(ConnectionManager.conOptions.POST, "/lol-lobby/v2/lobby/matchmaking/search", "");
                con.getResponseCode();
                con.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
    }


    public void createLobby(int queueId, boolean selectRoles) {
        this.showSelectedRoles = selectRoles;
        try {
            HttpURLConnection con = mainInitiator.getConnectionManager().buildConnection(ConnectionManager.conOptions.POST, "/lol-lobby/v2/lobby", "{\"queueId\":" + queueId + "}");
            con.getResponseCode();
            con.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        refreshLobby();
    }

    public void pushAndCheckRoles() {
        positionOneIndex = comboBoxLobbyPosition1.getSelectedIndex();
        positionTwoIndex = comboBoxLobbyPositionTwo.getSelectedIndex();
        if (comboBoxLobbyPositionTwo.getSelectedIndex() == comboBoxLobbyPosition1.getSelectedIndex()) {
            comboBoxLobbyPositionTwo.setSelectedIndex(0);
            positionTwoIndex = 0;
        }
        try {
            HttpURLConnection con = mainInitiator.getConnectionManager().buildConnection(ConnectionManager.conOptions.PUT, "/lol-lobby/v2/lobby/members/localMember/position-preferences", "{\"firstPreference\": \"" + internalPositionNames[comboBoxLobbyPosition1.getSelectedIndex()] + "\",\"secondPreference\":\"" + internalPositionNames[comboBoxLobbyPositionTwo.getSelectedIndex()] + "\"}");
            con.getResponseCode();
            con.disconnect();
            lobbyUpdateMembers();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return;
    }

    public ImageIcon getSummonerIcon(Integer id) {
        if (id == null) {
            return null;
        }
        try {
            URL url = new URL("https://ddragon.leagueoflegends.com/cdn/12.18.1/img/profileicon/" + id + ".png");
            return new ImageIcon(createResizedCopy(ImageIO.read(url), 142, 142, true));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private BufferedImage createResizedCopy(Image originalImage, int scaledWidth, int scaledHeight, boolean preserveAlpha) {
        int imageType = preserveAlpha ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage scaledBI = new BufferedImage(scaledWidth, scaledHeight, imageType);
        Graphics2D g = scaledBI.createGraphics();
        if (preserveAlpha) {
            g.setComposite(AlphaComposite.Src);
        }
        g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
        g.dispose();
        return scaledBI;
    }


    private JPanel createLobbyMemberPage(Summoner summoner) {
        if (summoner == null) {
            summoner = new Summoner(null);
            summoner.setFirstPositionPreference(null);
            summoner.setSecondPositionPreference(null);
            summoner.setSummonerLevel(null);
            summoner.setDisplayName(null);
        }
        JPanel panelLobbyMember1;
        panelLobbyMember1 = new JPanel();
        panelLobbyMember1.setLayout(new BorderLayout(0, 0));

        JPanel panelLobbyMember1Positions;
        panelLobbyMember1Positions = new JPanel();
        panelLobbyMember1.add(panelLobbyMember1Positions, BorderLayout.SOUTH);
        panelLobbyMember1Positions.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 50));

        JLabel lblLobbyMember1Position1;
        lblLobbyMember1Position1 = new JLabel(summoner.getFirstPositionPreference());
        panelLobbyMember1Positions.add(lblLobbyMember1Position1);

        JLabel lblLobbyMember1Position2;
        lblLobbyMember1Position2 = new JLabel(summoner.getSecondPositionPreference());
        panelLobbyMember1Positions.add(lblLobbyMember1Position2);

        Component verticalStrutLobbyMember1 = Box.createVerticalStrut(40);
        panelLobbyMember1.add(verticalStrutLobbyMember1, BorderLayout.NORTH);

        Component horizontalStrutLobbyMember1Left = Box.createHorizontalStrut(40);
        panelLobbyMember1.add(horizontalStrutLobbyMember1Left, BorderLayout.WEST);

        Component horizontalStrutLobbyMember1Right = Box.createHorizontalStrut(40);
        panelLobbyMember1.add(horizontalStrutLobbyMember1Right, BorderLayout.EAST);

        JPanel panelLobbyMember1Card;
        panelLobbyMember1Card = new JPanel();
        panelLobbyMember1.add(panelLobbyMember1Card, BorderLayout.CENTER);
        panelLobbyMember1Card.setLayout(null);

        JLabel lblLobbyMember1Icon;
        lblLobbyMember1Icon = new JLabel(getSummonerIcon(summoner.getProfileIconId()));
        lblLobbyMember1Icon.setHorizontalAlignment(SwingConstants.CENTER);
        lblLobbyMember1Icon.setBounds(10, 11, 142, 142);
        panelLobbyMember1Card.add(lblLobbyMember1Icon);

        JLabel lblLobbyMember1Name;
        lblLobbyMember1Name = new JLabel(summoner.getDisplayName());
        lblLobbyMember1Name.setHorizontalAlignment(SwingConstants.CENTER);
        lblLobbyMember1Name.setBounds(10, 200, 142, 14);
        panelLobbyMember1Card.add(lblLobbyMember1Name);
        panelLobbyMember1.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        return panelLobbyMember1;
    }

    private void switchMainPage(JPanel newPanel) {
        mainLayeredPage.removeAll();
        mainLayeredPage.add(newPanel);
        mainLayeredPage.repaint();
        mainLayeredPage.revalidate();
    }

    private void switchTaskOptionPage(JPanel newPanel) {
        panelTasksOptions.removeAll();
        panelTasksOptions.add(newPanel);
        panelTasksOptions.repaint();
        panelTasksOptions.revalidate();
    }


    public void init() {
        frame = new JFrame();
        frame.setBounds(100, 100, 1280, 720);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);


        panelMainSelector = new JPanel();
        panelMainSelector.setBounds(10, 11, 1244, 90);
        frame.getContentPane().add(panelMainSelector);
        panelMainSelector.setLayout(null);


        btnMainLobby = new JButton("Show Lobby");
        btnMainLobby.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                switchMainPage(panelLobby);
            }
        });
        btnMainLobby.setBounds(10, 21, 165, 40);
        panelMainSelector.add(btnMainLobby);


        btnMainSummoner = new JButton("Show Summoner");
        btnMainSummoner.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            }
        });
        btnMainSummoner.setBounds(185, 21, 165, 40);
        panelMainSelector.add(btnMainSummoner);


        btnMainLoot = new JButton("Show Loot");
        btnMainLoot.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            }
        });
        btnMainLoot.setBounds(360, 21, 165, 40);
        panelMainSelector.add(btnMainLoot);

        btnMainTasks = new JButton("Show Tasks");
        btnMainTasks.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switchMainPage(panelTasks);
            }
        });
        btnMainTasks.setBounds(535, 21, 165, 40);
        panelMainSelector.add(btnMainTasks);

        mainLayeredPage = new JLayeredPane();
        mainLayeredPage.setBounds(10, 112, 1244, 558);
        frame.getContentPane().add(mainLayeredPage);
        mainLayeredPage.setLayout(null);

        panelLobby = new JPanel();
        panelLobby.setBounds(0, 0, 1244, 558);
        mainLayeredPage.add(panelLobby);
        panelLobby.setLayout(null);


        btnLobbySRBlind = new JButton("Blind Pick");
        btnLobbySRBlind.setBounds(20, 11, 115, 23);
        btnLobbySRBlind.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentQueue.setText(btnLobbySRBlind.getText());
                createLobby(430, false);
            }
        });
        panelLobby.add(btnLobbySRBlind);


        btnLobbySQ = new JButton("Ranked Solo/Duo");
        btnLobbySQ.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentQueue.setText(btnLobbySQ.getText());
                createLobby(420, true);
            }
        });
        btnLobbySQ.setBounds(270, 11, 115, 23);
        panelLobby.add(btnLobbySQ);


        btnLobbyFQ = new JButton("Ranked Flex");
        btnLobbyFQ.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentQueue.setText(btnLobbyFQ.getText());
                createLobby(440, true);
            }
        });
        btnLobbyFQ.setBounds(395, 11, 115, 23);
        panelLobby.add(btnLobbyFQ);


        btnLobbySRDraft = new JButton("Draft Pick");
        btnLobbySRDraft.setBounds(145, 11, 115, 23);
        btnLobbySRDraft.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentQueue.setText(btnLobbySRDraft.getText());
                createLobby(400, true);
            }
        });
        panelLobby.add(btnLobbySRDraft);


        btnLobbyAram = new JButton("Aram");
        btnLobbyAram.setBounds(540, 11, 115, 23);
        btnLobbyAram.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentQueue.setText(btnLobbyAram.getText());
                createLobby(450, false);
            }
        });
        panelLobby.add(btnLobbyAram);


        comboBoxLobbyPosition1 = new JComboBox(Positions);
        comboBoxLobbyPosition1.setSelectedIndex(0);
        comboBoxLobbyPosition1.setBounds(10, 524, 200, 23);
        comboBoxLobbyPosition1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pushAndCheckRoles();
            }
        });
        panelLobby.add(comboBoxLobbyPosition1);


        btnLobbyStartQueue = new JButton("Start Queue");
        btnLobbyStartQueue.setBounds(566, 524, 115, 23);
        btnLobbyStartQueue.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startMatchmaking();
            }
        });
        panelLobby.add(btnLobbyStartQueue);


        comboBoxLobbyPositionTwo = new JComboBox(Positions);
        comboBoxLobbyPositionTwo.setSelectedIndex(0);
        comboBoxLobbyPositionTwo.setBounds(220, 524, 200, 23);
        comboBoxLobbyPositionTwo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pushAndCheckRoles();
            }
        });
        panelLobby.add(comboBoxLobbyPositionTwo);


        panelLobbyMembers = new JPanel();
        panelLobbyMembers.setBounds(20, 45, 1214, 468);
        panelLobby.add(panelLobbyMembers);
        panelLobbyMembers.setLayout(new GridLayout(1, 0, 0, 0));



        DebugRefresh = new JButton("DEBUG Refresh");
        DebugRefresh.setBounds(1145, 524, 89, 23);
        DebugRefresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainInitiator.getTaskManager().createTask(Task.tasks.AUTO_PICK_CHAMP,111);
            }
        });
        panelLobby.add(DebugRefresh);

        /*Object[] champArray = mainInitiator.getConnectionManager().ChampHash.keySet().toArray();
        Arrays.sort(champArray);

        comboBoxPickChamp = new JComboBox(champArray);
        comboBoxPickChamp.setBounds(900,524,89,23);
        panelLobby.add(comboBoxPickChamp);*/

        currentQueue = new JLabel("No Queue Selected");
        currentQueue.setBounds(900,11,150,23);
        panelLobby.add(currentQueue);

        panelTasks = new JPanel();
        panelTasks.setBounds(0, 0, 1244, 558);
        panelTasks.setLayout(new GridLayout(1, 0, 0, 0));

        panelTaskOverview = new JPanel();
        panelTasks.add(panelTaskOverview);
        GridBagLayout gbl_panelTaskOverview = new GridBagLayout();
        gbl_panelTaskOverview.columnWidths = new int[]{0, 0};
        gbl_panelTaskOverview.rowHeights = new int[]{0, 0, 0, 0};
        gbl_panelTaskOverview.columnWeights = new double[]{1.0, Double.MIN_VALUE};
        gbl_panelTaskOverview.rowWeights = new double[]{1.0, 1.0, 1.0, Double.MIN_VALUE};
        panelTaskOverview.setLayout(gbl_panelTaskOverview);

        panelTaskOne = new JPanel();
        panelTaskOne.setLayout(null);
        GridBagConstraints gbc_panelTaskOne = new GridBagConstraints();
        gbc_panelTaskOne.insets = new Insets(0, 0, 5, 0);
        gbc_panelTaskOne.fill = GridBagConstraints.BOTH;
        gbc_panelTaskOne.gridx = 0;
        gbc_panelTaskOne.gridy = 0;
        panelTaskOverview.add(panelTaskOne, gbc_panelTaskOne);

        btnTaskOneOptions = new JButton("Options");
        btnTaskOneOptions.setBounds(523, 11, 89, 23);
        btnTaskOneOptions.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switchTaskOptionPage(panelTasksOptionsOne);
            }
        });
        panelTaskOne.add(btnTaskOneOptions);

        rdbtnTaskOneActivate = new JRadioButton("Activate");
        rdbtnTaskOneActivate.setBounds(6, 7, 80, 23);
        rdbtnTaskOneActivate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println(rdbtnTaskOneActivate.isSelected());
                if (rdbtnTaskOneActivate.isSelected()) {
                    mainInitiator.getTaskManager().createTask(Task.tasks.AUTO_ACCEPT_QUEUE);
                } else {
                    mainInitiator.getTaskManager().removeTask(Task.tasks.AUTO_ACCEPT_QUEUE);
                }
            }
        });
        panelTaskOne.add(rdbtnTaskOneActivate);

        txtpnTaskOne = new JTextPane();
        txtpnTaskOne.setText("Activate this option to Automatically accept Ready Checks \r\n(for now only if you are the one to start the queue)");
        txtpnTaskOne.setBounds(6, 37, 500, 134);
        panelTaskOne.add(txtpnTaskOne);

        panelTaskTwo = new JPanel();
        panelTaskTwo.setLayout(null);
        GridBagConstraints gbc_panelTaskTwo = new GridBagConstraints();
        gbc_panelTaskTwo.insets = new Insets(0, 0, 5, 0);
        gbc_panelTaskTwo.fill = GridBagConstraints.BOTH;
        gbc_panelTaskTwo.gridx = 0;
        gbc_panelTaskTwo.gridy = 1;
        panelTaskOverview.add(panelTaskTwo, gbc_panelTaskTwo);

        btnTaskTwoOptions = new JButton("Options");
        btnTaskTwoOptions.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                switchTaskOptionPage(panelTasksOptionsTwo);
            }
        });
        btnTaskTwoOptions.setBounds(523, 11, 89, 23);
        panelTaskTwo.add(btnTaskTwoOptions);

        rdbtnTaskTwoActivate = new JRadioButton("Activate");
        rdbtnTaskTwoActivate.setBounds(6, 7, 80, 23);
        rdbtnTaskTwoActivate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(rdbtnTaskThreeActivate.isSelected()) {
                    mainInitiator.getTaskManager().createTask(Task.tasks.AUTO_PICK_CHAMP, 1);
                } else {
                    mainInitiator.getTaskManager().removeTask(Task.tasks.AUTO_PICK_CHAMP);
                }
            }
        });
        panelTaskTwo.add(rdbtnTaskTwoActivate);

        txtpnTaskTwo = new JTextPane();
        txtpnTaskTwo.setText("Activate this option if you want to Auto-Pick a certian Champ\r\n(some Bugs still may happen WIP)");
        txtpnTaskTwo.setBounds(6, 37, 500, 134);
        panelTaskTwo.add(txtpnTaskTwo);

        panelTaskThree = new JPanel();
        panelTaskThree.setLayout(null);
        GridBagConstraints gbc_panelTaskThree = new GridBagConstraints();
        gbc_panelTaskThree.fill = GridBagConstraints.BOTH;
        gbc_panelTaskThree.gridx = 0;
        gbc_panelTaskThree.gridy = 2;
        panelTaskOverview.add(panelTaskThree, gbc_panelTaskThree);

        btnTaskThreeOptions = new JButton("Options");
        btnTaskThreeOptions.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                switchTaskOptionPage(panelTasksOptionsThree);
            }
        });
        btnTaskThreeOptions.setBounds(523, 11, 89, 23);
        panelTaskThree.add(btnTaskThreeOptions);

        rdbtnTaskThreeActivate = new JRadioButton("Activate");
        rdbtnTaskThreeActivate.setBounds(10, 7, 80, 23);
        rdbtnTaskThreeActivate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        panelTaskThree.add(rdbtnTaskThreeActivate);

        txtpnTaskThree = new JTextPane();
        txtpnTaskThree.setText("Activate this option if you want to Auto-Ban a certian Champ\r\n(not implemented yet WIP)");
        txtpnTaskThree.setBounds(10, 37, 500, 134);
        panelTaskThree.add(txtpnTaskThree);

        panelTasksOptions = new JPanel();
        panelTasks.add(panelTasksOptions);
        panelTasksOptions.setLayout(null);

        panelTasksOptionsTwo = new JPanel();
        panelTasksOptionsTwo.setBounds(0, 0, 622, 558);


        panelTasksOptionsOne = new JPanel();
        panelTasksOptionsOne.setBounds(0, 0, 622, 558);


        panelTasksOptionsThree = new JPanel();
        panelTasksOptionsThree.setBounds(0, 0, 622, 558);


        frame.setVisible(true);
    }

};

