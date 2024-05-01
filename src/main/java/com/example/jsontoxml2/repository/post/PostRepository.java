package com.example.jsontoxml2.repository.post;

import com.example.jsontoxml2.model.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {

    List<Post> findAll(Specification<Post> spec);

    Page<Post> findAll(Specification<Post> spec, Pageable pageable);

    long count(Specification<Post> spec);

}
