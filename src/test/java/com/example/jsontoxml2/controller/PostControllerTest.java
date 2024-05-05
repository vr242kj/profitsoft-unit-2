package com.example.jsontoxml2.controller;

import com.example.jsontoxml2.Main;
import com.example.jsontoxml2.model.dto.post.PostCreateRequestDto;
import com.example.jsontoxml2.model.dto.post.PostInfoDto;
import com.example.jsontoxml2.model.dto.post.PostUpdateRequestDto;
import com.example.jsontoxml2.model.dto.user.UserSaveDto;
import com.example.jsontoxml2.model.entity.Post;
import com.example.jsontoxml2.model.entity.User;
import com.example.jsontoxml2.repository.post.PostRepository;
import com.example.jsontoxml2.repository.user.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        properties = { "spring.liquibase.enabled=false" },
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = Main.class)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
class PostControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private static Long userId;

    @BeforeAll
    static void setup(@Autowired UserRepository userRepository, @Autowired ModelMapper modelMapper) {
        UserSaveDto userSaveDto = new UserSaveDto("Anna Lore", "anna.lore@example.com");
        User newUser = modelMapper.map(userSaveDto, User.class);
        User user = userRepository.save(newUser);
        userId = user.getId();
    }

    @AfterEach
    public void afterEach() {
        postRepository.deleteAll();
    }

    private Post createPost(String title, String content, Boolean isPublished, Long user) {
        PostCreateRequestDto postCreateRequestDto = new PostCreateRequestDto(title, content, isPublished, user);
        Post post = modelMapper.map(postCreateRequestDto, Post.class);
        return postRepository.save(post);
    }

    @Test
    void testGetPostByIdIsOk() throws Exception {
        // Given
        Post savedPostFromDb = createPost("Test Post", "This is a test post content.",
                true, userId);

        // When
        MvcResult mvcResult = mvc.perform(get("/api/v1/posts/{id}", savedPostFromDb.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        PostInfoDto returnedPost = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                PostInfoDto.class
        );

        assertEquals(savedPostFromDb.getId(), returnedPost.getId());
        assertEquals(savedPostFromDb.getTitle(), returnedPost.getTitle());
        assertEquals(savedPostFromDb.getContent(), returnedPost.getContent());
    }

    @Test
    public void testGetPostByIdNotFound() throws Exception {
        // Given
        long nonExistentId = 9999999L;

        // When/Then
        mvc.perform(get("/api/v1/posts/{id}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.dateTime").exists())
                .andExpect(jsonPath("$.errorMessage")
                        .value("An error occurred: Post not found with id: " + nonExistentId));
    }

    @Test
    void testSavePostIsCreated() throws Exception {
        // Given
        PostCreateRequestDto newPost = new PostCreateRequestDto("Test Post",
                "This is a test post content.", true, userId);

        // When
        MvcResult mvcResult = mvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newPost)))
                .andExpect(status().isCreated())
                .andReturn();

        // Then
        PostInfoDto returnedPost = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                PostInfoDto.class);

        assertThat(returnedPost.getId()).isGreaterThanOrEqualTo(1);
        assertEquals(newPost.getTitle(), returnedPost.getTitle());
        assertEquals(newPost.getContent(), returnedPost.getContent());
        assertEquals(newPost.getIsPublished(), returnedPost.getIsPublished());
        assertEquals(0, returnedPost.getLikesCount());
    }

    @Test
    void testSavePost_WhenPostNotValid_ThanBadRequest() throws Exception {
        // Given
        Long nonExistentId = 9999999L;
        PostCreateRequestDto newPost = new PostCreateRequestDto(null,
                "This is a test post content.", true, nonExistentId);

        // When/Than
        mvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newPost)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeletePostById() throws Exception {
        // Given
        Post savedPostFromDb = createPost("Test Post",
                "This is a test post content.", true, userId);

        // When
        mvc.perform(delete("/api/v1/posts/{id}", savedPostFromDb.getId()))
                .andExpect(status().isOk());

        // Then
        Optional<Post> deletedPost = postRepository.findById(savedPostFromDb.getId());
        assertFalse(deletedPost.isPresent());
    }

    @Test
    public void testDeletePostNotFound() throws Exception {
        // Given
        long nonExistentId = 9999999L;

        // When/Then
        mvc.perform(delete("/api/v1/posts/{id}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.dateTime").exists())
                .andExpect(jsonPath("$.errorMessage").value("An error occurred: Post not found with id: " + nonExistentId));
    }


    @Test
    void testUpdatePostIsOk() throws Exception {
        // Given
        Post savedPostFromDb = createPost("Test Post",
                "This is a test post content.", true, userId);

        PostUpdateRequestDto postUpdateRequestDto = new PostUpdateRequestDto("Updated Title",
                "Updated content", true, 1, userId);

        // When
        mvc.perform(put("/api/v1/posts/{id}", savedPostFromDb.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postUpdateRequestDto)))
                .andExpect(status().isOk());

        // Then
        Post updatedPost = postRepository.findById(savedPostFromDb.getId()).orElse(null);
        assertNotNull(updatedPost);
        assertThat(updatedPost.getId()).isGreaterThanOrEqualTo(1);
        assertEquals(postUpdateRequestDto.getTitle(), updatedPost.getTitle());
        assertEquals(postUpdateRequestDto.getContent(), updatedPost.getContent());
        assertEquals(postUpdateRequestDto.getIsPublished(), updatedPost.getIsPublished());
        assertEquals(postUpdateRequestDto.getLikesCount(), updatedPost.getLikesCount());
    }

    @Test
    void testUpdatePostNotFound() throws Exception {
        // Given
        PostUpdateRequestDto postUpdateRequestDto = new PostUpdateRequestDto("Updated Title",
                "Updated content", true, 1, userId);

        long nonExistentId = 9999999L;

        // When/Then
        mvc.perform(put("/api/v1/posts/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postUpdateRequestDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdatePost_WhenPostNotValid_ThanBadRequest() throws Exception {
        // Given
        Post savedPostFromDb = createPost("Test Post",
                "This is a test post content.", true, userId);

        PostUpdateRequestDto postUpdateRequestDto = new PostUpdateRequestDto(null,
                null, true, 1, userId);

        // When/Then
        mvc.perform(put("/api/v1/posts/{id}", savedPostFromDb.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postUpdateRequestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetPostList() throws Exception {
        // Given
        createPost("Title 1", "Content 1", true, userId);

        int page = 0;
        int size = 3;
        String body = """
                  {
                      "userId": %d,
                      "page": %d,
                      "size": %d
                  }
                """.formatted(userId, page, size);


        // When
        MvcResult mvcResult = mvc.perform(post("/api/v1/posts/_list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        Map<String, Object> responseMap = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<Map<String, Object>>() {
        });
        List<Map<String, Object>> posts = (List<Map<String, Object>>) responseMap.get("list");
        assertEquals(1, posts.size(), "Should return 1 published posts for the given user");

        List<String> postTitles = posts.stream().map(post -> (String) post.get("title")).toList();
        assertTrue(postTitles.contains("Title 1"), "Should contain post with title 'Title 1'");
    }

    @Test
    void testGetPostList_WhenPageIsNegative_isBadRequest() throws Exception {
        // Given
        int page = -1;
        int size = 1;
        String body = """
                  {
                      "userId": %d,
                      "page": %s,
                      "size": %d
                  }
                """.formatted(userId, page, size);

        // When/Then
        mvc.perform(post("/api/v1/posts/_list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetPostList_WhenSizeIsNegative_isBadRequest() throws Exception {
        // Given
        int page = 0;
        int size = -1;
        String body = """
                  {
                      "userId": %d,
                      "page": %s,
                      "size": %d
                  }
                """.formatted(userId, page, size);

        // When/Then
        mvc.perform(post("/api/v1/posts/_list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGenerateReportIsOk() throws Exception {
        // Given
        Post post1 = createPost("Title 1", "Content 1", true, userId);
        Post post2 = createPost("Title 2", "Content 2", false, userId);
        Post post3 = createPost("Title 3", "Content 3", true, userId);

        List<Post> allPosts = postRepository.findAll();
        System.err.println("testGenerateReportIsOk ");
        allPosts.forEach(System.out::println);

        Map<String, Object> filters = new HashMap<>();
        filters.put("userId", userId);

        // When
        MvcResult mvcResult = mvc.perform(post("/api/v1/posts/_report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(filters)))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String contentDisposition = mvcResult.getResponse().getHeader(HttpHeaders.CONTENT_DISPOSITION);
        assertNotNull(contentDisposition, "Content-Disposition header should not be null");
        assertTrue(contentDisposition.contains("attachment; filename=report.csv"), "Content-Disposition header should indicate CSV file attachment");
        String contentType = mvcResult.getResponse().getContentType();
        assertEquals("text/csv", contentType, "Content-Type should be text/csv");

        String csvContent = mvcResult.getResponse().getContentAsString();
        String[] csvLines = csvContent.split("\\r?\\n");
        assertEquals(4, csvLines.length, "CSV should have 4 lines (1 header + 3 posts)");

        String headerLine = csvLines[0];
        assertTrue(headerLine.contains("Post ID;Title;Content;Likes Count;Published;User ID"), "CSV header should contain expected columns");

        List<String> postLines = Arrays.asList(csvLines).subList(1, csvLines.length);
        assertTrue(postLines.stream().anyMatch(line -> line.contains(post1.getId().toString())), "CSV should contain post with ID " + post1.getId());
        assertTrue(postLines.stream().anyMatch(line -> line.contains(post3.getId().toString())), "CSV should contain post with ID " + post3.getId());
    }

    @Test
    void testUploadPosts_WhenTwoValidAndOneNotValid() throws Exception {
        // Given
        long nonExistentId = 9999999L;
        String jsonData = """
                [
                    {
                        "title": "Post 1",
                        "content": "Content 1",
                        "isPublished": true,
                        "userId": %d
                    },
                    {
                        "title": "Post 2",
                        "content": "Content 2",
                        "isPublished": false,
                        "userId": %d
                    },
                    {
                        "title": "Post 3",
                        "content": "Content 3",
                        "isPublished": true,
                        "userId": %d
                    }
                ]
                """.formatted(userId, userId, nonExistentId);


        byte[] csvBytes = jsonData.getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile("file", csvBytes);

        // When
        MvcResult mvcResult = mvc.perform(multipart("/api/v1/posts/upload")
                        .file(file))
                .andReturn();

        // Then
        int status = mvcResult.getResponse().getStatus();
        assertEquals(HttpStatus.OK.value(), status, "HTTP status should be 200 (OK)");

        Map<String, Integer> response = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Map.class);
        assertEquals(2, response.get("successfulImports"), "Should have 2 successful imports");
        assertEquals(1, response.get("failedImports"), "Should have 1 failed import");

        List<Post> savedPosts = postRepository.findAll();
        assertEquals(2, savedPosts.size(), "Should have 2 posts saved in the database");
        assertTrue(savedPosts.stream().anyMatch(post -> post.getTitle().equals("Post 1") && post.getContent().equals("Content 1") &&
                post.getIsPublished() && post.getUser().getId().equals(userId)), "Should contain the valid post 1");
        assertTrue(savedPosts.stream().anyMatch(post -> post.getTitle().equals("Post 2") && post.getContent().equals("Content 2") &&
                !post.getIsPublished() && post.getUser().getId().equals(userId)), "Should contain the valid post 2");
    }

}
