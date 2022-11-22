import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;

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
    private Object args;
    private state currentState;
    private Boolean alreadyInitialized = false;
    private Object saveData;

    public Task(tasks task, MainInitiator mainInitiator, Object args) {
        this.task = task;
        this.mainInitiator = mainInitiator;
        this.args = args;
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
            default -> {

            }
        }
    }

    private synchronized void updateAutoPickChamp(JSONArray array) {
        try {
            Integer actionId = null;
            JSONArray outerArray = array;
            if("OnJsonApiEvent_lol-champ-select_v1_session".equals(outerArray.getString(1))) { // Eigentlich nur bei Init nach Pick-Order schauen, dann ob bei actions, ally action mit der summoner ID in progress ist
                JSONObject outerObject = outerArray.getJSONObject(2);
                if (!alreadyInitialized) {
                    System.out.println("Init localPlayerCellId");
                    this.saveData = outerObject.getJSONObject("data").getInt("localPlayerCellId");
                    alreadyInitialized = true;
                }
                JSONArray actionArray = outerObject.getJSONObject("data").getJSONArray("actions");
                System.out.println(actionArray.toString());
                outer: for(int i = 2; i < actionArray.length(); i++) {
                    JSONArray actionSubArray = actionArray.getJSONArray(i);
                    for (int j = 0; j < actionSubArray.length(); j++) {
                        JSONObject action = actionSubArray.getJSONObject(j);
                        if(action.getInt("actorCellId") == (int) saveData) { // CellIdFrom my Team
                            System.out.println("Found ActorCell! "+ saveData);
                            if(action.getBoolean("isAllyAction") && action.getBoolean("isInProgress") && "pick".equals(action.getString("type"))) {
                                //TODO: Stop input for any other, reset upon dodge!
                                actionId = action.getInt("id");
                                action.put("championId", (int) args); //Put ChampionId here!
                                action.remove("pickTurn");
                                pickChamp(action, actionId);
                                break outer;
                            }
                        } else break;
                    }
                }
            }
            if("OnJsonApiEvent_lol-gameflow_v1_gameflow-phase".equals(outerArray.getString(1))) {
                JSONObject outerObject = outerArray.getJSONObject(2);
                if("Update".equals(outerObject.getString("eventType"))) {
                    if("lobby".equals(outerObject.getString("data"))) {
                        resetAutoPickChamp();
                    }
                    if("None".equals(outerObject.getString("data"))||"GameStart".equals(outerObject.getString("data"))) {
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resetAutoPickChamp() {
        alreadyInitialized = false;
    }

    private synchronized void updateAutoAcceptQueue(JSONArray array) {
        try {
            JSONArray outerArray = array;
            if("OnJsonApiEvent_lol-gameflow_v1_gameflow-phase".equals(outerArray.getString(1))) {
                JSONObject outerObject = outerArray.getJSONObject(2);
                if("Update".equals(outerObject.getString("eventType"))) {
                    String data = outerObject.getString("data");
                    currentState = state.getByName(data);
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
