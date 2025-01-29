package com.rpaton.filestorage.processor;

import com.rpaton.filestorage.repository.entity.FileEntity;
import com.rpaton.filestorage.service.StorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileUploadProcessorTest {

    @Mock
    private StorageService storageService;

    @InjectMocks
    private FileUploadProcessor fileUploadProcessor;

    @Test
    void should_process_file_and_store_content_once() {
        // Given
        String fileName = "test.txt";
        String fileContent = "This is a test file content";
        FilePart mockFilePart = mock(FilePart.class);
        when(mockFilePart.filename()).thenReturn(fileName);

        DataBuffer dataBuffer = createRealDataBuffer(fileContent);
        when(mockFilePart.content()).thenReturn(Flux.just(dataBuffer));

        doAnswer(invocation -> {
            InputStream is = invocation.getArgument(1);
            byte[] content = is.readAllBytes();
            assertThat(content).isEqualTo(fileContent.getBytes(StandardCharsets.UTF_8));
            return null;
        }).when(storageService).store(eq(fileName), any(InputStream.class));

        // When
        Mono<FileEntity> result = fileUploadProcessor.processFile(mockFilePart);

        // Then
        StepVerifier.create(result)
                .assertNext(fileEntity -> {
                    assertThat(fileEntity).isNotNull();
                    assertThat(fileEntity.fileName()).isEqualTo(fileName);
                    assertThat(fileEntity.size()).isEqualTo(fileContent.getBytes(StandardCharsets.UTF_8).length);
                    assertThat(fileEntity.digest()).isNotNull();
                })
                .verifyComplete();

        verify(storageService, times(1)).store(eq(fileName), any(InputStream.class));

        DataBufferUtils.release(dataBuffer);
    }

    @Test
    void should_process_large_file_in_parts() {
        // Given
        String fileName = "large_test.txt";
        String part1 = "This is part 1 of the file.";
        String part2 = "This is part 2 of the file.";
        String part3 = "And this is part 3.";
        String expectedContent = part1 + part2 + part3;

        FilePart mockFilePart = mock(FilePart.class);
        when(mockFilePart.filename()).thenReturn(fileName);

        DataBuffer buffer1 = createRealDataBuffer(part1);
        DataBuffer buffer2 = createRealDataBuffer(part2);
        DataBuffer buffer3 = createRealDataBuffer(part3);
        when(mockFilePart.content()).thenReturn(Flux.just(buffer1, buffer2, buffer3));

        doAnswer(invocation -> {
            InputStream is = invocation.getArgument(1);
            byte[] content = is.readAllBytes();
            assertThat(content).isEqualTo(expectedContent.getBytes(StandardCharsets.UTF_8));
            return null;
        }).when(storageService).store(eq(fileName), any(InputStream.class));

        // When
        Mono<FileEntity> result = fileUploadProcessor.processFile(mockFilePart);

        // Then
        StepVerifier.create(result)
                .assertNext(fileEntity -> {
                    assertThat(fileEntity).isNotNull();
                    assertThat(fileEntity.fileName()).isEqualTo(fileName);
                    assertThat(fileEntity.size()).isEqualTo(expectedContent.length());
                    assertThat(fileEntity.digest()).isNotNull();
                })
                .verifyComplete();

        verify(storageService, times(1)).store(eq(fileName), any(InputStream.class));

        DataBufferUtils.release(buffer1);
        DataBufferUtils.release(buffer2);
        DataBufferUtils.release(buffer3);
    }

    private DataBuffer createRealDataBuffer(String content) {
        DefaultDataBufferFactory factory = new DefaultDataBufferFactory();
        DataBuffer buffer = factory.allocateBuffer(content.length());
        buffer.write(content.getBytes(StandardCharsets.UTF_8));
        return buffer;
    }
}