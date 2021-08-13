package com.richodemus.reader.backend

import com.richodemus.reader.backend.exception.NoSuchUserException
import com.richodemus.reader.backend.feed.FeedRepository
import com.richodemus.reader.backend.model.Feed
import com.richodemus.reader.backend.model.FeedWithoutItems
import com.richodemus.reader.backend.model.Item
import com.richodemus.reader.backend.model.User
import com.richodemus.reader.backend.subscription.SubscriptionRepository
import com.richodemus.reader.backend.user.UserRepository
import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.FeedName
import com.richodemus.reader.dto.FeedUrl
import com.richodemus.reader.dto.ItemId
import com.richodemus.reader.dto.UserId
import com.richodemus.reader.dto.Username
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import java.time.Duration
import java.util.Arrays
import java.util.stream.Collectors
import java.util.stream.Stream

class BackendTest {
    companion object {
        private val NON_EXISTING_USER = Username("non_existing_user")
        private val ITEM_THAT_SHOULD_BE_READ = Item(ItemId("item-id-1"), "item-title-1", "item-desc-1", "2017-01-01", "http", Duration.ZERO, 0L)
        private val ITEM_TO_MARK_AS_READ = Item(ItemId("item-id-2"), "item-title-2", "item-desc-2", "2017-01-02", "http", Duration.ZERO, 0L)
        private val FEED_1 = Feed(
                FeedId("existing_feed_id"),
                FeedName("name"),
                Arrays.asList(
                        ITEM_THAT_SHOULD_BE_READ,
                        ITEM_TO_MARK_AS_READ,
                        Item(ItemId("item-id-3"), "item-title-3", "item-desc-3", "2017-01-03", "http", Duration.ZERO, 0L),
                        Item(ItemId("item-id-4"), "item-title-4", "item-desc-4", "2017-01-04", "http", Duration.ZERO, 0L)
                ))
        private val FEED_2 = Feed(
                FeedId("feed_2"),
                FeedName("name"), listOf(Item(ItemId("feed2-item1"), "title", "desc", "2017-01-01", "http", Duration.ZERO, 0L)))
        private val EXISTING_USER = User(UserId("id"), Username("existing_user"), 0L, mutableMapOf(Pair(FEED_1.id, mutableSetOf(ITEM_THAT_SHOULD_BE_READ.id)), Pair(FEED_2.id, mutableSetOf())))

    }

    private var target: Backend? = null
    private var subscriptionRepository: SubscriptionRepository? = null
    private var userRepository: UserRepository? = null
    private var feedRepository: FeedRepository? = null

    @Before
    fun setUp() {
        feedRepository = Mockito.mock(FeedRepository::class.java)
        subscriptionRepository = Mockito.mock(SubscriptionRepository::class.java)
        userRepository = Mockito.mock(UserRepository::class.java)
        target = Backend(subscriptionRepository!!, feedRepository!!, userRepository!!)
        Mockito.`when`(userRepository!!.getUserId(EXISTING_USER.name)).thenReturn(EXISTING_USER.id)
        Mockito.`when`(userRepository!!.getUserId(NON_EXISTING_USER)).thenThrow(NoSuchUserException::class.java)
        Mockito.`when`(feedRepository!!.getFeed(FEED_1.id)).thenReturn(FEED_1)
        Mockito.`when`(feedRepository!!.getFeed(FEED_2.id)).thenReturn(FEED_2)
        Mockito.`when`(subscriptionRepository!!.get(EXISTING_USER.id)).thenReturn(Arrays.asList(FEED_1, FEED_2))
    }

    @Test
    fun testAddFeed() {
        val url = FeedUrl("http://asd.com")
        val id = FeedId("id")
        Mockito.`when`(feedRepository!!.getFeedId(url)).thenReturn(id)
        target!!.addFeed(EXISTING_USER.name, url)
        Mockito.verify(subscriptionRepository)!!.subscribe(EXISTING_USER.id, id)
    }

    @Test
    fun feedsShouldReturnSubscribedFeeds(){
            val expected = Stream.of(FEED_1, FEED_2)
                    .map(Feed::id)
                    .collect(Collectors.toList())
            val result = target!!.getAllFeedsWithoutItems(EXISTING_USER.name)
            Assertions.assertThat(result).extracting<FeedId, RuntimeException>(FeedWithoutItems::id).isEqualTo(expected)
        }

    @Test(expected = NoSuchUserException::class)
    fun feedShouldThrowNoSuchUserExceptionIfUserDoesntExist(){
            target!!.getFeed(NON_EXISTING_USER, FEED_1.id)
        }

    @Test(expected = NoSuchUserException::class)
    fun allFeedsWithoutItemsShouldThrowNoSuchUserExceptionIfUserDoesntExist() {
            target!!.getAllFeedsWithoutItems(NON_EXISTING_USER)
        }

    @Test
    fun shouldMarkItemAsRead() {
        target!!.markAsRead(EXISTING_USER.name, FEED_1.id, ITEM_TO_MARK_AS_READ.id)
        Mockito.verify(subscriptionRepository)!!.markAsRead(EXISTING_USER.id, FEED_1.id, ITEM_TO_MARK_AS_READ.id)
    }

    @Test
    fun shouldMarkItemAsUnread() {
        target!!.markAsUnread(EXISTING_USER.name, FEED_1.id, ITEM_TO_MARK_AS_READ.id)
        Mockito.verify(subscriptionRepository)!!.markAsUnread(EXISTING_USER.id, FEED_1.id, ITEM_TO_MARK_AS_READ.id)
    }

    @Test
    fun markOlderItemsAsUnreadShouldLeadToOlderItemsNotBeingReturned() {
        val id = ItemId("item-id-4")
        target!!.markOlderItemsAsRead(EXISTING_USER.name, FEED_1.id, id)
        Mockito.verify(subscriptionRepository)!!.markAsRead(EXISTING_USER.id, FEED_1.id, ItemId("item-id-1"))
        Mockito.verify(subscriptionRepository)!!.markAsRead(EXISTING_USER.id, FEED_1.id, ItemId("item-id-2"))
        Mockito.verify(subscriptionRepository)!!.markAsRead(EXISTING_USER.id, FEED_1.id, ItemId("item-id-3"))
        Mockito.verify(subscriptionRepository, Mockito.never())!!.markAsRead(EXISTING_USER.id, FEED_1.id, ItemId("item-id-4"))
    }
}
