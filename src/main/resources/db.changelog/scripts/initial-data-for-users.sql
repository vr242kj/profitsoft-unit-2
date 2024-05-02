-- Changeset for insert initial data for users
-- liquibase: changeSet id=initial-data-for-users
INSERT INTO users (username, email) VALUES ('john doe', 'john.doe@example.com');
INSERT INTO users (username, email) VALUES ('jane smith', 'jane.smith@example.com');
INSERT INTO users (username, email) VALUES ('bob johnson', 'bob.johnson@example.com');
