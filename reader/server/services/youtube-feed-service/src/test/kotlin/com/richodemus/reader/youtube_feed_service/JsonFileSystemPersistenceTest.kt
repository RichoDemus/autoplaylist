package com.richodemus.reader.youtube_feed_service

import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.FeedName
import com.richodemus.reader.dto.PlaylistId
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.time.OffsetDateTime
import java.util.UUID

class JsonFileSystemPersistenceTest {
    private var target: JsonFileSystemPersistence<String, String>? = null
    private var path: String? = null

    @Before
    fun setUp() {
        path = "target/data/" + UUID.randomUUID()
        target = JsonFileSystemPersistence(path!!, "filename", String::class.java)
    }

    @Test
    @Throws(IOException::class)
    fun shouldSaveStuffInTheRightPlace() {
        val path = "target/data/" + UUID.randomUUID()
        val target = JsonFileSystemPersistence<FeedId, Channel>(path, "filename", Channel::class.java)
        target.updateChannel(FeedId("id"), Channel(
                FeedId("channel-id"),
                FeedName("Channel"),
                PlaylistId("PlaylistId"),
                OffsetDateTime.parse("2007-12-03T10:15:30+01:00"),
                OffsetDateTime.parse("2007-12-03T10:15:30+01:00")
        ))
        Assert.assertTrue(File("$path/feeds/id/filename.json").exists())
        val data = Files.readString(Paths.get("$path/feeds/id/filename.json"))
        println(data)
    }

    @Test
    fun shouldBeAbleToLoadSavedData() {
        target!!.updateChannel("id", "cool-data")
        val maybeResult = target!!.getChannel("id")
        Assert.assertTrue("Should've gotten a channel", maybeResult.isPresent)
        val result = maybeResult.get()
        Assert.assertEquals("cool-data", result)
    }
}
