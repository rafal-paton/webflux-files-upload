package com.rpaton.filestorage.service;

import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public interface StorageService {

    void store(String fileName, InputStream content);
}
