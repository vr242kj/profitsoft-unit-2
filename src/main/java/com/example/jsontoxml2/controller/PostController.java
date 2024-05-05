package com.example.jsontoxml2.controller;

import com.example.jsontoxml2.model.dto.post.PostCreateRequestDto;
import com.example.jsontoxml2.model.dto.post.PostInfoDto;
import com.example.jsontoxml2.model.dto.post.PostQueryDto;
import com.example.jsontoxml2.model.dto.post.PostQueryWithPaginationDto;
import com.example.jsontoxml2.model.dto.post.PostUpdateRequestDto;
import com.example.jsontoxml2.service.PostService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
public class PostController {

    private final PostService postService;

    @GetMapping("/{id}")
    public ResponseEntity<PostInfoDto> getPostById(@PathVariable long id) {
        var post = postService.getPostById(id);
        return ResponseEntity.ok(post);
    }

    @PostMapping
    public ResponseEntity<PostInfoDto> savePost(@Valid @RequestBody PostCreateRequestDto newPost) {
        var savedPost = postService.addPost(newPost);
        return ResponseEntity
                .created(URI.create(String.format("/%d", savedPost.getId())))
                .body(savedPost);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updatePost(@PathVariable long id, @Valid @RequestBody PostUpdateRequestDto updatePost) {
        postService.updatePost(id, updatePost);
        return ResponseEntity.ok().body("Post updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePostById(@PathVariable long id) {
        postService.deletePost(id);
        return ResponseEntity.ok("Post deleted successfully.");
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Integer>> uploadPosts(@RequestParam("file") MultipartFile file) {
        Map<String, Integer> response = postService.importPosts(file);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/_list")
    public ResponseEntity<Map<String, Object>> getPostList(
            @Valid @RequestBody PostQueryWithPaginationDto postQueryWithPaginationDto) {
        Map<String, Object> response = postService.getPostsByUserIdAndFilters(postQueryWithPaginationDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/_report", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void generateReport(HttpServletResponse response, @Valid @RequestBody PostQueryDto postQueryDto) {
        postService.generateCSVReport(response, postQueryDto);
    }

}
