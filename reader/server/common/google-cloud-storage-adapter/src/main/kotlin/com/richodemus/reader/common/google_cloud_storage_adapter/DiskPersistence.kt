package com.richodemus.reader.common.google_cloud_storage_adapter

import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.io.File

@Profile(value = ["dev"])
@Component
internal class DiskPersistence(
        private val directory: String = "data"
) : Persistence {
    private val eventDeserializer = EventDeserializer()
    private val eventSerializer = EventSerializer()

    init {
        File("$directory/events").mkdirs()
    }

    override fun readEvents(): Sequence<Event> {
        return File("$directory/events/").walk().sorted()
                .filter { it.isFile }
                .map { file ->
                    val offset = file.name.toLong()
                    val key = ""
                    val data = file.readText()
                    Event(Offset(offset), Key(key), Data(data))
                }
    }

    override fun persist(event: Event) {
        val filename = event.offset.value
        File("$directory/events/$filename").writeText(event.data.value)
    }

}