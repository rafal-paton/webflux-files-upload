package com.rpaton.filestorage.web;

import com.rpaton.filestorage.processor.MultipartRequestProcessor;
import com.rpaton.filestorage.repository.entity.FileEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileRouterHandlerTest {
    @Mock
    private MultipartRequestProcessor requestProcessor;

    private WebTestClient webTestClient;

    @BeforeEach
    void setup() {
        FileRouter router = new FileRouter();
        webTestClient = WebTestClient.bindToRouterFunction(router.route(new FileHandler(requestProcessor))).build();
    }

    @Test
    void should_route_file_upload_request_successfully() {
        // Given
        FileEntity entity = new FileEntity(
                1L,
                "example.txt",
                "abc123hash",
                1024L);

        when(requestProcessor.processRequest(any())).thenReturn(Flux.just(entity));

        // When & Then
        webTestClient.post()
                .uri("/file/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectHeader().contentType(MediaType.APPLICATION_NDJSON)
                .expectBodyList(FileEntity.class)
                .hasSize(1)
                .value(fileEntities -> {
                    assertThat(fileEntities.getFirst())
                            .isEqualTo(entity);
                });

        verify(requestProcessor, times(1)).processRequest(any());
    }

    @Test
    void should_reject_upload_route_when_content_type_is_not_multipart() {
        // When & Then
        webTestClient.post()
                .uri("/file/upload")
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is4xxClientError();

        verifyNoInteractions(requestProcessor);
    }
}
