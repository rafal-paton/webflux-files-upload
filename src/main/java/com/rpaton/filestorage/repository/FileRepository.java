package com.rpaton.filestorage.repository;

import com.rpaton.filestorage.repository.entity.FileEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends ReactiveCrudRepository<FileEntity,Integer> {
}


