package com.example.jsontoxml2.service;

import com.example.jsontoxml2.model.dto.post.PostCreateRequestDto;
import com.example.jsontoxml2.model.dto.post.PostDto;
import com.example.jsontoxml2.model.dto.post.PostUpdateRequestDto;
import com.example.jsontoxml2.model.entity.Post;
import com.example.jsontoxml2.repository.PostRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final ModelMapper modelMapper;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public PostDto getPostById(Long id) {
        Post postById = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + id));

        return modelMapper.map(postById, PostDto.class);
    }

    public PostDto addPost(PostCreateRequestDto postDto) {
        Post post = modelMapper.map(postDto, Post.class);
        Post savedPost = postRepository.save(post);
        return modelMapper.map(savedPost, PostDto.class);
    }

    public void updatePost(long id, PostUpdateRequestDto updatedPostDto) {
        getPostById(id);
        Post post = modelMapper.map(updatedPostDto, Post.class);
        post.setId(id);
        postRepository.save(post);
    }

    public void deletePost(Long id) {
        getPostById(id);
        postRepository.deleteById(id);
    }

    public Map<String, Integer> importPosts(MultipartFile file) {
        int successfulImports = 0;
        int failedImports = 0;

        List<PostCreateRequestDto> postsFromFile = readPostsFromFile(file);
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        for (PostCreateRequestDto postCreateRequestDto : postsFromFile) {
            Set<ConstraintViolation<PostCreateRequestDto>> violations = validator.validate(postCreateRequestDto);

            if (violations.isEmpty()) {
                try {
                    Post post = modelMapper.map(postCreateRequestDto, Post.class);
                    postRepository.save(post);
                    successfulImports++;
                } catch (Exception e) {
                    failedImports++;
                }
            } else {
                failedImports++;
            }
        }

        return buildResponseMap(successfulImports, failedImports);
    }

    private List<PostCreateRequestDto> readPostsFromFile(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            validateFile(file);
            return objectMapper.readValue(inputStream, new TypeReference<>() {});
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("The provided file does not exist");
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("The provided file has invalid JSON format", e);
        } catch (IOException e) {
            throw new RuntimeException("Failed to import data due to I/O error");
        }
    }


    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("The provided file is empty");
        }
    }

    private Map<String, Integer> buildResponseMap(int successfulImports, int failedImports) {
        return Map.ofEntries(
                Map.entry("successfulImports", successfulImports),
                Map.entry("failedImports", failedImports)
        );
    }

    public Map<String, Object> getPostsByUserIdAndFilters(Map<String, Object> filters) {
        int pageNumber = validatePaginationParameters((Integer) filters.get("page"), "page");
        int pageSize = validatePaginationParameters((Integer) filters.get("size"), "size");

        List<Post> filteredPosts =
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

    private Map<String, Object> buildResponse(List<Post> filteredPosts, int totalPages) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("list", filteredPosts);
        response.put("totalPages", totalPages);
        return response;
    }

    public ByteArrayResource generateCSVReport(Map<String, Object> filters) {
        List<Post> filteredPosts = postRepository.findByUserIdAndFilters(filters);
        var csvContent = new StringBuilder();

        csvContent.append("Post ID;Title;Content;Likes Count;Published;User ID\n");
        for (var post : filteredPosts) {
            csvContent.append(String.format("%s%n", post.toCSV()));
        }

        byte[] csvBytes = csvContent.toString().getBytes();

        return new ByteArrayResource(csvBytes);
    }

}
