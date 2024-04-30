package com.example.jsontoxml2.controller;

import com.example.jsontoxml2.model.entity.Post;
import com.example.jsontoxml2.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
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
    public ResponseEntity<Post> getPostById(@PathVariable long id) {
        var post = postService.getPostById(id);
        return ResponseEntity.ok(post);
    }

    // add validation
    @PostMapping
    public ResponseEntity<Post> savePost(@RequestBody Post newPost) {
        var savedPost = postService.addPost(newPost);
        return ResponseEntity
                .created(URI.create(String.format("/%d", savedPost.getId())))
                .body(savedPost);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updatePost(@PathVariable long id,@Valid @RequestBody Post updatePost) {
        postService.updatePost(id, updatePost);
        return ResponseEntity.ok().body("Post updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePostById(@PathVariable long id) {
        postService.deletePost(id);
        return ResponseEntity.ok("Post deleted successfully.");
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Integer>> uploadPosts(@RequestParam("file") MultipartFile file) throws Exception {
        Map<String, Integer> response = postService.importPosts(file);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/_list")
    public ResponseEntity<Map<String, Object>> getPostList(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = postService.getPostsByUserIdAndFilters(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/_report")
    public ResponseEntity<ByteArrayResource> generateReport(@RequestBody Map<String, Object> request) {
        ByteArrayResource resource = postService.generateCSVReport(request);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report.csv");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(resource);
    }

}
