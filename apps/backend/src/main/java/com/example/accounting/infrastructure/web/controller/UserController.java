package com.example.accounting.infrastructure.web.controller;

import com.example.accounting.application.port.in.GetUsersUseCase;
import com.example.accounting.application.port.in.query.GetUsersQuery;
import com.example.accounting.application.port.in.query.UserSummary;
import com.example.accounting.infrastructure.web.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "ユーザー管理", description = "ユーザー管理に関する API")
@RequiredArgsConstructor
public class UserController {

    private final GetUsersUseCase getUsersUseCase;

    @Operation(summary = "ユーザー一覧取得", description = "管理者がユーザー一覧を取得します")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String keyword) {
        GetUsersQuery query = new GetUsersQuery(role, keyword);
        List<UserSummary> summaries = getUsersUseCase.execute(query);
        List<UserResponse> responses = summaries.stream()
                .map(UserResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }
}
