package com.richodemus.reader.common.google_cloud_storage_adapter

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.util.*

internal class DiskPersistenceTest {
    @Test
    fun test() {
        val target = DiskPersistence("target/" + UUID.randomUUID().toString())

        assertThat(target.readEvents().toList()).isEmpty()

        target.persist(Event(Offset(1), Key(""), Data("heeloo")))
        target.persist(Event(Offset(2), Key(""), Data("heeloo2")))

        assertThat(target.readEvents().toList()).containsExactly(
                Event(Offset(1), Key(""), Data("heeloo")),
                Event(Offset(2), Key(""), Data("heeloo2"))
        )
    }
}
