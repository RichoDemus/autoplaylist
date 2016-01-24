package com.richo.reader.backend.user;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonFileSystemUserPersistenceTest
{
	@Test
	public void testPassword() throws Exception
	{
		JsonFileSystemUserPersistence target = new JsonFileSystemUserPersistence("target/testsaving");
		target.setPassword("Richo", "MyPass");

		assertThat(target.isPasswordValid("Richo", "MyPass")).isTrue();
	}
}