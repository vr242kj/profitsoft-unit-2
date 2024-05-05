package com.example.jsontoxml2.repository.post;

import com.example.jsontoxml2.model.dto.post.PostQueryDto;
import com.example.jsontoxml2.model.entity.Post;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class PostSpecifications {

    public static Specification<Post> withUserIdAndFilters(PostQueryDto filters) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.equal(root.get("user").get("id"), filters.getUserId());

            if (filters.getLikesCount() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get("likesCount"), filters.getLikesCount()));
            }

            if (filters.getIsPublished() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("isPublished"), filters.getIsPublished()));
            }

            return predicate;
        };
    }

}
