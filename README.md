# JsonToXML2
This project is a Spring Boot application that serves as a RESTful API for managing entities related to user posts and users themselves. The application uses PostgreSQL as its database backend and provides endpoints to perform CRUD (Create, Read, Update, Delete) operations on these entities.   
## How to Run
1. Install Docker and Docker Compose:
- If not already installed, please follow the official Docker installation instructions available on the [Docker website](https://www.docker.com/get-started/).
2. Navigate to the Directory:
- Open your terminal or command prompt and navigate to the directory containing docker-compose.yml [file](https://github.com/vr242kj/JsonToXml2/blob/master/docker-compose.yml).
3. Run Docker Compose:
- To run in the background: ```docker compose up```
4. Use collection API from Postman - [click]()


5. JSON file to import data - [click](https://github.com/vr242kj/JsonToXml2/blob/master/src/main/resources/posts.json)  
5 posts for user id 1 (3 true, 2 false)  
3 posts for user id 2 (2 true, 1 false)
## Entity Information
**Post Entity** (many)

The Post entity represents a post in the system. Each post contains a title, content, information about its publication status, the number of likes it has received, and a reference to the user who created the post.

Attributes:
- **id:**  Unique identifier for the post.  
- **title:**  Title of the post.
- **content:** Content of the post.
- **published:** Boolean value indicating whether the post is published or not.
- **likes_count:** Number of likes received by the post.
- **user_id:** Reference to the user who created the post.

**User Entity** (one)

The User entity represents a user in the system. Each user has a unique username and email address, and may have authored multiple posts.  

Attributes:
- **id:** Unique identifier for the user.
- **username:** Unique username of the user.
- **email:** Unique email address of the user.


