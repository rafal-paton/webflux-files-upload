package com.rpaton.filestorage;

import org.springframework.boot.SpringApplication;

public class TestFilestorageApplication {

	public static void main(String[] args) {
		SpringApplication.from(FilestorageApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
