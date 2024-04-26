package com.example.jsontoxml2.service;

import com.example.jsontoxml2.entity.Post;
import com.example.jsontoxml2.repository.PostRepository;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public Post getPostById(Long id) {
        return postRepository.findById(id).orElse(null);
    }

    public Post addPost(Post post) {
        return postRepository.save(post);
    }

    public Map<String, Integer> importPosts(List<Post> posts) {
        int successfulImports = 0;
        int failedImports = 0;

        for (Post post : posts) {
            try {
                postRepository.save(post);
                successfulImports++;
            } catch (Exception e) {
                failedImports++;
            }
        }

        Map<String, Integer> response = new HashMap<>();
        response.put("successfulImports", successfulImports);
        response.put("failedImports", failedImports);

        return response;
    }

    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }

    public Map<String, Object> getPostsByUserIdAndFilters(Map<String, Object> filters) {
        Integer page = (Integer) filters.get("page");
        Integer size = (Integer) filters.get("size");

        if (page == null || page <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'page' parameter must be a positive integer greater than 0.");
        }

        if (size == null || size <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'size' parameter must be a positive integer greater than 0.");
        }

        List<Post> filteredPosts = postRepository.findByUserIdAndFilters(filters, PageRequest.of(page - 1, size));
        int countFilteredPosts = postRepository.countByUserIdAndFilters(filters);
        int totalPages = (int) Math.ceil((double) countFilteredPosts / size);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("list", filteredPosts);
        response.put("totalPages", totalPages);

        return response;
    }

    public ByteArrayResource generateCSVReport(Map<String, Object> filters) {
        List<Post> filteredPosts = postRepository.findByUserIdAndFiltersCSV(filters);

        StringBuilder csvContent = new StringBuilder();
        // Додавання заголовків CSV
        csvContent.append("Post ID;Title;Content;Likes Count;Published\n");
        // Додавання даних про пости
        for (Post post : filteredPosts) {
            csvContent.append(post.getId()).append(";")
                    .append(post.getTitle()).append(";")
                    .append(post.getContent()).append(";")
                    .append(post.getLikesCount()).append(";")
                    .append(post.getPublished()).append("\n");
        }

        byte[] csvBytes = csvContent.toString().getBytes();

        return new ByteArrayResource(csvBytes);
    }
}
