package com.richo.reader.backend.user;

import com.richodemus.reader.dto.UserId;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonFileSystemUserPersistenceTest
{
	@Test
	public void testPassword() throws Exception
	{
		JsonFileSystemUserPersistence target = new JsonFileSystemUserPersistence("target/testsaving");
		target.setPassword(new UserId("Richo"), "MyPass");

		assertThat(target.isPasswordValid(new UserId("Richo"), "MyPass")).isTrue();
	}
}