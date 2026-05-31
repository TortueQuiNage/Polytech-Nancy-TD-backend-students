package com.example.todoapp.presentation;

import com.example.todoapp.JsonUtils;
import com.example.todoapp.Task;
import com.example.todoapp.business.service.TaskService;
import com.example.todoapp.business.service.ValidationException;
import com.example.todoapp.presentation.dto.ValidationErrorDto;
import com.example.todoapp.presentation.dto.TaskCreateDto;
import com.example.todoapp.presentation.dto.TaskUpdateDto;
import com.example.todoapp.presentation.dto.TaskResponseDto;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.nonNull;

public class TaskController {

    private static final Pattern ID_PATH = Pattern.compile("^/tasks/([0-9]+)$");
    private static final TaskService service = new TaskService();
    public static void handleTasks(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        Matcher m = ID_PATH.matcher(path);

        try {
            // --- POST /tasks ---
            if ("POST".equals(method) && "/tasks".equals(path)) {
                TaskCreateDto dto = JsonUtils.deserialize(
                        new String(exchange.getRequestBody().readAllBytes(), UTF_8),
                        TaskCreateDto.class
                );

                Task createdTask = service.createTask(dto);

                exchange.getResponseHeaders().add("Location", "/tasks/" + createdTask.id());

                TaskResponseDto response = new TaskResponseDto(createdTask.id(), createdTask.title(), createdTask.description(), createdTask.done());
                sendResponse(exchange, 201, JsonUtils.serialize(response));
                return;
            }

            // --- GET /tasks (All) ---
            if ("GET".equals(method) && "/tasks".equals(path)) {
                String query = exchange.getRequestURI().getQuery();
                boolean todoOnly = nonNull(query) && query.contains("todo-only=true");

                java.util.Collection<Task> tasks = service.getAllTasks(todoOnly);

                if (tasks.isEmpty()) {
                    sendResponse(exchange, 204, null);
                } else {
                    java.util.List<TaskResponseDto> dtos = tasks.stream()
                            .map(t -> new TaskResponseDto(t.id(), t.title(), t.description(), t.done()))
                            .toList();
                    sendResponse(exchange, 200, JsonUtils.serialize(dtos));
                }
                return;
            }

            // --- GET /tasks/{id} ---
            if ("GET".equals(method) && m.matches()) {
                int id = Integer.parseInt(m.group(1));
                Optional<Task> task = service.getTaskById(id);

                if (task.isPresent()) {
                    Task t = task.get();
                    TaskResponseDto dto = new TaskResponseDto(t.id(), t.title(), t.description(), t.done());
                    sendResponse(exchange, 200, JsonUtils.serialize(dto));
                } else {
                    sendResponse(exchange, 404, null);
                }
                return;
            }

            // --- DELETE /tasks/{id} ---
            if ("DELETE".equals(method) && m.matches()) {
                int id = Integer.parseInt(m.group(1));
                boolean deleted = service.deleteTask(id);

                if (deleted) {
                    sendResponse(exchange, 204, null);
                } else {
                    sendResponse(exchange, 404, null);
                }
                return;
            }

            // --- PUT /tasks/{id} ---
            if ("PUT".equals(method) && m.matches()) {
                int id = Integer.parseInt(m.group(1));

                TaskUpdateDto dto = JsonUtils.deserialize(
                        new String(exchange.getRequestBody().readAllBytes(), UTF_8),
                        TaskUpdateDto.class
                );

                boolean updated = service.updateTask(id, dto);

                if (updated) {
                    sendResponse(exchange, 204, null);
                } else {
                    sendResponse(exchange, 404, null);
                }
                return;
            }

            sendResponse(exchange, 404, null);

        } catch (com.example.todoapp.business.service.ValidationException e) {
            com.example.todoapp.presentation.dto.ValidationErrorDto errorDto =
                    new com.example.todoapp.presentation.dto.ValidationErrorDto(e.getField(), e.getMessage());
            sendResponse(exchange, 400, JsonUtils.serialize(errorDto));
        }
    }

    private static void sendResponse(HttpExchange exchange, int status, String json) throws IOException {
        if (nonNull(json)) {
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