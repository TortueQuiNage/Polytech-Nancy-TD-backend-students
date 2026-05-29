package com.example.todoapp.presentation;

import com.example.todoapp.JsonUtils;
import com.example.todoapp.Task;
import com.example.todoapp.dao.TaskDao;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.nonNull;

public class StudentController {

    private static final Pattern ID_PATH = Pattern.compile("^/tasks/([0-9]+)$");
    private static final TaskDao dao = new TaskDao();

    public static void handleTasks(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        Matcher m = ID_PATH.matcher(path);

        if ("POST".equals(method) && "/tasks".equals(path)) {
            // 1. On lit le JSON reçu sous forme de TaskCreateDto au lieu de Task
            com.example.todoapp.presentation.dto.TaskCreateDto dto = JsonUtils.deserialize(
                    new String(exchange.getRequestBody().readAllBytes(), UTF_8),
                    com.example.todoapp.presentation.dto.TaskCreateDto.class
            );

            // 2. Validation du titre obligatoire et taille max 50 caractères
            if (dto.title() == null || dto.title().isBlank()) {
                com.example.todoapp.presentation.dto.ValidationErrorDto error =
                        new com.example.todoapp.presentation.dto.ValidationErrorDto("title", "Le titre est obligatoire.");
                sendResponse(exchange, 400, JsonUtils.serialize(error));
                return;
            }
            if (dto.title().length() > 50) {
                com.example.todoapp.presentation.dto.ValidationErrorDto error =
                        new com.example.todoapp.presentation.dto.ValidationErrorDto("title", "Le titre ne doit pas dépasser 50 caractères.");
                sendResponse(exchange, 400, JsonUtils.serialize(error));
                return;
            }

            // 3. Validation de la description max 255 caractères
            if (dto.description() != null && dto.description().length() > 255) {
                com.example.todoapp.presentation.dto.ValidationErrorDto error =
                        new com.example.todoapp.presentation.dto.ValidationErrorDto("description", "La description ne doit pas dépasser 255 caractères.");
                sendResponse(exchange, 400, JsonUtils.serialize(error));
                return;
            }

            // 4. Si c'est valide, on crée l'entité avec id=0 (géré par SQLite) et done=false
            Task taskToCreate = new Task(0, dto.title(), dto.description(), false);
            Task createdTask = dao.save(taskToCreate);

            exchange.getResponseHeaders().add("Location", "/tasks/" + createdTask.id());
            sendResponse(exchange, 201, JsonUtils.serialize(createdTask));
            return;
        }

        if ("GET".equals(method) && "/tasks".equals(path)) {
            String query = exchange.getRequestURI().getQuery();
            boolean todoOnly = nonNull(query) && query.contains("todo-only=true");

            java.util.Collection<Task> tasks = dao.findAll(todoOnly);

            if (tasks.isEmpty()) {
                sendResponse(exchange, 204, null);
            } else {
                sendResponse(exchange, 200, JsonUtils.serialize(tasks));
            }
            return;
        }

        if ("GET".equals(method) && m.matches()) {
            int id = Integer.parseInt(m.group(1));
            Optional<Task> task = dao.findById(id);

            if (task.isPresent()) {
                sendResponse(exchange, 200, JsonUtils.serialize(task.get()));
            } else {
                sendResponse(exchange, 404, null);
            }
            return;
        }

        if ("DELETE".equals(method) && m.matches()) {
            int id = Integer.parseInt(m.group(1));
            boolean deleted = dao.deleteById(id);

            if (deleted) {
                sendResponse(exchange, 204, null);
            } else {
                sendResponse(exchange, 404, null);
            }
            return;
        }

        if ("PUT".equals(method) && m.matches()) {
            int id = Integer.parseInt(m.group(1));
            Task input = JsonUtils.deserialize(new String(exchange.getRequestBody().readAllBytes(), UTF_8), Task.class);
            boolean updated = dao.update(id, input);

            if (updated) {
                sendResponse(exchange, 204, null);
            } else {
                sendResponse(exchange, 404, null);
            }
            return;
        }

        sendResponse(exchange, 404, null);
    }

    private static void sendResponse(HttpExchange exchange, int status, String json) throws IOException {
        if(nonNull(json)) {
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            byte[] bytes = json.getBytes(UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } else {
            exchange.sendResponseHeaders(status, 0);
            exchange.close();
        }
    }
}