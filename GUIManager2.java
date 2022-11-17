import org.json.JSONArray;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.net.HttpURLConnection;
import java.net.URL;

public class GUIManager2 {
    enum Roles {
        NONE("Unselected","UNSELECTED"),
        TOP("Top","TOP"),
        JUNGLE("Jungle","JUNGLE"),
        MID("Middle","MIDDLE"),
        BOTTOM("Bottom","Bottom"),
        SUPPORT("Support","UTILITY"),
        FILL("Fill","FILL");
        final String displayName;
        final String internalName;
        private Roles(String displayName, String internalName) {
            this.displayName = displayName;
            this.internalName = internalName;
        }
    }

    enum queues {
        BLIND("BLind Pick", 430, 5, false),
        DRAFT("Draft Pick",400, 5, true),
        SOLOQUEUE("Ranked Solo/Duo", 420, 2, true),
        FLEXQUEUE("Ranked Flex", 440, 3, true),

        ARAM("ARAM", 450, 5, false);

        final String name;
        final Integer queueId;
        final Integer allowedPremateSize;
        final Boolean showRoles;
        queues(String name, Integer queueId, Integer allowedPremateSizes, Boolean showRoles) {
            this.name = name;
            this.queueId = queueId;
            this.allowedPremateSize = allowedPremateSizes;
            this.showRoles = showRoles;
        }
    }

    MainInitiator mainInitiator;

    JPanel panelMainSelector;

    JButton btnMainLobby;
    JButton btnMainSummoner;
    JButton btnMainLoot;
    JButton btnMainTaks;

    JLayeredPane mainLayeredPage;

    JPanel panelLobby;

    JButton btnLobbySRBlind;
    JButton btnLobbySRDraft;
    JButton btnLobbySQ;
    JButton btnLobbyFQ;
    JButton btnLobbyAram;
    JButton btnLobbyStartQueue;

    JComboBox comboBoxLobbyPosition1;
    JComboBox comboBoxLobbyPositionTwo;

    JPanel panelLobbyMembers;

    JLabel currentQueue;

    public void update(JSONArray jsonArray) {
    }


}
