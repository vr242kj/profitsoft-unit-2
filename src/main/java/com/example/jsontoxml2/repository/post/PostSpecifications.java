package com.example.jsontoxml2.repository.post;

import com.example.jsontoxml2.model.entity.Post;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.Map;

public class PostSpecifications {

    public static Specification<Post> withUserIdAndFilters(Map<String, Object> filters) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.equal(root.get("user").get("id"), filters.get("userId"));

            if (filters.containsKey("likesCount")) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get("likesCount"), (Integer) filters.get("likesCount")));
            }

            if (filters.containsKey("published")) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("isPublished"), filters.get("published")));
            }

            return predicate;
        };
    }

}
