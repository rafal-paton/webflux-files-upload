package com.rpaton.filestorage.processor;

import com.rpaton.filestorage.repository.entity.FileEntity;
import com.rpaton.filestorage.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.Pipe;
import java.nio.channels.WritableByteChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicLong;

@Log4j2
@Component
@RequiredArgsConstructor
public class FileUploadProcessor {

    private final StorageService storageService;

    public Mono<FileEntity> processFile(FilePart filePart) {
        return Mono.<FileEntity>create(emitter -> {
            try {
                Pipe pipe = Pipe.open();
                MessageDigest digest = createDigest();
                AtomicLong size = new AtomicLong(0);
                initializeStorage(filePart, pipe, emitter);
                processFileContent(filePart, pipe, digest, size, emitter);
            } catch (Exception e) {
                emitter.error(new RuntimeException("Error initializing file processing", e));
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private void processFileContent(FilePart filePart, Pipe pipe, MessageDigest digest, AtomicLong size, MonoSink<FileEntity> sink) {
        filePart.content()
                .doOnNext(dataBuffer -> processChunk(dataBuffer, pipe.sink(), digest, size, sink))
                .doOnComplete(() -> finishProcessing(filePart, pipe, digest, size, sink))
                .doOnError(sink::error)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }

    private void initializeStorage(FilePart filePart, Pipe pipe, MonoSink<FileEntity> sink) {
        Schedulers.boundedElastic().schedule(() -> {
            try (InputStream inputStream = Channels.newInputStream(pipe.source())) {
                storageService.store(filePart.filename(), inputStream);
            } catch (Exception e) {
                sink.error(new RuntimeException("Error reading from pipe", e));
            }
        });
    }

    private void processChunk(DataBuffer dataBuffer, WritableByteChannel sinkChannel, MessageDigest digest, AtomicLong size, MonoSink<FileEntity> sink) {
        try {
            byte[] chunk = new byte[dataBuffer.readableByteCount()];
            dataBuffer.read(chunk);
            digest.update(chunk);
            size.addAndGet(chunk.length);
            sinkChannel.write(ByteBuffer.wrap(chunk));
        } catch (IOException e) {
            sink.error(new RuntimeException("Error writing to pipe", e));
        } finally {
            DataBufferUtils.release(dataBuffer);
        }
    }

    private void finishProcessing(FilePart filePart, Pipe pipe, MessageDigest digest, AtomicLong size, MonoSink<FileEntity> emitter) {
        try {
            pipe.sink().close();
            FileEntity fileEntity = FileEntity.builder()
                    .fileName(filePart.filename())
                    .digest(Base64.getEncoder().encodeToString(digest.digest()))
                    .size(size.get())
                    .build();
            emitter.success(fileEntity);
        } catch (Exception e) {
            emitter.error(new RuntimeException("Error completing file processing", e));
        }
    }

    private static MessageDigest createDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
