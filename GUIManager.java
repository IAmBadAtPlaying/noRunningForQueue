import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;


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

    JPanel panelTasksOptionsOne;

    JPanel panelTasksOptionsTwo;
    JComboBox comboBoxTasksOptionsTwoChamps;
    JLabel lblTaskOptionsTwoChamp;
    JRadioButton rdbtnTaskOptionsTwoHover;

    JPanel panelTasksOptionsThree;
    JComboBox comboBoxTasksOptionsThreeChamps;
    JLabel lblTaskOptionsThreeChamp;

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
    JTextField textFieldLobbyInvite;
    JButton btnLobbyInvite;

    JToggleButton tglbtnQueueAutoAccept;

    JPanel panelSummoner;

    JPanel panelSummonerInfo;
    JProgressBar progressBarSummonerLvl;
    TextField textSummonerSettingsTitle;
    JPanel panelSummonerSettings;
    JButton btnSummonerSettingsTokensSave;
    TextField textSummonerSettingsTokensOne;
    TextField textSummonerSettingsTokensTwo;
    TextField textSummonerSettingsTokensThree;
    JLabel lblSummonerSettingsTokensOne;
    JLabel lblSummonerSettingsTokensTwo;
    JLabel lblSummonerSettingsTokensThree;
    JPanel panelSummonerSettingsTokens;
    JLabel lblSummonerSettingsTitle;
    JLabel lblSummonerSettingsTokensAndTitles;
    JLabel lblSummonerSettingsTokensStatus;
    JLabel lblSummonerInfoPicture;
    JLabel lblSummonerInfoName;
    JLabel lblSummonerInfoLevel;

    boolean canQueue = false;

    MainInitiator mainInitiator;

    boolean showSelectedRoles = false;

    String[] Positions = {"Unselected", "Top", "Jungle", "Middle", "Bottom", "Support", "Fill"};
    String[] internalPositionNames = {"UNSELECTED", "TOP", "JUNGLE", "MIDDLE", "BOTTOM", "UTILITY", "FILL"};

    HashMap<Integer, String> queueMap = new HashMap<>();

    int positionOneIndex = 0;
    int positionTwoIndex = 0;

    public GUIManager(MainInitiator mainInitiator) {
        this.mainInitiator = mainInitiator;
    }

    public void update(JSONArray jsonArray) {
        try {
            JSONArray outerArray = jsonArray;
            if("OnJsonApiEvent_lol-lobby_v2_lobby".equals(outerArray.getString(1))) {
                JSONObject data = outerArray.getJSONObject(2);
                if(!"/lol-lobby/v2/lobby".equals(data.getString("uri"))) {
                    return;
                }
                refreshLobby();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void lobbyUpdateMembers() {
        try {
            Summoner self = null;
            JPanel[] panelArray = {null,null,null,null,null};
            int index = 0;
            HttpURLConnection con = mainInitiator.getConnectionManager().buildConnection(ConnectionManager.conOptions.GET, "/lol-lobby/v2/lobby", null);
            JSONObject data = (JSONObject) mainInitiator.getConnectionManager().getResponse(ConnectionManager.responseFormat.JSON_OBJECT, con);
            con.disconnect();
            if(data.has("localMember")) {
                self = mainInitiator.getConnectionManager().SummonerFromJsonObject(data.getJSONObject("localMember"));
                panelArray[0] = createLobbyMemberPage(self);
                index++;
            }
            if(data.has("members")) {
                JSONArray jsonMembers = data.getJSONArray("members");
                for(int i = 0; i < panelArray.length; i++) {
                    if(i < jsonMembers.length()) {
                        Summoner summoner = mainInitiator.getConnectionManager().SummonerFromJsonObject(jsonMembers.getJSONObject(i));
                        if(self != null && summoner.getSummonerId().intValue() == self.getSummonerId().intValue()) {
                            continue;
                        } else {
                            panelArray[index] = createLobbyMemberPage(summoner);
                        }
                    } else {
                        panelArray[index] = createLobbyMemberPage(null);
                    }
                    index++;
                }
            }
            if(data.has("gameConfig")) {
                JSONObject jsonConfig = data.getJSONObject("gameConfig");
                showSelectedRoles = jsonConfig.getBoolean("showPositionSelector");
            }
            if(data.has("gameConfig")) {
                JSONObject jsonConfig = data.getJSONObject("gameConfig");
                Integer jsonQueueId = jsonConfig.getInt("queueId");
                String mapName = queueMap.get(jsonQueueId);
                if(mapName!= null) {
                    currentQueue.setText(mapName);
                } else currentQueue.setText("Unknown Queue");
                canQueue = true;
            } else {
                currentQueue.setText("No Queue");
                canQueue = false;
            }
            panelLobbyMembers.removeAll();
            panelLobbyMembers.add(panelArray[3]);
            panelLobbyMembers.add(panelArray[1]);
            panelLobbyMembers.add(panelArray[0]);
            panelLobbyMembers.add(panelArray[2]);
            panelLobbyMembers.add(panelArray[4]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshLobby() {
        panelLobby.remove(panelLobbyMembers);
        panelLobby.remove(currentQueue);
        lobbyUpdateMembers();
        if(!showSelectedRoles) {
            panelLobby.remove(comboBoxLobbyPosition1);
            panelLobby.remove(comboBoxLobbyPositionTwo);
        }
        if (showSelectedRoles) {
            panelLobby.add(comboBoxLobbyPosition1);
            if (comboBoxLobbyPosition1.getSelectedIndex() != Positions.length-1) {
                panelLobby.add(comboBoxLobbyPositionTwo);
            } else {
                panelLobby.remove(comboBoxLobbyPositionTwo);
            }
        }
        if(canQueue) {
            panelLobby.add(btnLobbyStartQueue);
            panelLobby.add(textFieldLobbyInvite);
            panelLobby.add(btnLobbyInvite);
        } else {
            panelLobby.remove(btnLobbyStartQueue);
            panelLobby.remove(textFieldLobbyInvite);
            panelLobby.remove(btnLobbyInvite);
        }
        panelLobby.add(panelLobbyMembers);
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
        try {
            HttpURLConnection con = mainInitiator.getConnectionManager().buildConnection(ConnectionManager.conOptions.POST, "/lol-lobby/v2/lobby", "{\"queueId\":" + queueId + "}");
            con.getResponseCode();
            con.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
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
            URL url = new URL("https://ddragon.leagueoflegends.com/cdn/"+mainInitiator.getConnectionManager().version+"/img/profileicon/" + id + ".png");
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

        if(!internalPositionNames[6].equals(summoner.getFirstPositionPreference())) {
            JLabel lblLobbyMember1Position2;
            lblLobbyMember1Position2 = new JLabel(summoner.getSecondPositionPreference());
            panelLobbyMember1Positions.add(lblLobbyMember1Position2);
        }

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

    private void initIcon() {
        try {
            File iconPath = new File("assets\\Logo.png");
            System.out.println(iconPath.getAbsolutePath());
            ImageIcon icon = new ImageIcon(iconPath.getAbsolutePath().toString());
            frame.setIconImage(icon.getImage());
            frame.setTitle("Poro Client");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void init() {
        queueMap.put(430,"Blind Pick");
        queueMap.put(400,"Draft Pick");
        queueMap.put(420,"Ranked Solo");
        queueMap.put(440,"Ranked Flex");
        queueMap.put(450, "Aram");

        frame = new JFrame();
        frame.setBounds(100, 100, 1280, 720);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        initIcon();

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
                updateSummonerPage();
                switchMainPage(panelSummoner);
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

        panelSummoner = new JPanel();
        panelSummoner.setBounds(0,0,1244,558);
        panelSummoner.setLayout(new GridLayout(1, 0, 0, 0));

        panelSummonerInfo = new JPanel();
        panelSummoner.add(panelSummonerInfo);
        panelSummonerInfo.setLayout(null);

        lblSummonerInfoPicture = new JLabel();
        lblSummonerInfoPicture.setHorizontalAlignment(SwingConstants.CENTER);
        lblSummonerInfoPicture.setBounds(236, 0, 150, 150);
        panelSummonerInfo.add(lblSummonerInfoPicture);

        lblSummonerInfoName = new JLabel("Summoner Name");
        lblSummonerInfoName.setHorizontalAlignment(SwingConstants.CENTER);
        lblSummonerInfoName.setBounds(236, 186, 150, 30);
        panelSummonerInfo.add(lblSummonerInfoName);

        progressBarSummonerLvl = new JProgressBar();
        progressBarSummonerLvl.setBounds(236, 161, 150, 14);
        panelSummonerInfo.add(progressBarSummonerLvl);

        panelSummonerSettings = new JPanel();
        panelSummoner.add(panelSummonerSettings);
        panelSummonerSettings.setLayout(new GridLayout(0, 1, 0, 0));

        panelSummonerSettingsTokens = new JPanel();
        panelSummonerSettings.add(panelSummonerSettingsTokens);
        panelSummonerSettingsTokens.setLayout(null);

        btnSummonerSettingsTokensSave = new JButton("Save");
        btnSummonerSettingsTokensSave.setBounds(482, 238, 130, 30);
        btnSummonerSettingsTokensSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Integer first = null;
                Integer second = null;
                Integer third = null;
                String title= null;
                try {
                    first = Integer.parseInt(textSummonerSettingsTokensOne.getText().trim());
                    second = Integer.parseInt(textSummonerSettingsTokensTwo.getText().trim());
                    third = Integer.parseInt(textSummonerSettingsTokensThree.getText().trim());
                    title = textSummonerSettingsTitle.getText();
                    if(title == null) {
                        title = "";
                    }
                } catch (Exception ex) {
                    lblSummonerSettingsTokensStatus.setText("Error: Enter Integer values!");
                    ex.printStackTrace();
                    return;
                }
                lblSummonerSettingsTokensStatus.setText(mainInitiator.getConnectionManager().updatePlayerPreferences(first,second,third,title));
            }
        });
        panelSummonerSettingsTokens.add(btnSummonerSettingsTokensSave);

        textSummonerSettingsTokensOne = new TextField();
        textSummonerSettingsTokensOne.setBounds(10, 116, 175, 20);
        panelSummonerSettingsTokens.add(textSummonerSettingsTokensOne);

        textSummonerSettingsTokensTwo = new TextField();
        textSummonerSettingsTokensTwo.setBounds(219, 116, 175, 20);
        panelSummonerSettingsTokens.add(textSummonerSettingsTokensTwo);

        textSummonerSettingsTokensThree = new TextField();
        textSummonerSettingsTokensThree.setBounds(437, 116, 175, 20);
        panelSummonerSettingsTokens.add(textSummonerSettingsTokensThree);

        lblSummonerSettingsTokensOne = new JLabel("1st Token ID");
        lblSummonerSettingsTokensOne.setHorizontalAlignment(SwingConstants.CENTER);
        lblSummonerSettingsTokensOne.setBounds(10, 90, 175, 20);
        panelSummonerSettingsTokens.add(lblSummonerSettingsTokensOne);

        lblSummonerSettingsTokensTwo = new JLabel("2nd Token ID");
        lblSummonerSettingsTokensTwo.setHorizontalAlignment(SwingConstants.CENTER);
        lblSummonerSettingsTokensTwo.setBounds(219, 90, 175, 20);
        panelSummonerSettingsTokens.add(lblSummonerSettingsTokensTwo);

        lblSummonerSettingsTokensThree = new JLabel("3rd Token ID");
        lblSummonerSettingsTokensThree.setHorizontalAlignment(SwingConstants.CENTER);
        lblSummonerSettingsTokensThree.setBounds(437, 90, 175, 20);
        panelSummonerSettingsTokens.add(lblSummonerSettingsTokensThree);

        textSummonerSettingsTitle = new TextField();
        textSummonerSettingsTitle.setBounds(219, 200, 175, 20);
        panelSummonerSettingsTokens.add(textSummonerSettingsTitle);

        lblSummonerSettingsTitle = new JLabel("Title ID");
        lblSummonerSettingsTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblSummonerSettingsTitle.setBounds(219, 174, 175, 20);
        panelSummonerSettingsTokens.add(lblSummonerSettingsTitle);

        lblSummonerSettingsTokensAndTitles = new JLabel("Tokens and Titles");
        lblSummonerSettingsTokensAndTitles.setFont(new Font("Tahoma", Font.PLAIN, 20));
        lblSummonerSettingsTokensAndTitles.setHorizontalAlignment(SwingConstants.CENTER);
        lblSummonerSettingsTokensAndTitles.setBounds(219, 11, 175, 20);
        panelSummonerSettingsTokens.add(lblSummonerSettingsTokensAndTitles);

        lblSummonerSettingsTokensStatus = new JLabel("");
        lblSummonerSettingsTokensStatus.setFont(new Font("Tahoma", Font.PLAIN, 10));
        lblSummonerSettingsTokensStatus.setBounds(10, 238, 175, 30);
        panelSummonerSettingsTokens.add(lblSummonerSettingsTokensStatus);


        panelLobby = new JPanel();
        panelLobby.setBounds(0, 0, 1244, 558);
        mainLayeredPage.add(panelLobby);
        panelLobby.setLayout(null);


        btnLobbySRBlind = new JButton("Blind Pick");
        btnLobbySRBlind.setBounds(20, 11, 115, 23);
        btnLobbySRBlind.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
                createLobby(400, true);
            }
        });
        panelLobby.add(btnLobbySRDraft);


        btnLobbyAram = new JButton("Aram");
        btnLobbyAram.setBounds(540, 11, 115, 23);
        btnLobbyAram.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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


        currentQueue = new JLabel("No Queue Selected");
        currentQueue.setBounds(900,11,150,23);
        panelLobby.add(currentQueue);

        textFieldLobbyInvite = new JTextField();
        textFieldLobbyInvite.setToolTipText("Input Name of Summoner you want to invite");
        textFieldLobbyInvite.setBounds(820, 525, 270, 20);
        panelLobby.add(textFieldLobbyInvite);
        textFieldLobbyInvite.setColumns(10);

        btnLobbyInvite = new JButton("Invite Summoner");
        btnLobbyInvite.setBounds(1100, 525, 132, 20);
        btnLobbyInvite.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String summoner = textFieldLobbyInvite.getText();
                mainInitiator.getConnectionManager().inviteIntoLobby(summoner);
                textFieldLobbyInvite.setText("");
            }
        });
        panelLobby.add(btnLobbyInvite);

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
                if (rdbtnTaskOneActivate.isSelected()) {
                    mainInitiator.getTaskManager().createTask(Task.tasks.AUTO_ACCEPT_QUEUE);
                } else {
                    mainInitiator.getTaskManager().removeTask(Task.tasks.AUTO_ACCEPT_QUEUE);
                }
            }
        });
        panelTaskOne.add(rdbtnTaskOneActivate);

        txtpnTaskOne = new JTextPane();
        txtpnTaskOne.setText("Activate this option to Automatically accept Ready Checks");
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

        Object[] champArray = mainInitiator.getConnectionManager().ChampHash.keySet().toArray();
        Arrays.sort(champArray);

        rdbtnTaskTwoActivate = new JRadioButton("Activate");
        rdbtnTaskTwoActivate.setBounds(6, 7, 80, 23);
        rdbtnTaskTwoActivate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(rdbtnTaskTwoActivate.isSelected()) {
                    Object[] arr = {mainInitiator.getConnectionManager().ChampHash.get(champArray[comboBoxTasksOptionsTwoChamps.getSelectedIndex()]),!rdbtnTaskOptionsTwoHover.isSelected()};
                    mainInitiator.getTaskManager().createTask(Task.tasks.AUTO_PICK_CHAMP, arr);
                } else {
                    mainInitiator.getTaskManager().removeTask(Task.tasks.AUTO_PICK_CHAMP);
                }
            }
        });
        panelTaskTwo.add(rdbtnTaskTwoActivate);

        txtpnTaskTwo = new JTextPane();
        txtpnTaskTwo.setText("Activate this option if you want to Auto-Pick a certian Champ\r\n(WIP, but base functionality should work)");
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
                if(rdbtnTaskThreeActivate.isSelected()) {
                    Object[] arr = {mainInitiator.getConnectionManager().ChampHash.get(champArray[comboBoxTasksOptionsThreeChamps.getSelectedIndex()])};
                    mainInitiator.getTaskManager().createTask(Task.tasks.AUTO_BAN_CHAMP, arr);
                } else {
                    mainInitiator.getTaskManager().removeTask(Task.tasks.AUTO_BAN_CHAMP);
                }
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

        panelTasksOptionsOne = new JPanel();
        panelTasksOptionsOne.setBounds(0, 0, 622, 558);
        panelTasksOptionsOne.setLayout(null);

        panelTasksOptionsTwo = new JPanel();
        panelTasksOptionsTwo.setBounds(0, 0, 622, 558);
        panelTasksOptionsTwo.setLayout(null);


        comboBoxTasksOptionsTwoChamps = new JComboBox(champArray);
        comboBoxTasksOptionsTwoChamps.setBounds(312,11,300,50);
        comboBoxTasksOptionsTwoChamps.setSelectedIndex(0);
        /*listTasksOptionsTwoChamps.setLayoutOrientation(JList.VERTICAL);*/
        comboBoxTasksOptionsTwoChamps.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object[] arr = {mainInitiator.getConnectionManager().ChampHash.get(champArray[comboBoxTasksOptionsTwoChamps.getSelectedIndex()]),!rdbtnTaskOptionsTwoHover.isSelected()};
                mainInitiator.getTaskManager().updateTask(Task.tasks.AUTO_PICK_CHAMP, arr);
            }
        });
        panelTasksOptionsTwo.add(comboBoxTasksOptionsTwoChamps);


        lblTaskOptionsTwoChamp = new JLabel("Select Champion to pick");
        lblTaskOptionsTwoChamp.setHorizontalAlignment(SwingConstants.CENTER);
        lblTaskOptionsTwoChamp.setBounds(10, 11, 300, 50);
        panelTasksOptionsTwo.add(lblTaskOptionsTwoChamp);

        rdbtnTaskOptionsTwoHover = new JRadioButton("Hover over Champ before pick");
        rdbtnTaskOptionsTwoHover.setBounds(434, 90, 178, 23);
        rdbtnTaskOptionsTwoHover.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object[] arr = {mainInitiator.getConnectionManager().ChampHash.get(champArray[comboBoxTasksOptionsTwoChamps.getSelectedIndex()]),!rdbtnTaskOptionsTwoHover.isSelected()};
                mainInitiator.getTaskManager().updateTask(Task.tasks.AUTO_PICK_CHAMP, arr);
            }
        });
        panelTasksOptionsTwo.add(rdbtnTaskOptionsTwoHover);

        panelTasksOptionsThree = new JPanel();
        panelTasksOptionsThree.setBounds(0, 0, 622, 558);
        panelTasksOptionsThree.setLayout(null);

        comboBoxTasksOptionsThreeChamps = new JComboBox(champArray);
        comboBoxTasksOptionsThreeChamps.setBounds(312,11,300,50);
        comboBoxTasksOptionsThreeChamps.setSelectedIndex(0);
        comboBoxTasksOptionsThreeChamps.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object[] arr = {mainInitiator.getConnectionManager().ChampHash.get(champArray[comboBoxTasksOptionsThreeChamps.getSelectedIndex()])};
                mainInitiator.getTaskManager().updateTask(Task.tasks.AUTO_BAN_CHAMP, arr);
            }
        });
        panelTasksOptionsThree.add(comboBoxTasksOptionsThreeChamps);

        lblTaskOptionsThreeChamp = new JLabel("Select Champion to ban");
        lblTaskOptionsThreeChamp.setHorizontalAlignment(SwingConstants.CENTER);
        lblTaskOptionsThreeChamp.setBounds(10, 11, 300, 50);
        panelTasksOptionsThree.add(lblTaskOptionsThreeChamp);

        updateSummonerPage();
        refreshLobby();

        frame.setVisible(true);
    }

    private void updateSummonerPage() {
        Summoner self = (Summoner) mainInitiator.getConnectionManager().getResponse(ConnectionManager.responseFormat.SUMMONER, mainInitiator.getConnectionManager().buildConnection(ConnectionManager.conOptions.GET, "/lol-summoner/v1/current-summoner", null));
        if(self == null) return;
        JSONObject jsonData = (JSONObject) mainInitiator.getConnectionManager().getResponse(ConnectionManager.responseFormat.JSON_OBJECT, mainInitiator.getConnectionManager().buildConnection(ConnectionManager.conOptions.GET, "/lol-hovercard/v1/friend-info-by-summoner/"+self.getSummonerId(), null));
        if(jsonData.has("lol")) {
            JSONObject lolData = jsonData.getJSONObject("lol");
            if(lolData.has("challengeTokensSelected")) {
                String selectedTokens = lolData.getString("challengeTokensSelected");
                tokens:if(selectedTokens != null) {
                    String tokens[] = selectedTokens.split(",");
                    if(tokens.length != 3) {break tokens;}
                    textSummonerSettingsTokensOne.setText(tokens[0].trim());
                    textSummonerSettingsTokensTwo.setText(tokens[1].trim());
                    textSummonerSettingsTokensThree.setText(tokens[2].trim());
                }
            }
        }
        lblSummonerInfoPicture.setIcon(getSummonerIcon(self.getProfileIconId()));
        lblSummonerInfoPicture.setToolTipText("Profile Icon ID: " +self.getProfileIconId());
        lblSummonerInfoName.setText(self.getDisplayName());
        progressBarSummonerLvl.setValue(self.getPercentCompleteForNextLevel());
        progressBarSummonerLvl.setString(self.getSummonerLevel().toString());
    }
};

