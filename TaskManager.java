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
        if(allTasks.containsKey(task)) {
            System.out.println("Removed Task: " + task.name());
            allTasks.remove(task);
        }
    }

    public synchronized void createTask(Task.tasks task, Object[] args) {
        if(allTasks.containsKey(task)) {
            return;
        }
        System.out.println("Created Task:" + task.name());
        Task newTask = new Task(task, this.mainInitiator, args);
        allTasks.put(task, newTask);
    }

    public synchronized void createTask(Task.tasks task) {
        createTask(task, null);
    }

    public synchronized void updateTask(Task.tasks task, Object[] args) {
        if(allTasks.containsKey(task)) {
            System.out.println("Updated Task " + task.name() + " to: " + args[0]);
            Task t = new Task(task,this.mainInitiator, args);
            allTasks.put(task,t );
        }
    }

    public void update(JSONArray jsonArray) {
        if(!allTasks.isEmpty())
        allTasks.forEach((k,v) -> {
            v.update(jsonArray);
        });
    }
}
