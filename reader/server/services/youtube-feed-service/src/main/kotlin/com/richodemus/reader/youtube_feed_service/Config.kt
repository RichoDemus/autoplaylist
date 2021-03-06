package com.richodemus.reader.youtube_feed_service

import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.PlaylistId
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration
internal open class Config {
    @Bean
    @Qualifier("videoCache")
    // todo use feed here aswell
    open fun videoCache(@Value("\${saveRoot}") saveRoot: String): Cache<PlaylistId, Videos> {
        return Cache(JsonFileSystemPersistence(saveRoot, "videos", Videos::class.java))
    }

    @Bean
    @Qualifier("channelCache")
    open fun channelCache(@Value("\${saveRoot}") saveRoot: String): Cache<FeedId, Channel> {
        return Cache(JsonFileSystemPersistence(saveRoot, "channel", Channel::class.java))
    }

    @Bean
    open fun clock(): Clock {
        return Clock.systemDefaultZone()
    }
}
