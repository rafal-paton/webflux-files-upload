package com.rpaton.filestorage.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Log4j2
@Service
public class StorageServiceImpl implements StorageService {

    @Value("${buffer.write.size:8192}")
    private int readBufferSize;

    private static final String UPLOAD_DIR = "uploaded-files";

    @Override
    public void store(String fileName, InputStream content) {
//        log.info("Starting to store file: {}", fileName);
//
//        try {
//            Path uploadPath = Path.of(".", UPLOAD_DIR).toAbsolutePath().normalize();
//            Files.createDirectories(uploadPath);
//            Path filePath = uploadPath.resolve(fileName);
//
//            try (InputStream bufferedInput = new BufferedInputStream(content);
//                 OutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(filePath, StandardOpenOption.CREATE, StandardOpenOption.APPEND))) {
//
//                byte[] buffer = new byte[readBufferSize];
//                long totalBytes = 0;
//                int bytesRead;
//
//                while ((bytesRead = bufferedInput.read(buffer)) != -1) {
//                    outputStream.write(buffer, 0, bytesRead);
//                    totalBytes += bytesRead;
//                    if (totalBytes % (readBufferSize * 1024L) == 0) {
//                        outputStream.flush();
//                    }
//                }
//                outputStream.flush();
//                log.info("Stored file: {}, total bytes: {}", fileName, totalBytes);
//            }
//
//        } catch (IOException e) {
//            throw new RuntimeException("Failed to store file", e);
//        }
    }
}
