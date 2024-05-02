-- Changeset for adding indexes to the posts table
-- liquibase: changeSet id=add-indexes-to-posts
CREATE INDEX multiIndexUserAndPublished ON posts (user_id, published);
CREATE INDEX multiIndexUserAndLikedCount ON posts (user_id, likes_count);
CREATE INDEX multiIndexUserAndFilters ON posts (user_id, published, likes_count);
