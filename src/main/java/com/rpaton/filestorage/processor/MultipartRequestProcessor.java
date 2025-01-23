package com.rpaton.filestorage.processor;

import com.rpaton.filestorage.repository.FileRepository;
import com.rpaton.filestorage.repository.entity.FileEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Flux;

@Log4j2
@Component
@RequiredArgsConstructor
public class MultipartRequestProcessor {

    private final FileRepository fileRepository;
    private final FileUploadProcessor fileProcessor;

    public Flux<FileEntity> processRequest(ServerRequest request) {
        return request.multipartData()
                .doOnNext(multipart -> log.info("Starting file upload processing: {}", multipart.size()))
                .flatMapMany(multipart -> Flux.fromIterable(multipart.get("file")))
                .cast(FilePart.class)
                .flatMap(fileProcessor::processFile)
                .flatMap(file -> fileRepository.save(file)
                        .doOnSuccess(savedFile -> log.info("FileEntity saved: {}", savedFile.fileName())))
                .onErrorContinue((error, obj) -> log.error("Error processing file: {}", obj, error));
    }
}
