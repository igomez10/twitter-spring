package com.ignacio.twitter.services;

import com.ignacio.twitter.dto.UserRequest;
import com.ignacio.twitter.auth.AuthenticatedUser;
import com.ignacio.twitter.models.Event;
import com.ignacio.twitter.models.EventType;
import com.ignacio.twitter.models.User;
import com.ignacio.twitter.models.UserCredential;
import com.ignacio.twitter.repositories.EventRepository;
import com.ignacio.twitter.repositories.RoleRepository;
import com.ignacio.twitter.repositories.UserCredentialRepository;
import com.ignacio.twitter.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String ENTITY_TYPE_USER = "user";

    private final UserRepository repository;
    private final UserCredentialRepository userCredentialRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EventRepository eventRepository;

    public List<User> listUsers() {
        return repository.findAllByDeletedAtIsNull();
    }

    public User getUser(Long id) {
        return repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @Transactional
    public User createUser(UserRequest request) {
        Long actorUserId = resolveActorUserId();
        validateCredentials(request);
        ensureUniqueOnCreate(request);
        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .handle(request.handle())
                .build();
        User created = repository.save(user);
        UserCredential credential = buildCredential(created, request.username(), request.password());
        userCredentialRepository.save(credential);
        assignDefaultRole(created);
        eventRepository.save(buildEvent(EventType.USER_CREATED, created.getId(), actorUserId));
        return created;
    }

    @Transactional
    public User updateUser(Long id, UserRequest request) {
        Long actorUserId = resolveActorUserId();
        User user = getUser(id);
        ensureUniqueOnUpdate(user, request);
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setHandle(request.handle());
        User updated = repository.save(user);
        UserCredential credential = userCredentialRepository.findByUserId(updated.getId())
                .orElseGet(() -> UserCredential.builder().userId(updated.getId()).build());
        credential.setUsername(request.username());
        String hashed = passwordEncoder.encode(request.password());
        credential.setPasswordHash(hashed);
        credential.setPasswordSalt(extractSalt(hashed));
        userCredentialRepository.save(credential);
        eventRepository.save(buildEvent(EventType.USER_UPDATED, updated.getId(), actorUserId));
        return updated;
    }

    @Transactional
    public void deleteUser(Long id) {
        Long actorUserId = resolveActorUserId();
        User user = getUser(id);
        user.setDeletedAt(LocalDateTime.now());
        repository.save(user);
        eventRepository.save(buildEvent(EventType.USER_DELETED, user.getId(), actorUserId));
    }

    private void validateCredentials(UserRequest request) {
        if (request.username() == null || request.username().isBlank()
                || request.password() == null || request.password().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username and password are required");
        }
    }

    private void ensureUniqueOnCreate(UserRequest request) {
        if (repository.findByHandle(request.handle()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Handle already exists");
        }
        if (repository.findByEmail(request.email()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }
        if (userCredentialRepository.findByUsername(request.username()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
    }

    private void ensureUniqueOnUpdate(User user, UserRequest request) {
        if (!user.getHandle().equals(request.handle())) {
            repository.findByHandle(request.handle())
                    .filter(existing -> !existing.getId().equals(user.getId()))
                    .ifPresent(existing -> {
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "Handle already exists");
                    });
        }
        if (!user.getEmail().equals(request.email())) {
            repository.findByEmail(request.email())
                    .filter(existing -> !existing.getId().equals(user.getId()))
                    .ifPresent(existing -> {
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
                    });
        }
        userCredentialRepository.findByUsername(request.username())
                .filter(existing -> !existing.getUserId().equals(user.getId()))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
                });
    }

    private UserCredential buildCredential(User user, String username, String password) {
        String hashed = passwordEncoder.encode(password);
        return UserCredential.builder()
                .userId(user.getId())
                .username(username)
                .passwordHash(hashed)
                .passwordSalt(extractSalt(hashed))
                .build();
    }

    private Event buildEvent(EventType eventType, Long entityId, Long actorUserId) {
        return Event.builder()
                .eventType(eventType)
                .entityType(ENTITY_TYPE_USER)
                .entityId(entityId)
                .actorUserId(actorUserId)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private Long resolveActorUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthenticatedUser authenticatedUser) {
            return authenticatedUser.userId();
        }
        return null;
    }

    private String extractSalt(String hash) {
        if (hash == null) {
            return "";
        }
        return hash.length() >= 29 ? hash.substring(0, 29) : hash;
    }

    private void assignDefaultRole(User user) {
        roleRepository.findByName("basic").ifPresent(role -> user.getRoles().add(role));
    }
}
