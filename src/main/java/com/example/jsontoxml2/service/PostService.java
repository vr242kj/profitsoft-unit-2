package com.example.jsontoxml2.service;

import com.example.jsontoxml2.model.dto.post.PostCreateRequestDto;
import com.example.jsontoxml2.model.dto.post.PostInfoDto;
import com.example.jsontoxml2.model.dto.post.PostQueryDto;
import com.example.jsontoxml2.model.dto.post.PostQueryWithPaginationDto;
import com.example.jsontoxml2.model.dto.post.PostUpdateRequestDto;
import com.example.jsontoxml2.model.entity.Post;
import com.example.jsontoxml2.repository.post.PostRepository;
import com.example.jsontoxml2.repository.post.PostSpecifications;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final ModelMapper modelMapper;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public PostInfoDto getPostById(Long id) {
        Post postById = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + id));

        return modelMapper.map(postById, PostInfoDto.class);
    }

    public PostInfoDto addPost(PostCreateRequestDto postDto) {
        Post post = modelMapper.map(postDto, Post.class);
        Post savedPost = postRepository.save(post);
        return modelMapper.map(savedPost, PostInfoDto.class);
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

        try (ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = validatorFactory.getValidator();

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
        }

        return buildResponseMap(successfulImports, failedImports);
    }

    private List<PostCreateRequestDto> readPostsFromFile(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            validateFile(file);
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
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

    public Map<String, Object> getPostsByUserIdAndFilters(PostQueryWithPaginationDto filters) {
        List<Post> filteredPosts = getCustomerListFromPage(filters);
        int totalPages = calculateTotalPages(filters);

        return buildResponse(filteredPosts, totalPages);
    }

    private List<Post> getCustomerListFromPage(PostQueryWithPaginationDto filters) {
        int pageNumber = filters.getPage();
        int pageSize = filters.getSize();
        Page<Post> filteredPosts = postRepository.findAll(PostSpecifications.withUserIdAndFilters(filters),
                PageRequest.of(pageNumber, pageSize));

        return filteredPosts.hasContent() ? filteredPosts.getContent() : Collections.emptyList();
    }

    private int calculateTotalPages(PostQueryWithPaginationDto filters) {
        long countFilteredPosts = postRepository.count(PostSpecifications.withUserIdAndFilters(filters));
        int pageSize = filters.getSize();
        return (int) Math.ceil((double) countFilteredPosts / pageSize);
    }

    private Map<String, Object> buildResponse(List<Post> filteredPosts, int totalPages) {
        Map<String, Object> response = new LinkedHashMap<>();
        List<PostInfoDto> postInfoDtoList = filteredPosts.stream()
                .map(post -> modelMapper.map(post, PostInfoDto.class))
                .collect(Collectors.toList());

        response.put("list", postInfoDtoList);
        response.put("totalPages", totalPages);
        return response;
    }

    public void generateCSVReport(HttpServletResponse response, PostQueryDto filters) {
        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report.csv");

        try {
            var csvContent = new StringBuilder();
            csvContent.append("Post ID;Title;Content;Likes Count;Published;User ID\n");

            List<Post> filteredPosts = postRepository.findAll(PostSpecifications.withUserIdAndFilters(filters));
            for (var post : filteredPosts) {
                csvContent.append(String.format("%s%n", post.toCSV()));
            }

            response.getOutputStream().write(csvContent.toString().getBytes());
            response.getOutputStream().flush();
        } catch (IOException e) {
            throw new RuntimeException("Error generating text file", e);
        }
    }

}
