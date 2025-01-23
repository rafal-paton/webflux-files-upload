package com.rpaton.filestorage.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;

@Configuration
public class FileRouter {

    @Bean
    public RouterFunction<ServerResponse> route(FileHandler fileHandler) {
        return RouterFunctions.route(POST("/file/upload").and(contentType(MULTIPART_FORM_DATA)), fileHandler::uploadFile);
    }
}
