package com.rpaton.filestorage.web;

import com.rpaton.filestorage.processor.MultipartRequestProcessor;
import com.rpaton.filestorage.repository.entity.FileEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Log4j2
@Component
@RequiredArgsConstructor
public class FileHandler {

    private final MultipartRequestProcessor requestProcessor;

    public Mono<ServerResponse> uploadFile(ServerRequest request) {
        return ServerResponse.status(HttpStatus.MULTI_STATUS)
                .contentType(MediaType.APPLICATION_NDJSON)
                .body(requestProcessor.processRequest(request), FileEntity.class);
    }
}