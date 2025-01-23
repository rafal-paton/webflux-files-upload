package com.rpaton.filestorage.integration;

import com.rpaton.filestorage.TestcontainersConfiguration;
import com.rpaton.filestorage.repository.entity.FileEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class FileUploadIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void should_process_file_and_return_metadata() throws IOException {
        // Given
        String testContent = "Test content for verification";
        Path testFile = Files.createTempFile("test", ".txt");
        Files.write(testFile, testContent.getBytes());

        // When & Then
        webTestClient.post()
                .uri("/file/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData("file", new FileSystemResource(testFile)))
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBodyList(FileEntity.class)
                .hasSize(1)
                .value(list -> {
                    FileEntity file = list.getFirst();
                    assertThat(file.fileName()).startsWith("test");
                    assertThat(file.fileName()).endsWith(".txt");
                    assertThat(file.size()).isEqualTo(testContent.length());
                    assertThat(file.digest()).isNotEmpty();
                });

        Files.delete(testFile);
    }

    @Test
    void should_process_multiple_files_and_return_metadata() throws IOException {
        // Given
        String content1 = "First file content";
        String content2 = "Second file with different content";

        Path testFile1 = Files.createTempFile("test1", ".txt");
        Path testFile2 = Files.createTempFile("test2", ".txt");

        Files.write(testFile1, content1.getBytes());
        Files.write(testFile2, content2.getBytes());

        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("file", new FileSystemResource(testFile1));
        bodyBuilder.part("file", new FileSystemResource(testFile2));

        // When & Then
        webTestClient.post()
                .uri("/file/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBodyList(FileEntity.class)
                .hasSize(2)
                .value(list -> {
                    assertThat(list).extracting(FileEntity::fileName)
                            .containsExactlyInAnyOrder(
                                    testFile1.getFileName().toString(),
                                    testFile2.getFileName().toString()
                            );
                    assertThat(list).extracting(FileEntity::size)
                            .containsExactlyInAnyOrder(
                                    (long) content1.length(),
                                    (long) content2.length()
                            );
                    assertThat(list).extracting(FileEntity::digest)
                            .allMatch(digest -> digest != null && !digest.isEmpty());
                });

        Files.delete(testFile1);
        Files.delete(testFile2);
    }
}
