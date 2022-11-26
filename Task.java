import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;

public class Task {
    enum tasks {
        AUTO_ACCEPT_QUEUE (1),
        AUTO_PICK_CHAMP (2),
        AUTO_BAN_CHAMP (3);
        final int id;
        tasks(int id) {
            this.id = id;
        }
    }


    enum state {
        LOBBY(tasks.AUTO_ACCEPT_QUEUE,"Lobby"),
        MATCHMAKING(tasks.AUTO_ACCEPT_QUEUE, "Matchmaking"),
        READY_CHECK(tasks.AUTO_ACCEPT_QUEUE, "ReadyCheck"),
        CHAMP_SELECT(tasks.AUTO_ACCEPT_QUEUE,"ChampSelect"),
        GAME_START(tasks.AUTO_ACCEPT_QUEUE, "GameStart");

        final String name;
        final tasks task;
        state(tasks task, String name) {
            this.task = task;
            this.name = name;
        }

        public static state getByName(String s) {
            for(state st : state.values()) {
                if(st.name.equals(s)) {
                    return st;
                }
            }
            return null;
        }
    }

    private tasks task;
    private MainInitiator mainInitiator;
    private Object[] args;
    private state currentState;

    private ArrayList<Object> saveDataa;

    public Task(tasks task, MainInitiator mainInitiator, Object[] args) {
        this.task = task;
        this.mainInitiator = mainInitiator;
        this.args = args;
        if(args!=null) {
            resetAutoPickChamp();
        }
    }

    public Task(tasks task, MainInitiator mainInitiator) {
        this.task = task;
        this.mainInitiator = mainInitiator;
    }


    public void update(JSONArray array) {
        switch (task) {
            case AUTO_ACCEPT_QUEUE -> {
                updateAutoAcceptQueue(array);
            }
            case AUTO_PICK_CHAMP -> {
                updateAutoPickChamp(array);
            }
            case AUTO_BAN_CHAMP -> {
                updateAutoBanChamp(array);
            }
            default -> {

            }
        }
    }

    private synchronized void updateAutoBanChamp(JSONArray array) {
        try {
            Integer actionId = null;
            JSONArray outerArray = array;
            if("OnJsonApiEvent_lol-champ-select_v1_session".equals(outerArray.getString(1))) {
                if((Boolean) saveDataa.get(2)) {
                    return;
                }
                JSONObject outerObject = outerArray.getJSONObject(2);
                if(!(Boolean) saveDataa.get(1)) {
                    System.out.println("Init localPlayerCellId");
                    this.saveDataa.set(0, outerObject.getJSONObject("data").getInt("localPlayerCellId"));
                    saveDataa.set(1, true);
                }
                JSONArray actionArray = outerObject.getJSONObject("data").getJSONArray("actions");
                System.out.println();
                for (int j = 0; j < actionArray.length(); j++) {
                    JSONArray actionSubArray = actionArray.getJSONArray(j);
                    for(int i = 0; i < actionSubArray.length(); i++) {
                        JSONObject action = actionSubArray.getJSONObject(i);
                        if(action.getInt("actorCellId") == (int) this.saveDataa.get(0)) {
                            if(action.getBoolean("isAllyAction") && "ban".equals(action.getString("type")) && action.getBoolean("isInProgress")) {
                                actionId = action.getInt("id");
                                action.put("championId", (int) args[0]); //Put ChampionId here!
                                pickChamp(action,actionId);
                            }
                        }
                    }
                }
            }
            if("OnJsonApiEvent_lol-gameflow_v1_gameflow-phase".equals(outerArray.getString(1))) {
                JSONObject outerObject = outerArray.getJSONObject(2);
                if("Update".equals(outerObject.getString("eventType"))) {
                    if("Lobby".equals(outerObject.getString("data"))) {
                        resetAutoPickChamp();
                    }
                    if("None".equals(outerObject.getString("data"))||"GameStart".equals(outerObject.getString("data"))) {
                        resetAutoPickChamp();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized void updateAutoPickChamp(JSONArray array) {
        try {
            Integer actionId = null;
            JSONArray outerArray = array;
            if("OnJsonApiEvent_lol-champ-select_v1_session".equals(outerArray.getString(1))) { // Eigentlich nur bei Init nach Pick-Order schauen, dann ob bei actions, ally action mit der summoner ID in progress ist
                if((Boolean) saveDataa.get(2)) {
                    return;
                }
                JSONObject outerObject = outerArray.getJSONObject(2);
                if (!(Boolean) saveDataa.get(1)) {
                    System.out.println("Init localPlayerCellId");
                    this.saveDataa.set(0, outerObject.getJSONObject("data").getInt("localPlayerCellId"));
                    saveDataa.set(1, true);
                }
                JSONArray actionArray = outerObject.getJSONObject("data").getJSONArray("actions");
                System.out.println(actionArray.toString());
                outer: for(int i = 0; i < actionArray.length(); i++) {
                    JSONArray actionSubArray = actionArray.getJSONArray(i);
                    for (int j = 0; j < actionSubArray.length(); j++) {
                        JSONObject action = actionSubArray.getJSONObject(j);
                        if(action.getInt("actorCellId") == (int) this.saveDataa.get(0)) { // CellIdFrom my Team
                            if(action.getBoolean("isAllyAction") && "pick".equals(action.getString("type"))) {
                                actionId = action.getInt("id");
                                action.put("championId", (int) args[0]); //Put ChampionId here!
                                action.remove("pickTurn");
                                if(action.getBoolean("isInProgress")) {
                                    pickChamp(action, actionId);
                                } else if(!(Boolean) saveDataa.get(3)) {
                                    hoverChamp(action, actionId);
                                }
                                break outer;
                            }
                        }
                    }
                }
            }
            if("OnJsonApiEvent_lol-gameflow_v1_gameflow-phase".equals(outerArray.getString(1))) {
                JSONObject outerObject = outerArray.getJSONObject(2);
                if("Update".equals(outerObject.getString("eventType"))) {
                    if("Lobby".equals(outerObject.getString("data"))) {
                        resetAutoPickChamp();
                    }
                    if("None".equals(outerObject.getString("data"))||"GameStart".equals(outerObject.getString("data"))) {
                        resetAutoPickChamp();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resetAutoPickChamp() {
        saveDataa = new ArrayList<Object>();
        saveDataa.add(0, -1);
        saveDataa.add(1,false); //alreadyInitialized
        saveDataa.add(2, false); //alreadyPicked
        if(args.length > 1) {
            saveDataa.add(3, args[1]); //alreadyHovered
        }
    }

    private synchronized void updateAutoAcceptQueue(JSONArray array) {
        try {
            JSONArray outerArray = array;
            if("OnJsonApiEvent_lol-gameflow_v1_gameflow-phase".equals(outerArray.getString(1))) {
                JSONObject outerObject = outerArray.getJSONObject(2);
                if("Update".equals(outerObject.getString("eventType"))) {
                    String data = outerObject.getString("data");
                    currentState = state.getByName(data);
                    if(currentState == null) {
                        return;
                    }
                    switch (currentState) {
                        case LOBBY -> {
                            //TODO: Reset Upon Dodge! -> Taskabhängig!
                        }
                        case READY_CHECK -> {
                            acceptReadyCheck();
                        }
                        case GAME_START -> {
                        }
                        default -> {}
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void pickChamp(JSONObject information, int id) {
        try {
                System.out.println("Should be picking!");
                boolean success = mainInitiator.getConnectionManager().buildPatchRequest("/lol-champ-select/v1/session/actions/"+id, information.toString());
                if (success) {
                    HttpURLConnection con = mainInitiator.connectionManager.buildConnection(ConnectionManager.conOptions.POST, "/lol-champ-select/v1/session/actions/"+id+"/complete" , "{}");
                    con.getResponseCode();
                    con.disconnect();
                    this.saveDataa.set(2, true);
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hoverChamp(JSONObject information, int id) {
        try {
            System.out.println("Should be hovering");
            boolean success = mainInitiator.getConnectionManager().buildPatchRequest("/lol-champ-select/v1/session/actions/"+id, information.toString());
            if (success) {
                this.saveDataa.set(3,true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void acceptReadyCheck() {
        try {
            HttpURLConnection con = mainInitiator.getConnectionManager().buildConnection(ConnectionManager.conOptions.POST,"/lol-matchmaking/v1/ready-check/accept","{}");
            con.getResponseCode();
            con.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
