import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class TaskManager {

    MainInitiator mainInitiator = null;
    HashMap<Task.tasks, Task> allTasks = null;

    public TaskManager(MainInitiator mainInitiator) {
        this.mainInitiator = mainInitiator;
        allTasks = new HashMap<>();
    }



    public void createTask(Task.tasks task, MainInitiator mainInitiator, Object args) {
        if(allTasks.containsKey(task)) {
            return;
        }
        Task newTask = new Task(task, mainInitiator, args);
        allTasks.put(task, newTask);
    }

    public void createTask(Task.tasks task, MainInitiator mainInitiator) {
        createTask(task, mainInitiator, null);
    }

    public void update(JSONArray jsonArray) {
        if(!allTasks.isEmpty())
        allTasks.forEach((k,v) -> {
            v.update(jsonArray);
        });
    }
}
