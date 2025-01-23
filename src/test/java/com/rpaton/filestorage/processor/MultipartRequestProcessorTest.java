package com.rpaton.filestorage.processor;

import com.rpaton.filestorage.repository.FileRepository;
import com.rpaton.filestorage.repository.entity.FileEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MultipartRequestProcessorTest {

    public static final String FILE_NAME = "test.txt";
    public static final String FILE_CONTENT = "This is a test file content";

    @Mock
    private FileRepository fileRepository;
    @Mock
    private FileUploadProcessor fileProcessor;

    @InjectMocks
    private MultipartRequestProcessor multipartRequestProcessor;

    @Test
    void should_process_file_and_save_entity_successfully() {
        // Given
        FilePart mockFilePart = buildFilePart();
        FileEntity mockFileEntity = buildFileEntity(FILE_CONTENT);

        when(fileProcessor.processFile(any(FilePart.class))).thenReturn(Mono.just(mockFileEntity));
        when(fileRepository.save(any(FileEntity.class))).thenReturn(Mono.just(mockFileEntity));

        ServerRequest mockRequest = mockServerRequest(mockFilePart);

        // When
        Flux<FileEntity> result = multipartRequestProcessor.processRequest(mockRequest);

        // Then
        StepVerifier.create(result)
                .assertNext(fileEntity -> {
                    assertThat(fileEntity).isNotNull();
                    assertThat(fileEntity.fileName()).isEqualTo(FILE_NAME);
                    assertThat(fileEntity.size()).isEqualTo(FILE_CONTENT.getBytes(StandardCharsets.UTF_8).length);
                    assertThat(fileEntity.digest()).isNotNull();
                })
                .verifyComplete();

        verify(fileProcessor, times(1)).processFile(mockFilePart);
        verify(fileRepository, times(1)).save(any(FileEntity.class));
    }

    @Test
    void should_emit_no_entities_and_skip_save_when_processing_fails() {
        // Given
        FilePart mockFilePart = buildFilePart();

        when(fileProcessor.processFile(any(FilePart.class))).thenReturn(Mono.error(new RuntimeException("File processing error")));
        ServerRequest mockRequest = mockServerRequest(mockFilePart);

        // When
        Flux<FileEntity> result = multipartRequestProcessor.processRequest(mockRequest);

        // Then
        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();

        verify(fileProcessor, times(1)).processFile(mockFilePart);
        verify(fileRepository, never()).save(any(FileEntity.class));
    }

    @Test
    void should_return_no_entities_when_save_fails() {
        // Given
        FilePart mockFilePart = buildFilePart();
        FileEntity mockFileEntity = buildFileEntity(FILE_CONTENT);

        when(fileProcessor.processFile(any(FilePart.class))).thenReturn(Mono.just(mockFileEntity));
        when(fileRepository.save(any(FileEntity.class))).thenReturn(Mono.error(new RuntimeException("Database save error")));

        ServerRequest mockRequest = mockServerRequest(mockFilePart);

        // When
        Flux<FileEntity> result = multipartRequestProcessor.processRequest(mockRequest);

        // Then
        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();

        verify(fileProcessor, times(1)).processFile(mockFilePart);
        verify(fileRepository, times(1)).save(any(FileEntity.class));
    }

    private FilePart buildFilePart() {
        return mock(FilePart.class);
    }

    private static FileEntity buildFileEntity(String fileContent) {
        return FileEntity.builder()
                .fileName(FILE_NAME)
                .digest("mockDigest")
                .size((long) fileContent.getBytes(StandardCharsets.UTF_8).length)
                .build();
    }

    private static ServerRequest mockServerRequest(FilePart mockFilePart) {
        LinkedMultiValueMap<String, Part> multipartData = new LinkedMultiValueMap<>();
        multipartData.add("file", mockFilePart);
        ServerRequest mockRequest = mock(ServerRequest.class);
        when(mockRequest.multipartData()).thenReturn(Mono.just(multipartData));
        return mockRequest;
    }
}
