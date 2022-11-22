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

    public synchronized void removeTask(Task.tasks task) {
        allTasks.remove(task);
    }

    public synchronized void createTask(Task.tasks task, Object args) {
        if(allTasks.containsKey(task)) {
            return;
        }
        Task newTask = new Task(task, this.mainInitiator, args);
        allTasks.put(task, newTask);
    }

    public void createTask(Task.tasks task) {
        createTask(task, null);
    }

    public void update(JSONArray jsonArray) {
        if(!allTasks.isEmpty())
        allTasks.forEach((k,v) -> {
            v.update(jsonArray);
        });
    }
}
