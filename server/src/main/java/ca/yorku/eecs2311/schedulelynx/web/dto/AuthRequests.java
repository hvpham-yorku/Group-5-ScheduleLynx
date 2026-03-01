package ca.yorku.eecs2311.schedulelynx.web.dto;

import jakarta.validation.constraints.NotBlank;

record RegisterRequest(@NotBlank String username, @NotBlank String password) {}

record LoginRequest(@NotBlank String username, @NotBlank String password) {}

record MeResponse(Long id, String username) {}
