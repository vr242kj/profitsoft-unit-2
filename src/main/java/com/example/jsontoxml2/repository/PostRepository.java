package com.example.jsontoxml2.repository;

import com.example.jsontoxml2.entity.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;

public interface PostRepository extends JpaRepository<Post, Long> {
    @Query("SELECT new com.example.jsontoxml2.entity.Post(p.id, p.title, p.content, p.isPublished, p.likesCount)  " +
            "FROM Post p " +
            "WHERE p.user.id = :#{#filters['userId']} " +
            "AND (:#{#filters['likesCount']} IS NULL OR p.likesCount >= :#{#filters['likesCount']}) " +
            "AND (:#{#filters['published']} IS NULL OR p.isPublished = :#{#filters['published']})")
    List<Post> findByUserIdAndFilters(Map<String, Object> filters, Pageable pageable);

    @Query("SELECT COUNT(p) " +
            "FROM Post p " +
            "WHERE p.user.id = :#{#filters['userId']} " +
            "AND (:#{#filters['likesCount']} IS NULL OR p.likesCount >= :#{#filters['likesCount']}) " +
            "AND (:#{#filters['published']} IS NULL OR p.isPublished = :#{#filters['published']})")
    int countByUserIdAndFilters(Map<String, Object> filters);

    @Query("SELECT new com.example.jsontoxml2.entity.Post(p.id, p.title, p.content, p.isPublished, p.likesCount)  " +
            "FROM Post p " +
            "WHERE p.user.id = :#{#filters['userId']} " +
            "AND (:#{#filters['likesCount']} IS NULL OR p.likesCount >= :#{#filters['likesCount']}) " +
            "AND (:#{#filters['published']} IS NULL OR p.isPublished = :#{#filters['published']})")
    List<Post> findByUserIdAndFiltersCSV(Map<String, Object> filters);
}
