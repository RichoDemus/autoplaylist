package com.richodemus.reader.mock.youtube

import com.google.common.io.Resources
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Path("youtube/v3/")
@Produces(MediaType.APPLICATION_JSON)
class MockResource {
    @GET
    @Path("channels")
    fun getChannel(): String = getResourceAsString("getChannelResponse.json")

    @GET
    @Path("playlistItems")
    fun getListItems(): String = getResourceAsString("getListItemsResponseTwoVideos.json")

    @GET
    @Path("videos")
    fun getVideoInfo(): String = getResourceAsString("getVideoDetailsResponse.json")

    private fun getResourceAsString(path: String) = path
            .let { Resources.getResource(it) }
            .let { Resources.toString(it, Charsets.UTF_8) }
}
