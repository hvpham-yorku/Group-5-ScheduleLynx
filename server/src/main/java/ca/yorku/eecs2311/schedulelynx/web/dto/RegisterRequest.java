package ca.yorku.eecs2311.schedulelynx.web.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(@NotBlank String username,
                              @NotBlank String password) {}
