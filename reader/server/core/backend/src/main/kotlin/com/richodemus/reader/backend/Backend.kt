package com.richodemus.reader.backend

import com.richodemus.reader.backend.exception.ItemNotInFeedException
import com.richodemus.reader.backend.exception.NoSuchChannelException
import com.richodemus.reader.backend.exception.NoSuchUserException
import com.richodemus.reader.backend.exception.UserNotSubscribedToThatChannelException
import com.richodemus.reader.backend.feed.FeedRepository
import com.richodemus.reader.backend.model.Feed
import com.richodemus.reader.backend.model.FeedWithoutItems
import com.richodemus.reader.backend.model.Item
import com.richodemus.reader.backend.subscription.SubscriptionRepository
import com.richodemus.reader.backend.user.UserRepository
import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.FeedName
import com.richodemus.reader.dto.FeedUrl
import com.richodemus.reader.dto.ItemId
import com.richodemus.reader.dto.Username
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.stream.Collectors

@Component
class Backend(private val subscriptionRepository: SubscriptionRepository,
              private val feedRepository: FeedRepository,
              private val userRepository: UserRepository) {
    private val logger = LoggerFactory.getLogger(javaClass)
    fun getFeed(username: Username, feedId: FeedId): Feed? {
        logger.debug("Getting feed {} for user {}", feedId, username)
        val userId = userRepository.getUserId(username)
        val feed = feedRepository.getFeed(feedId)
        if (feed == null) {
            logger.warn("No such feed {}", feedId)
            return null
        }
        val watchedItems = subscriptionRepository[userId, feedId]
        val items = feed.items.stream().filter { item: Item -> !watchedItems.contains(item.id) }.collect(Collectors.toList())
        return Feed(feedId, feed.name, items)
    }

    fun getAllFeedsWithoutItems(username: Username): List<FeedWithoutItems> {
        logger.debug("Getting all feeds for user {}", username)
        val userId = userRepository.getUserId(username)
        val feeds = subscriptionRepository[userId]
        val feedIds = getAllFeedsForUser(feeds)
        return mergeFeeds(feeds, feedIds)
    }

    private fun getAllFeedsForUser(feeds: List<Feed>): Map<FeedId, Feed> {
        return feeds.stream()
                .collect(Collectors.toMap({ obj: Feed -> obj.id }) { feed: Feed ->
                    val feedRepositoryFeed = feedRepository.getFeed(feed.id)
                    feedRepositoryFeed ?: Feed(feed.id, FeedName("UNKNOWN_FEED"), emptyList())
                })
    }

    private fun mergeFeeds(feeds: List<Feed>, feedsWithItems: Map<FeedId, Feed>): List<FeedWithoutItems> {
        val results: MutableList<FeedWithoutItems> = ArrayList()
        for (feed in feeds) {
            val watchedItems = ArrayList(feed.items)
            val repoFeed = feedsWithItems[feed.id]
            val itemIds = repoFeed?.items
            var numberOfUnwatchedItems = itemIds?.size ?: 0
            if (itemIds != null) {
                for (item in itemIds) {
                    for (i in watchedItems.indices) {
                        if (item.id == watchedItems[i]?.id) {
                            numberOfUnwatchedItems--
                            watchedItems.removeAt(i)
                            break
                        }
                    }
                }
            }
            if (repoFeed != null) {
                results.add(FeedWithoutItems(feed.id, repoFeed.name, numberOfUnwatchedItems))
            }
        }
        return results
    }

    @Throws(NoSuchChannelException::class, NoSuchUserException::class)
    fun addFeed(username: Username, feedUrl: FeedUrl) {
        logger.info("Add feed: {} for user {}", feedUrl, username)
        val feedId = feedRepository.getFeedId(feedUrl)
        val userId = userRepository.getUserId(username)

        //Todo its now possible to add feeds that doesnt exist...
        subscriptionRepository.subscribe(userId, feedId!!)
    }

    @Throws(NoSuchUserException::class, UserNotSubscribedToThatChannelException::class)
    fun markAsRead(username: Username, feedId: FeedId, itemId: ItemId) {
        logger.info("Marking item {} in feed {} for user {} as read", itemId, feedId, username)
        val userId = userRepository.getUserId(username)
        subscriptionRepository.markAsRead(userId, feedId, itemId)
    }

    @Throws(NoSuchUserException::class)
    fun markAsUnread(username: Username, feedId: FeedId, itemId: ItemId) {
        logger.info("Marking item {} in feed {} for user {} as unread", itemId, feedId, username)
        val userId = userRepository.getUserId(username)
        subscriptionRepository.markAsUnread(userId, feedId, itemId)
    }

    @Throws(NoSuchChannelException::class, ItemNotInFeedException::class, UserNotSubscribedToThatChannelException::class)
    fun markOlderItemsAsRead(username: Username, feedId: FeedId, itemId: ItemId) {
        logger.info("Marking items older than {} in feed {} for user {} as read", itemId, feedId, username)
        val userId = userRepository.getUserId(username)
        val feed = feedRepository.getFeed(feedId)
        if (feed == null) {
            logger.error("No such channel: {}", feedId)
            throw NoSuchChannelException("No such channel: $feedId")
        }
        val targetItem = feed.items.stream()
                .filter { item: Item -> item.id == itemId }
                .findAny()
                .orElseThrow { ItemNotInFeedException("Item $itemId is not in feed $feedId") }
        feed.items.stream()
                .filter { item: Item -> item.isBefore(targetItem) }
                .map { obj: Item -> obj.id }
                .forEach { id: ItemId -> subscriptionRepository.markAsRead(userId, feedId, id) }
    }
}
