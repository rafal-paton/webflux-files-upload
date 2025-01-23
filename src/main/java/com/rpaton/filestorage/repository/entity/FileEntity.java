package com.rpaton.filestorage.repository.entity;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Builder
@Table("files")
public record FileEntity(
        @Id
        Long id,
        String fileName,
        String digest,
        Long size
) {
}
