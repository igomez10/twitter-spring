package com.ignacio.twitter.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ignacio.twitter.dto.UserRequest;
import com.ignacio.twitter.models.User;
import com.ignacio.twitter.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    void listUsers_returnsUsers() throws Exception {
        User user = User.builder()
                .id(1L)
                .firstName("Ignacio")
                .lastName("Gomez")
                .email("ignacio@gomez.com")
                .handle("nachogomez")
                .build();
        when(userService.listUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].email").value("ignacio@gomez.com"));
    }

    @Test
    void getUser_returnsUser() throws Exception {
        User user = User.builder()
                .id(2L)
                .firstName("Ada")
                .lastName("Lovelace")
                .email("ada@lovelace.com")
                .handle("adal")
                .build();
        when(userService.getUser(2L)).thenReturn(user);

        mockMvc.perform(get("/users/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.handle").value("adal"));
    }

    @Test
    void createUser_returnsCreatedUser() throws Exception {
        UserRequest request = new UserRequest("Ignacio", "Gomez", "ignacio@gomez.com", "nachogomez");
        User user = User.builder()
                .id(3L)
                .firstName("Ignacio")
                .lastName("Gomez")
                .email("ignacio@gomez.com")
                .handle("nachogomez")
                .build();
        when(userService.createUser(any(UserRequest.class))).thenReturn(user);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.email").value("ignacio@gomez.com"));
    }

    @Test
    void updateUser_returnsUpdatedUser() throws Exception {
        UserRequest request = new UserRequest("Ignacio", "Updated", "ignacio@gomez.com", "nachogomez");
        User user = User.builder()
                .id(4L)
                .firstName("Ignacio")
                .lastName("Updated")
                .email("ignacio@gomez.com")
                .handle("nachogomez")
                .build();
        when(userService.updateUser(eq(4L), any(UserRequest.class))).thenReturn(user);

        mockMvc.perform(put("/users/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("Updated"));
    }

    @Test
    void deleteUser_returnsNoContent() throws Exception {
        doNothing().when(userService).deleteUser(5L);

        mockMvc.perform(delete("/users/5"))
                .andExpect(status().isNoContent());
    }

    @Test
    void createUser_validationError() throws Exception {
        UserRequest request = new UserRequest("Ignacio", "Gomez", "", "");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
