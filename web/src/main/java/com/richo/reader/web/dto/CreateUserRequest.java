package com.richo.reader.web.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateUserRequest
{
	public final String username;
	public final String password;
	public final String inviteCode;

	@JsonCreator
	public CreateUserRequest(@JsonProperty("username") final String username,
							 @JsonProperty("password") final String password,
							 @JsonProperty("inviteCode") final String inviteCode)
	{
		this.username = username;
		this.password = password;
		this.inviteCode = inviteCode;
	}
}
