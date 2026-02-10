package com.ignacio.twitter.services;

import com.ignacio.twitter.dto.UserRequest;
import com.ignacio.twitter.models.User;
import com.ignacio.twitter.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void listUsers_returnsNonDeleted() {
        User user = User.builder().id(1L).email("a@b.com").handle("ab").build();
        when(userRepository.findAllByDeletedAtIsNull()).thenReturn(List.of(user));

        List<User> users = userService.listUsers();

        assertThat(users).hasSize(1);
        verify(userRepository).findAllByDeletedAtIsNull();
    }

    @Test
    void getUser_returnsUser() {
        User user = User.builder().id(2L).email("a@b.com").handle("ab").build();
        when(userRepository.findByIdAndDeletedAtIsNull(2L)).thenReturn(Optional.of(user));

        User result = userService.getUser(2L);

        assertThat(result.getId()).isEqualTo(2L);
    }

    @Test
    void getUser_missing_throws() {
        when(userRepository.findByIdAndDeletedAtIsNull(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUser(3L))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void createUser_savesUser() {
        UserRequest request = new UserRequest("Ignacio", "Gomez", "ignacio@gomez.com", "nachogomez");
        when(userRepository.save(org.mockito.ArgumentMatchers.any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User created = userService.createUser(request);

        assertThat(created.getEmail()).isEqualTo("ignacio@gomez.com");
    }

    @Test
    void updateUser_updatesFields() {
        User existing = User.builder().id(4L).email("old@x.com").handle("old").build();
        when(userRepository.findByIdAndDeletedAtIsNull(4L)).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);

        UserRequest request = new UserRequest("New", "Name", "new@x.com", "new");
        User updated = userService.updateUser(4L, request);

        assertThat(updated.getEmail()).isEqualTo("new@x.com");
        assertThat(updated.getHandle()).isEqualTo("new");
    }

    @Test
    void deleteUser_setsDeletedAt() {
        User existing = User.builder().id(5L).email("a@b.com").handle("ab").build();
        when(userRepository.findByIdAndDeletedAtIsNull(5L)).thenReturn(Optional.of(existing));

        userService.deleteUser(5L);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        LocalDateTime deletedAt = captor.getValue().getDeletedAt();
        assertThat(deletedAt).isNotNull();
    }
}
