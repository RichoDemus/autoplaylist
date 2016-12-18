package com.richo.reader.web.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.richodemus.reader.dto.UserId;

public class CreateUserRequest
{
	public final UserId username;
	public final String password;
	public final String inviteCode;

	@JsonCreator
	public CreateUserRequest(@JsonProperty("username") final UserId username,
							 @JsonProperty("password") final String password,
							 @JsonProperty("inviteCode") final String inviteCode)
	{
		this.username = username;
		this.password = password;
		this.inviteCode = inviteCode;
	}
}
