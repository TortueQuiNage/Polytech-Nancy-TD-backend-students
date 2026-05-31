package com.example.todoapp.business.service;

import com.example.todoapp.Task;
import com.example.todoapp.dao.TaskDao;
import com.example.todoapp.presentation.dto.TaskCreateDto;
import com.example.todoapp.presentation.dto.TaskUpdateDto;

import java.util.Collection;
import java.util.Optional;

public class TaskService {

    private final TaskDao dao = new TaskDao();

    public Task createTask(TaskCreateDto dto) {

        if (dto.title() == null || dto.title().isBlank()) {
            throw new ValidationException("title", "Le titre est obligatoire.");
        }
        if (dto.title().length() > 50) {
            throw new ValidationException("title", "Le titre ne doit pas dépasser 50 caractères.");
        }


        if (dto.description() != null && dto.description().length() > 255) {
            throw new ValidationException("description", "La description ne doit pas dépasser 255 caractères.");
        }

        Task taskToCreate = new Task(0, dto.title(), dto.description(), false);
        return dao.save(taskToCreate);
    }

    public boolean updateTask(int id, TaskUpdateDto dto) {
        // Validation du titre
        if (dto.title() == null || dto.title().isBlank()) {
            throw new ValidationException("title", "Le titre est obligatoire.");
        }
        if (dto.title().length() > 50) {
            throw new ValidationException("title", "Le titre ne doit pas dépasser 50 caractères.");
        }


        if (dto.description() != null && dto.description().length() > 255) {
            throw new ValidationException("description", "La description ne doit pas dépasser 255 caractères.");
        }

        Task input = new Task(id, dto.title(), dto.description(), dto.done());
        return dao.update(id, input);
    }

    public Collection<Task> getAllTasks(boolean todoOnly) {
        return dao.findAll(todoOnly);
    }

    public Optional<Task> getTaskById(int id) {
        return dao.findById(id);
    }

    public boolean deleteTask(int id) {
        return dao.deleteById(id);
    }
}