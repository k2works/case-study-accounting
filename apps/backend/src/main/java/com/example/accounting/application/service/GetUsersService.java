package com.example.accounting.application.service;

import com.example.accounting.application.port.in.GetUsersUseCase;
import com.example.accounting.application.port.in.query.GetUsersQuery;
import com.example.accounting.application.port.in.query.UserSummary;
import com.example.accounting.application.port.out.UserRepository;
import com.example.accounting.domain.model.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetUsersService implements GetUsersUseCase {

    private final UserRepository userRepository;

    @Override
    public List<UserSummary> execute(GetUsersQuery query) {
        List<User> users;
        if (query.role() == null && query.keyword() == null) {
            users = userRepository.findAll();
        } else {
            users = userRepository.search(query.role(), query.keyword());
        }
        return users.stream()
                .map(UserSummary::from)
                .toList();
    }
}
