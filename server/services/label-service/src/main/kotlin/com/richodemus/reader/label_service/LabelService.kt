package com.richodemus.reader.label_service

import com.richodemus.reader.dto.EventId
import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.LabelId
import com.richodemus.reader.dto.LabelName
import com.richodemus.reader.dto.UserId
import com.richodemus.reader.events.AddFeedToLabel
import com.richodemus.reader.events.CreateLabel
import io.reactivex.rxkotlin.subscribeBy
import org.slf4j.LoggerFactory
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LabelService @Inject internal constructor(val eventStore: EventStore) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val labels = mutableListOf<Label>()

    init {
        eventStore.observe().subscribeBy(
                onNext = {
                    when (it) {
                        is CreateLabel -> add(it)
                        is AddFeedToLabel -> addFeedToLabel(it)
                        else -> logger.debug("Event of type: ${it.javaClass} not handled")
                    }
                },
                onError = { logger.error("Label service event stream failure", it) },
                onComplete = { logger.info("Label service event stream closed") }
        )
    }

    fun create(name: LabelName, userId: UserId): LabelId {
        assertLabelDoesntExist(userId, name)

        val eventId = EventId(UUID.randomUUID())
        val labelId = LabelId(UUID.randomUUID())

        eventStore.add(CreateLabel(eventId, labelId, name, userId))

        return labelId
    }

    private fun add(label: CreateLabel) {
        logger.info("Creating label {}", label.labelName)
        if (labels.find { it.id == label.labelId } != null) {
            logger.warn("Label ${label.labelName} with id ${label.labelId} already exists...")
            return
        }

        labels.add(Label(label))
    }

    fun addFeedToLabel(id: LabelId, feedId: FeedId) {
        assertLabelExists(id) { "Can't add feed $feedId to non-existing label $id" }

        val eventId = EventId(UUID.randomUUID())
        eventStore.add(AddFeedToLabel(eventId, id, feedId))
    }

    private fun addFeedToLabel(event: AddFeedToLabel) {
        logger.info("Adding feed ${event.feedId} to ${event.labelId}")
        val label = labels.find { it.id == event.labelId }
        if (label == null) {
            logger.warn("Attempting to apply event $event but label doesn't exist")
            return
        }

        label.add(event)
    }

    fun get(userId: UserId): List<Label> {
        return labels.filter { it.userId == userId }
    }

    private fun assertLabelExists(labelId: LabelId, msg: () -> String) {
        if (labels.find { it.id == labelId } == null) {
            throw IllegalStateException(msg.invoke())
        }
    }

    private fun assertLabelDoesntExist(userId: UserId, name: LabelName) {
        if (labels.find { it.name == name && it.userId == userId } != null) {
            throw IllegalStateException("User $userId already has a label named $name")
        }
    }
}
