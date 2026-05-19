package com.melodev.storeservice.service;

import com.melodev.storeservice.model.User;
import com.melodev.storeservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static com.melodev.storeservice.config.ContextUser.USER_DEFAULT_ID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;

    public Mono<User> me() {
        return getUser(USER_DEFAULT_ID);
    }

    public Mono<User> getUser(String userId) {
        return userRepository.findById(userId);
    }
}
