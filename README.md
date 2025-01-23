# Reactive Files Processor

This application leverages reactive streams to efficiently process file uploads, ensuring optimal memory usage regardless of file size.
It processes files in configurable chunks, generates secure metadata, and stores file content on disk while saving metadata in a PostgreSQL database.

---

## Core Features

- **Reactive File Upload**: Upload single or multiple files efficiently using non-blocking operations
- **Chunk Processing**: Smart handling of large files through configurable buffer sizes
- **File Metadata**: Automatic generation of file size and SHA-256 digest
- **Database Integration**: Persistent storage of file metadata in PostgreSQL (R2DBC)
- **Error Handling**: Handling of upload failures
- **Comprehensive Testing**: Unit and integration tests, some of them with TestContainers


---

## Technologies Used

- Backend: <br>
  ![Java 17](https://img.shields.io/badge/Java-21-orange?style=for-the-badge) &nbsp; ![Spring WebFlux](https://img.shields.io/badge/Spring_WebFlux-6DB33F?style=for-the-badge&logo=spring&logoColor=white) &nbsp; ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.0-brightgreen?style=for-the-badge&logo=springboot) &nbsp; ![Apache Maven](https://img.shields.io/badge/Apache_Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)

- Database: <br>
  ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white) &nbsp; ![pgAdmin](https://img.shields.io/badge/pgAdmin-316192?style=for-the-badge&logo=postgresql&logoColor=white)

- Testing: <br>
  ![JUnit5](https://img.shields.io/badge/-JUnit5-25A162?style=for-the-badge&logo=junit5&logoWidth=30) &nbsp; ![AssertJ](https://img.shields.io/badge/-AssertJ-6A2E2A?style=for-the-badge&logo=assertj&logoWidth=30) &nbsp; ![Mockito](https://img.shields.io/badge/-Mockito-1C1C1C?style=for-the-badge&logo=mockito&logoWidth=30&logoColor=white) &nbsp; ![TestContainers](https://img.shields.io/badge/-TestContainers-000000?style=for-the-badge&logo=testcontainers&logoWidth=30)

- Deployment: <br>
  ![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white) &nbsp; ![Docker Compose](https://img.shields.io/badge/Docker%20Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)

- Other: <br>
  ![Lombok](https://img.shields.io/badge/Lombok-Black?style=for-the-badge&logo=lombok) &nbsp; ![Log4j2](https://img.shields.io/badge/Log4j2-FF9B00?style=for-the-badge&logo=apachelog4j) &nbsp; ![Git](https://img.shields.io/badge/Git-F05032?style=for-the-badge&logo=git&logoColor=white) &nbsp; ![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white)

---

## API Endpoint

Application provides the following endpoint:

| **Endpoint**  | **Method**  |            **Request**            |           **Response**           |    **Function**     |
|:-------------:|:-----------:|:---------------------------------:|:--------------------------------:|:-------------------:|
|  `/file/upload`   |    POST     | Multipart Form Data (file) | APPLICATION_NDJSON (List of FileEntity containing: id, fileName, digest, size) |   Upload single or multiple files  |

---

## How It Works
1. **File Upload**: Users can upload single or multiple files through a reactive endpoint.
2. **Chunk Processing**: Files are processed in chunks using reactive streams for memory efficiency.
3. **Metadata Generation**: Each file generates metadata including size and SHA-256 digest.
4. **Physical Storage**: Files are stored on disk with buffered I/O operations.
5. **Database Integration**: File metadata is stored in PostgreSQL using reactive R2DBC.
6. **Reactive Response**: Server returns file metadata as a stream using APPLICATION_NDJSON format.

This architecture ensures efficient handling of large files while maintaining responsive performance through non-blocking operations.


---

## How to Run

### Prerequisites
- Java 21
- Docker Desktop
- Maven
- Postman (optional)

### Setup & Run
1. Start Docker Desktop
2. Clone the repository
3. Run PostgreSQL container:

 ```sh
docker-compose up -d
   ```


Build and run the application:

 ```sh
mvn clean install
mvn spring-boot:run
   ```

### Testing the API using Postman
- Create a new POST request to http://localhost:8080/file/upload
- Set request type to multipart/form-data
- Add files using key name file
- Send request and receive file metadata in response

The API will return file metadata including generated ID, filename, size, and SHA-256 digest.

#### Example response:

```json
{
  "id": "1",
  "fileName": "example.pdf",
  "size": 1234567,
  "digest": "sha256-hash"
}
```
---