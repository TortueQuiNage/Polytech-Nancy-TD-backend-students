package com.example.todoapp;

import com.example.todoapp.presentation.StudentController;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * Main class of the application. Managing routing and HTTP layer.
 */
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // On redirige tout vers notre couche présentation
        server.createContext("/tasks", StudentController::handleTasks);

        server.setExecutor(null);
        server.start();
        log.info("HTTP server started on http://localhost:8080");
    }
}