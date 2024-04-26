package com.example.jsontoxml2.controller;

import com.example.jsontoxml2.entity.Post;
import com.example.jsontoxml2.service.PostService;
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
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/posts")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable long id) {
        Optional<Post> post = Optional.ofNullable(postService.getPostById(id));
        return post.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // add validation
    @PostMapping
    public ResponseEntity<Post> savePost(@RequestBody Post newPost) {
        var savedPost = postService.addPost(newPost);
        return ResponseEntity
                .created(URI.create(String.format("/%d", savedPost.getId())))
                .body(savedPost);
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Integer>> uploadPosts(@RequestBody List<Post> posts) {
        Map<String, Integer> response = postService.importPosts(posts);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updatePost(@PathVariable long id, @RequestBody Post updatePost) {
        System.out.println("existingPost " + updatePost.toString());

        Post existingPost = postService.getPostById(id);
        if (existingPost == null) {
            return ResponseEntity.notFound().build();
        }

        if (updatePost.getTitle() != null) {
            existingPost.setTitle(updatePost.getTitle());
        }
        if (updatePost.getContent() != null) {
            existingPost.setContent(updatePost.getContent());
        }
        if (updatePost.getPublished() != null) {
            existingPost.setPublished(updatePost.getPublished());
        }
        if (updatePost.getLikesCount() != null) {
            existingPost.setLikesCount(updatePost.getLikesCount());
        }
        postService.addPost(existingPost);
        return ResponseEntity.ok().body("Post updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePostById(@PathVariable long id) {
        postService.deletePost(id);
        return ResponseEntity.ok("Post deleted successfully.");
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
