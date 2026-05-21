package com.melodev.storeservice.controller;

import com.melodev.storeservice.controller.requests.UpdateNameRequest;
import com.melodev.storeservice.model.User;
import com.melodev.storeservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PutMapping("/name")
    public Mono<User> updateUsername(@RequestBody UpdateNameRequest request) {
        return userService.updateUsername(request.getName());
    }
}
