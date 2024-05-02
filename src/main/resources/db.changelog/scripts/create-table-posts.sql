-- Changeset for creating the posts table
-- liquibase: changeSet id=create-table-posts
CREATE TABLE posts (
                       id SERIAL PRIMARY KEY,
                       title VARCHAR(255) NOT NULL,
                       content TEXT NOT NULL,
                       published BOOLEAN,
                       likes_count INTEGER DEFAULT 0,
                       user_id INT NOT NULL,
                       FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
