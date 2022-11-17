import org.json.JSONArray;
import org.json.JSONObject;

public class UpdateAgent extends Thread{
    private MainInitiator mainInitiator;
    private String message;
    public UpdateAgent(MainInitiator mainInitiator) {
        this.mainInitiator = mainInitiator;
    }

    public void updateMessage(String s) {
        this.message = s;
    }

    public void run() {
        try {
            JSONArray jsonArray= new JSONArray(message);
            if(jsonArray != null) {
                mainInitiator.getTaskManager().update(jsonArray);
                mainInitiator.getGuiManager().update(jsonArray);
            }
        } catch (Exception e) {
            if(message!=null && !"".equals(message.trim())) {
                System.out.println("Error occured:" + message);
            }
        }
    }
}
