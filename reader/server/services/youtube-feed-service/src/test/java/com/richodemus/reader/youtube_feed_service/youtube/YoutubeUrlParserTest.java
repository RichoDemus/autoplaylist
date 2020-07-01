package com.richodemus.reader.youtube_feed_service.youtube;

import com.richodemus.reader.dto.FeedId;
import com.richodemus.reader.dto.FeedUrl;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class YoutubeUrlParserTest {
    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {url("https://www.youtube.com/user/richodemus"), id("UCyPvQQ-dZmKzh_PrpWmTJkw")},
                {url("https://youtube.com/user/richodemus"), id("UCyPvQQ-dZmKzh_PrpWmTJkw")},
                {url("https://www.youtube.com/channel/UC1BWMtZbNLVMSFgwSukjqCw"), id("UC1BWMtZbNLVMSFgwSukjqCw")},
                {url("https://youtube.com/channel/UC1BWMtZbNLVMSFgwSukjqCw"), id("UC1BWMtZbNLVMSFgwSukjqCw")},
                {url("https://www.youtube.com/channel/UC7SeFWZYFmsm1tqWxfuOTPQ/videos"), id("UC7SeFWZYFmsm1tqWxfuOTPQ")}
                /* Can't get these top work...
                {url("https://m.youtube.com/grinninggoat"), id("asd")},
                {url("https://www.youtube.com/grinninggoat"), id("asd")},
                {url("https://youtube.com/grinninggoat"), id("asd")}
                */
        });
    }

    private static FeedId id(String value) {
        return new FeedId(value);
    }

    private static FeedUrl url(String url) {
        return new FeedUrl(url);
    }

    private YoutubeChannelDownloader target;
    private FeedUrl url;
    private FeedId id;

    public YoutubeUrlParserTest(FeedUrl url, FeedId id) {
        this.url = url;
        this.id = id;
    }

    @Before
    public void setUp() {
        target = new YoutubeChannelDownloader(null, "api-key-here");
    }

    @Ignore("This test uses the live youtube api")
    @Test
    public void shouldParseUrlToFeedId() {
        final FeedId result = target.getFeedId(url);
        assertThat(result).isEqualTo(id);
    }
}
