package com.example.jsontoxml2.controller;

import com.example.jsontoxml2.Main;
import com.example.jsontoxml2.model.dto.user.UserInputDto;
import com.example.jsontoxml2.model.dto.user.UserWithoutPostsDto;
import com.example.jsontoxml2.model.entity.User;
import com.example.jsontoxml2.repository.user.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = Main.class)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ModelMapper modelMapper;

    @AfterEach
    public void afterEach() {
        userRepository.deleteAll();
    }

    @Test
    void testGetAllUsers() throws Exception {
        // Given
        User user1 = createUser("John Doe", "john.doe@example.com");
        User user2 = createUser("Jane Smith", "jane.smith@example.com");
        userRepository.saveAll(Arrays.asList(user1, user2));

        // When
        MvcResult mvcResult = mvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        List<UserWithoutPostsDto> users = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                new TypeReference<List<UserWithoutPostsDto>>() {}
        );

        assertEquals(2, users.size());
        assertEquals("John Doe", users.get(0).getUsername());
        assertEquals("john.doe@example.com", users.get(0).getEmail());
        assertEquals("Jane Smith", users.get(1).getUsername());
        assertEquals("jane.smith@example.com", users.get(1).getEmail());
    }

    private User createUser(String name, String email) {
        UserInputDto userInputDto = new UserInputDto(name, email);
        return modelMapper.map(userInputDto, User.class);
    }

    @Test
    void testAddUser() throws Exception {
        UserInputDto userInputDto = new UserInputDto("Samuel Doe", "samuel.doe@example.com");

        // When
        MvcResult mvcResult = mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userInputDto)))
                .andExpect(status().isCreated())
                .andReturn();

        // Then
        UserWithoutPostsDto savedUser = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                UserWithoutPostsDto.class);

        assertNotNull(savedUser.getId());
        assertEquals(userInputDto.getUsername(), savedUser.getUsername());
        assertEquals(userInputDto.getEmail(), savedUser.getEmail());
    }

    @Test
    void testDeleteUserById() throws Exception {
        // Given
        User user = createUser("John Doe", "john.doe@example.com");
        User savedUser = userRepository.save(user);

        // When
        mvc.perform(delete("/api/v1/users/{id}", savedUser.getId()))
                .andExpect(status().isOk());

        // Then
        Optional<User> deletedUser = userRepository.findById(savedUser.getId());
        assertFalse(deletedUser.isPresent());
    }

    @Test
    void testUpdateUser() throws Exception {
        // Given
        User user = createUser("John Doe", "john.doe@example.com");
        userRepository.save(user);

        UserInputDto updatedUserInputDto = new UserInputDto("Jane Smith", "jane.smith@example.com");

        // When
        mvc.perform(put("/api/v1/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUserInputDto)))
                .andExpect(status().isOk());

        // Then
        User updatedUser = userRepository.findById(user.getId()).orElse(null);
        assertNotNull(updatedUser);
        assertEquals(updatedUserInputDto.getUsername(), updatedUser.getUsername());
        assertEquals(updatedUserInputDto.getEmail(), updatedUser.getEmail());
    }

}
