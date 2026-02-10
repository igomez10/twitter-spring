package com.ignacio.twitter.services;

import com.ignacio.twitter.dto.UserRequest;
import com.ignacio.twitter.models.User;
import com.ignacio.twitter.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;

    public List<User> listUsers() {
        return repository.findAllByDeletedAtIsNull();
    }

    public User getUser(Long id) {
        return repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public User createUser(UserRequest request) {
        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .handle(request.handle())
                .build();
        return repository.save(user);
    }

    public User updateUser(Long id, UserRequest request) {
        User user = getUser(id);
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setHandle(request.handle());
        return repository.save(user);
    }

    public void deleteUser(Long id) {
        User user = getUser(id);
        user.setDeletedAt(LocalDateTime.now());
        repository.save(user);
    }
}
