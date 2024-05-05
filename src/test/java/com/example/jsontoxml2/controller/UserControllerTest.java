package com.example.jsontoxml2.controller;

import com.example.jsontoxml2.Main;
import com.example.jsontoxml2.model.dto.user.UserInfoDto;
import com.example.jsontoxml2.model.dto.user.UserSaveDto;
import com.example.jsontoxml2.model.entity.User;
import com.example.jsontoxml2.repository.user.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest(
        properties = { "spring.liquibase.enabled=false" },
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

    private static User user;

    @BeforeEach
    void setUp() {
        UserSaveDto userSaveDto = new UserSaveDto("john doe",  "john.doe@example.com");
        User newUser = modelMapper.map(userSaveDto, User.class);
        user = userRepository.save(newUser);
    }

    @AfterEach
    public void afterEEach() {
        userRepository.deleteAll();
    }

    @Test
    void testGetAllUsers_WhenOneUserExist_ThanReturnListOfOneUser() throws Exception {
        // When
        MvcResult mvcResult = mvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();


        List<UserInfoDto> users = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                new TypeReference<List<UserInfoDto>>() {}
        );

        // Then
        assertFalse(users.isEmpty());
        assertEquals("john doe", users.get(0).getUsername());
        assertEquals("john.doe@example.com", users.get(0).getEmail());
    }

    @Test
    void testAddUserIsCreated() throws Exception {
        // Given
        UserSaveDto userSaveDto = new UserSaveDto("jane smith", "jane.smith@example.com");

        // When
        MvcResult mvcResult = mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userSaveDto)))
                .andExpect(status().isCreated())
                .andReturn();

        // Then
        UserInfoDto savedUserFromDb = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                UserInfoDto.class);

        assertThat(savedUserFromDb.getId()).isGreaterThanOrEqualTo(1);
        assertEquals(userSaveDto.getUsername(), savedUserFromDb.getUsername());
        assertEquals(userSaveDto.getEmail(), savedUserFromDb.getEmail());
    }

    @Test
    void testAddUser_WhenUserDataNotUnique_ThanBadRequest() throws Exception {
        // Given
        UserSaveDto userSaveDto = new UserSaveDto("john doe", "not.john.doe@example.com");

        // When/Then
        mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userSaveDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateUser() throws Exception {
        // Given
        UserSaveDto updatedUserSaveDto = new UserSaveDto("jane smith", "jane.smith@example.com");

        // When
        mvc.perform(put("/api/v1/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUserSaveDto)))
                .andExpect(status().isOk());

        // Then
        User updatedUser = userRepository.findById(user.getId()).orElse(null);

        assertNotNull(updatedUser);
        assertEquals(updatedUserSaveDto.getUsername(), updatedUser.getUsername());
        assertEquals(updatedUserSaveDto.getEmail(), updatedUser.getEmail());
    }

    @Test
    void testUpdateUser_WhenUserDataNotUnique_ThanBadRequest() throws Exception {
        // Given
        UserSaveDto userSaveDto = new UserSaveDto("jane smith",  "jane.smith@example.com");
        User newUser = modelMapper.map(userSaveDto, User.class);
        userRepository.save(newUser);

        UserSaveDto updatedUserSaveDto = new UserSaveDto("jane smith", "jane.smith@example.com");

        // When/Then
        mvc.perform(put("/api/v1/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUserSaveDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateUserNotFound() throws Exception {
        // Given
        long nonExistentId = 9999999L;

        UserSaveDto updatedUserSaveDto = new UserSaveDto("jane smith", "jane.smith@example.com");

        // When/Then
        mvc.perform(put("/api/v1/users/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUserSaveDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.dateTime").exists())
                .andExpect(jsonPath("$.errorMessage").value("An error occurred: User not found with id: " + nonExistentId));

    }

    @Test
    void testDeleteUserByIdIsOk() throws Exception {
        // When
        mvc.perform(delete("/api/v1/users/{id}", user.getId()))
                .andExpect(status().isOk());

        // Then
        Optional<User> deletedUser = userRepository.findById(user.getId());
        assertThat(deletedUser).isNotPresent();
    }

    @Test
    public void testDeleteUserNotFound() throws Exception {
        // Given
        long nonExistentId = 9999999L;

        // When/Then
        mvc.perform(delete("/api/v1/users/{id}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.dateTime").exists())
                .andExpect(jsonPath("$.errorMessage").value("An error occurred: User not found with id: " + nonExistentId));
    }

}
