package com.rpaton.filestorage.processor;

import com.rpaton.filestorage.repository.entity.FileEntity;
import com.rpaton.filestorage.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicLong;

@Log4j2
@Component
@RequiredArgsConstructor
public class FileUploadProcessor {

    @Value("${buffer.upload.size:8192}")
    private long bufferSize;

    private final StorageService storageService;

    public Mono<FileEntity> processFile(FilePart filePart) {
        AtomicLong size = new AtomicLong(0);
        AtomicLong accumulatedBufferSize = new AtomicLong(0);
        MessageDigest digest = createDigest();

        return filePart.content()
                .bufferUntil(dataBuffer -> checkIfBufferIsFull(dataBuffer, accumulatedBufferSize))
                .flatMap(bufferedList -> DataBufferUtils.join(Flux.fromIterable(bufferedList))
                        .doOnDiscard(DataBuffer.class, DataBufferUtils::release)
                        .doOnNext(dataBuffer -> processChunk(filePart, dataBuffer, digest, size)))
                .then(Mono.defer(() -> {
                    log.info("Finished processing file: {}", filePart.filename());
                    return Mono.just(FileEntity.builder()
                            .fileName(filePart.filename())
                            .digest(Base64.getEncoder().encodeToString(digest.digest()))
                            .size(size.get())
                            .build());
                }))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private static MessageDigest createDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    private void processChunk(FilePart filePart, DataBuffer dataBuffer, MessageDigest digest, AtomicLong size) {
        byte[] chunk = new byte[dataBuffer.readableByteCount()];
        dataBuffer.read(chunk);
        digest.update(chunk);
        size.addAndGet(chunk.length);
        try (InputStream inputStream = new ByteArrayInputStream(chunk)) {
            storageService.store(filePart.filename(), inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Error processing chunk", e);
        }
    }

    private boolean checkIfBufferIsFull(DataBuffer dataBuffer, AtomicLong accumulatedBufferSize) {
        long chunkSize = dataBuffer.readableByteCount();
        accumulatedBufferSize.addAndGet(chunkSize);
        if (accumulatedBufferSize.get() >= bufferSize) {
            accumulatedBufferSize.set(0);
            return true;
        } else {
            return false;
        }
    }
}
