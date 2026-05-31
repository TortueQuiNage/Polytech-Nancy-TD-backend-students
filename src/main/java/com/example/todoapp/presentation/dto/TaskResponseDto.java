package com.example.todoapp.presentation.dto;

public record TaskResponseDto(int id, String title, String description, boolean done) {
}