package com.symphony.applaunch.dto;

import org.springframework.http.HttpStatus;

import lombok.Data;

@Data
public class ResponseDTO {

	private String message;
	private Boolean isSuccess;
	private Boolean isMailSent;
	private Integer errorCodeId;
	private String userapp;
	private Long id;
	private String teamNameListStr;
	private String authToken;
	private HttpStatus httpStatus;
}
