package com.example.todoapp;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Data Access Object for {@link Task} model.
 */
public class TaskDao {

    private final Map<Integer, Task> storage = new HashMap<>();

    {
        save(new Task(1, "Réviser DS de maths", "Séries numériques et probabilités.", false));
        save(new Task(2, "Valider mon PIVE", "PIVE Club Poker.", true));
        save(new Task(3, "Choisir mon parcours de 4A", "SIR ou SIA ?", false));
    }

    /**
     * Persist {@link Task} model.
     * @param task task to save.
     * @return task model.
     */
    public Task save(Task task) {
        storage.put(task.id(), task);
        return task;
    }

    /**
     * Retrieve {@link Task} model by id.
     * @param id identifier of the {@link Task}.
     * @return {@link Task} model wrapped by Optional.
     */
    public Optional<Task> findById(int id) {
        return Optional.ofNullable(storage.get(id));
    }

    public java.util.Collection<Task> findAll(boolean todoOnly) {
        if (todoOnly) {
            java.util.List<Task> filteredTasks = new java.util.ArrayList<>();
            for (Task task : storage.values()) {
                if (!task.done()) {
                    filteredTasks.add(task);
                }
            }
            return filteredTasks;
        }
        return storage.values();
    }

    public boolean deleteById(int id) {
        Task removed = storage.remove(id);
        return removed != null;
    }

    public boolean update(int id, Task updatedTask) {
        if (!storage.containsKey(id)) {
            return false;
        }
        storage.put(id, updatedTask);
        return true;
    }
}