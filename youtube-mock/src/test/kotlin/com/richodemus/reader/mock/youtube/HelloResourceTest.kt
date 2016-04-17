package com.richodemus.reader.mock.youtube

import com.richodemus.reader.mock.youtube.MockResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

/**
 * Placeholder :p
 */
class HelloResourceTest {
    @Test
    fun shouldSayHello() {
        val target = MockResource()
        val result = target.getChannel()
        assertThat(result).isNotEmpty()
    }
}
