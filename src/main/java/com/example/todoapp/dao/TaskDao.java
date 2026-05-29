package com.example.todoapp.dao;

import com.example.todoapp.Task;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.Optional;

public class TaskDao {

    private static final String DB_URL = "jdbc:sqlite:tasks.db";

    public TaskDao() {
        // Au démarrage, on crée la table si elle n'existe pas
        String createTableSQL = """
                CREATE TABLE IF NOT EXISTS tasks (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    description TEXT,
                    done INTEGER NOT NULL DEFAULT 0
                );
                """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);

            // Si la base est totalement vide, on insère le jeu de données initial du TD
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM tasks;");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.execute("INSERT INTO tasks (id, title, description, done) VALUES (1, 'Réviser DS de maths', 'Séries numériques et probabilités.', 0);");
                stmt.execute("INSERT INTO tasks (id, title, description, done) VALUES (2, 'Valider mon PIVE', 'PIVE Club Poker.', 1);");
                stmt.execute("INSERT INTO tasks (id, title, description, done) VALUES (3, 'Choisir mon parcours de 4A', 'SIR ou SIA ?', 0);");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Impossible d'initialiser la base de données SQLite", e);
        }
    }

    public Task save(Task task) {
        // On ne mentionne PAS l'id dans le INSERT pour laisser SQLite l'auto-générer
        String sql = "INSERT INTO tasks (title, description, done) VALUES (?, ?, ?);";

        // Le "Statement.RETURN_GENERATED_KEYS" permet de demander à SQLite quel ID il a choisi
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, task.title());
            ps.setString(2, task.description());
            ps.setInt(3, task.done() ? 1 : 0);

            ps.executeUpdate();

            // On récupère l'ID généré par SQLite
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    // On renvoie une nouvelle Task avec le VRAI ID de la base de données
                    return new Task(generatedId, task.title(), task.description(), task.done());
                }
            }
            return task;
        } catch (SQLException e) {
            throw new RuntimeException("Échec du save de la tâche", e);
        }
    }

    public Optional<Task> findById(int id) {
        String sql = "SELECT id, title, description, done FROM tasks WHERE id = ?;";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Task task = new Task(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getInt("done") == 1 // Convertit l'entier 1 en boolean true
                    );
                    return Optional.of(task);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Échec de la recherche de la tâche " + id, e);
        }
        return Optional.empty();
    }

    public Collection<Task> findAll(boolean todoOnly) {
        List<Task> tasks = new ArrayList<>();
        // Si todoOnly est vrai, on ajoute un filtre WHERE done = 0
        String sql = todoOnly ?
                "SELECT id, title, description, done FROM tasks WHERE done = 0;" :
                "SELECT id, title, description, done FROM tasks;";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                tasks.add(new Task(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getInt("done") == 1
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Échec de la récupération des tâches", e);
        }
        return tasks;
    }

    public boolean deleteById(int id) {
        String sql = "DELETE FROM tasks WHERE id = ?;";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            int rowsDeleted = ps.executeUpdate();
            return rowsDeleted > 0; // Renvoie true si une ligne a bien été supprimée
        } catch (SQLException e) {
            throw new RuntimeException("Échec de la suppression de la tâche " + id, e);
        }
    }

    public boolean update(int id, Task updatedTask) {
        String sql = "UPDATE tasks SET title = ?, description = ?, done = ? WHERE id = ?;";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, updatedTask.title());
            ps.setString(2, updatedTask.description());
            ps.setInt(3, updatedTask.done() ? 1 : 0);
            ps.setInt(4, id);

            int rowsUpdated = ps.executeUpdate();
            return rowsUpdated > 0; // Renvoie true si la tâche existait et a été mise à jour
        } catch (SQLException e) {
            throw new RuntimeException("Échec de la mise à jour de la tâche " + id, e);
        }
    }
}