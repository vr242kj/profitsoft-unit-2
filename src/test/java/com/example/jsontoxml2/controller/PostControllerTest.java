package com.example.jsontoxml2.controller;

import com.example.jsontoxml2.Main;
import com.example.jsontoxml2.model.dto.post.PostCreateRequestDto;
import com.example.jsontoxml2.model.dto.post.PostDto;
import com.example.jsontoxml2.model.dto.post.PostUpdateRequestDto;
import com.example.jsontoxml2.model.dto.user.UserIdDto;
import com.example.jsontoxml2.model.dto.user.UserInputDto;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
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

    private static User user;

    @AfterEach
    public void afterEach() {
        postRepository.deleteAll();
    }

    private Post createPost(String title, String content, Boolean isPublished, UserIdDto user) {
        PostCreateRequestDto postCreateRequestDto = new PostCreateRequestDto(title, content, isPublished, user);
        return modelMapper.map(postCreateRequestDto, Post.class);
    }

    @BeforeAll
    static void setup(@Autowired UserRepository userRepository, @Autowired ModelMapper modelMapper) {
        UserInputDto userInputDto = new UserInputDto("Anna Lore", "anna.lore@example.com");
        user = modelMapper.map(userInputDto, User.class);
        userRepository.save(user);
    }

    @Test
    void testGetPostById() throws Exception {
        // Given
        Long userId = user.getId();
        Post post = createPost("Test Post", "This is a test post content.",
                true, new UserIdDto(userId));
        Post savedPost = postRepository.save(post);

        // When
        MvcResult mvcResult = mvc.perform(get("/api/v1/posts/{id}", savedPost.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        PostDto returnedPost = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                PostDto.class
        );

        assertEquals(savedPost.getId(), returnedPost.getId());
        assertEquals(savedPost.getTitle(), returnedPost.getTitle());
        assertEquals(savedPost.getContent(), returnedPost.getContent());
    }

    @Test
    void testSavePost() throws Exception {
        // Given
        Long userId = user.getId();
        PostCreateRequestDto newPost = new PostCreateRequestDto("Test Post",
                "This is a test post content.", true, new UserIdDto(userId));

        // When
        MvcResult mvcResult = mvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newPost)))
                .andExpect(status().isCreated())
                .andReturn();

        // Then
        String locationHeader = mvcResult.getResponse().getHeader("Location");
        assertNotNull(locationHeader);

        long postId = Long.parseLong(locationHeader.substring(locationHeader.lastIndexOf("/") + 1));

        Post savedPost = postRepository.findById(postId).orElse(null);
        assertNotNull(savedPost);
        assertEquals(newPost.getTitle(), savedPost.getTitle());
        assertEquals(newPost.getContent(), savedPost.getContent());
        assertEquals(newPost.getIsPublished(), savedPost.getIsPublished());
    }

    @Test
    void testUpdatePost() throws Exception {
        // Given
        Long userId = user.getId();
        Post post = createPost("Test Post",
                "This is a test post content.", true, new UserIdDto(userId));
        postRepository.save(post);

        PostUpdateRequestDto postUpdateRequestDto = new PostUpdateRequestDto("Updated Title",
                "Updated content", true, 1, new UserIdDto(userId));

        // When
        mvc.perform(put("/api/v1/posts/{id}", post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postUpdateRequestDto)))
                .andExpect(status().isOk());

        // Then
        Post updatedPost = postRepository.findById(post.getId()).orElse(null);
        assertNotNull(updatedPost);
        assertEquals(postUpdateRequestDto.getTitle(), updatedPost.getTitle());
        assertEquals(postUpdateRequestDto.getContent(), updatedPost.getContent());
        assertEquals(postUpdateRequestDto.getIsPublished(), updatedPost.getIsPublished());
        assertEquals(postUpdateRequestDto.getLikesCount(), updatedPost.getLikesCount());
    }

    @Test
    void testDeletePostById() throws Exception {
        // Given
        Long userId = user.getId();
        Post post = createPost("Test Post",
                "This is a test post content.", true, new UserIdDto(userId));
        Post savedPost = postRepository.save(post);

        // When
        mvc.perform(delete("/api/v1/posts/{id}", savedPost.getId()))
                .andExpect(status().isOk());

        // Then
        Optional<Post> deletedPost = postRepository.findById(savedPost.getId());
        assertFalse(deletedPost.isPresent());
    }

    @Test
    void testUploadPosts() throws Exception {
        // Given
        Long userId = user.getId();

        String jsonData = """
                [
                    {
                        "title": "Post 1",
                        "content": "Content 1",
                        "isPublished": true,
                        "user": {
                            "id": %d
                        }
                    },
                    {
                        "title": "Post 2",
                        "content": "Content 2",
                        "isPublished": false,
                        "user": {
                            "id": %d
                        }
                    },
                    {
                        "title": "Post 3",
                        "content": "Content 3",
                        "isPublished": true,
                        "user": {
                            "id": 10
                        }
                    }
                ]
                """.formatted(userId, userId);


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
                post.getIsPublished() && post.getUser().getId().equals(user.getId())), "Should contain the valid post 1");
        assertTrue(savedPosts.stream().anyMatch(post -> post.getTitle().equals("Post 2") && post.getContent().equals("Content 2") &&
                !post.getIsPublished() && post.getUser().getId().equals(user.getId())), "Should contain the valid post 2");
    }

    @Test
    void testGetPostList() throws Exception {
        // Given
        Long userId = user.getId();
        Post post1 = createPost("Title 1", "Content 1", true, new UserIdDto(userId));
        Post post2 = createPost("Title 2", "Content 2", false, new UserIdDto(userId));
        Post post3 = createPost("Title 3", "Content 3", true, new UserIdDto(userId));
        postRepository.saveAll(Arrays.asList(post1, post2, post3));

        int page = 1;
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
                .andReturn();

        // Then
        int status = mvcResult.getResponse().getStatus();
        assertEquals(HttpStatus.OK.value(), status, "HTTP status should be 200 (OK)");

        Map<String, Object> responseMap = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<Map<String, Object>>() {
        });
        List<Map<String, Object>> posts = (List<Map<String, Object>>) responseMap.get("list");
        assertEquals(3, posts.size(), "Should return 3 published posts for the given user");

        List<String> postTitles = posts.stream().map(post -> (String) post.get("title")).toList();
        assertTrue(postTitles.contains("Title 1"), "Should contain post with title 'Title 1'");
        assertTrue(postTitles.contains("Title 3"), "Should contain post with title 'Title 3'");
    }

    @Test
    void testGenerateReport() throws Exception {
        // Given
        Post post1 = createPost("Title 1", "Content 1", true, new UserIdDto(user.getId()));
        Post post2 = createPost("Title 2", "Content 2", false, new UserIdDto(user.getId()));
        Post post3 = createPost("Title 3", "Content 3", true, new UserIdDto(user.getId()));
        postRepository.saveAll(Arrays.asList(post1, post2, post3));

        Map<String, Object> filters = new HashMap<>();
        filters.put("userId", user.getId());

        // When
        MvcResult mvcResult = mvc.perform(post("/api/v1/posts/_report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(filters)))
                .andReturn();

        // Then
        int status = mvcResult.getResponse().getStatus();
        assertEquals(HttpStatus.OK.value(), status, "HTTP status should be 200 (OK)");

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
}
