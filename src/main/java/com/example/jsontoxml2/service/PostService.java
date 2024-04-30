package com.example.jsontoxml2.service;

import com.example.jsontoxml2.model.dto.PostDto;
import com.example.jsontoxml2.model.entity.Post;
import com.example.jsontoxml2.repository.PostRepository;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private static final ObjectMapper mapper = new ObjectMapper();

    public Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + id));
    }

    public Post addPost(Post post) {
        return postRepository.save(post);
    }

    public void updatePost(long id, Post updatedPost) {
        Post existingPost = getPostById(id);

        updateFieldIfNotNull(existingPost::setTitle, updatedPost.getTitle());
        updateFieldIfNotNull(existingPost::setContent, updatedPost.getContent());
        updateFieldIfNotNull(existingPost::setIsPublished, updatedPost.getIsPublished());
        updateFieldIfNotNull(existingPost::setLikesCount, updatedPost.getLikesCount());

        addPost(existingPost);
    }

    private <T> void updateFieldIfNotNull(Consumer<T> setter, T value) {
        if (value != null) {
            setter.accept(value);
        }
    }

    public void deletePost(Long id) {
        getPostById(id);
        postRepository.deleteById(id);
    }

    public Map<String, Integer> importPosts(MultipartFile file) throws Exception {
        int successfulImports = 0;
        int failedImports = 0;

        try (InputStream inputStream = file.getInputStream();
             JsonParser parser = mapper.getFactory().createParser(inputStream)) {
            validateFile(file);
            validateJsonFormat(parser);

            while (parser.nextToken() != JsonToken.END_ARRAY) {
                try {
                    Post post = mapper.readValue(parser, Post.class);
                    addPost(post);
                    successfulImports++;
                } catch (Exception e) {
                    failedImports++;
                }
            }
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("The provided file does not exist");
        } catch (IOException e) {
            throw new Exception("Failed to import data due to I/O error");
        }

        return buildResponseMap(successfulImports, failedImports);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("The provided file is empty");
        }
    }

    private void validateJsonFormat(JsonParser parser) throws IOException {
        if (parser.nextToken() != JsonToken.START_ARRAY) {
            throw new IllegalArgumentException("Invalid JSON format: expected array");
        }
    }

    private Map<String, Integer> buildResponseMap(int successfulImports, int failedImports) {
        Map<String, Integer> response = new HashMap<>();
        response.put("successfulImports", successfulImports);
        response.put("failedImports", failedImports);
        return response;
    }

    public Map<String, Object> getPostsByUserIdAndFilters(Map<String, Object> filters) {
        int pageNumber = validatePaginationParameters((Integer) filters.get("page"), "page");
        int pageSize = validatePaginationParameters((Integer) filters.get("size"), "size");

        List<PostDto> filteredPosts =
                postRepository.findByUserIdAndFiltersWithPagination(filters, PageRequest.of(pageNumber - 1, pageSize));
        int totalPages = calculateTotalPages(filters, pageSize);

        return buildResponse(filteredPosts, totalPages);
    }

    private int validatePaginationParameters(Integer value, String parameterName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(String.format("%s parameter must be a positive integer greater than 0.",
                    parameterName));
        }

        return value;
    }

    private int calculateTotalPages(Map<String, Object> filters, int pageSize) {
        int countFilteredPosts = postRepository.countByUserIdAndFilters(filters);
        return (int) Math.ceil((double) countFilteredPosts / pageSize);
    }

    private Map<String, Object> buildResponse(List<PostDto> filteredPosts, int totalPages) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("list", filteredPosts);
        response.put("totalPages", totalPages);
        return response;
    }

    public ByteArrayResource generateCSVReport(Map<String, Object> filters) {
        List<PostDto> filteredPosts = postRepository.findByUserIdAndFilters(filters);
        var csvContent = new StringBuilder();

        csvContent.append("Post ID;Title;Content;Likes Count;Published;User ID\n");
        for (var post : filteredPosts) {
            csvContent.append(String.format("%s%n", post.toCSV()));
        }

        byte[] csvBytes = csvContent.toString().getBytes();

        return new ByteArrayResource(csvBytes);
    }

}
