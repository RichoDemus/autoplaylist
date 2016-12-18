package com.richo.reader.web.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.richodemus.reader.dto.UserId;

public class Session
{
	private final UserId username;
	private final String token;

	@JsonCreator
	public Session(UserId username, String token)
	{
		this.username = username;
		this.token = token;
	}

	public UserId getUsername()
	{
		return username;
	}

	public String getToken()
	{
		return token;
	}
}
