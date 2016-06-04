package com.richodemus.reader.mock.youtube

import com.google.common.io.Resources
import org.slf4j.LoggerFactory
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Path("youtube/v3/")
@Produces(MediaType.APPLICATION_JSON)
class MockResource {
    private val logger = LoggerFactory.getLogger(this.javaClass.name)

    @GET
    @Path("channels")
    fun getChannel() =
            listOf("getChannelResponse.json")
                    .map { Resources.getResource(it) }
                    .map { Resources.toString(it, Charsets.UTF_8) }
                    .first()


    @GET
    @Path("playlistItems")
    fun getListItems() =
            listOf("getListItemsResponseTwoVideos.json")
                    .map { Resources.getResource(it) }
                    .map { Resources.toString(it, Charsets.UTF_8) }
                    .first()

}

// /youtube/v3/channels?forUsername=richodemus&key=AIzaSyChI7lMyLfc1ckOqcC-z2Oz-Lrq6d09x30&part=contentDetails


// /youtube/v3/playlistItems?key=AIzaSyChI7lMyLfc1ckOqcC-z2Oz-Lrq6d09x30&maxResults=50&pageToken&part=snippet&playlistId=UUyPvQQ-dZmKzh_PrpWmTJkw