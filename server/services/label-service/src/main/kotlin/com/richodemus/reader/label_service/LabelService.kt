package com.richodemus.reader.label_service

import com.richodemus.reader.common.kafka_adapter.EventStore
import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.LabelId
import com.richodemus.reader.dto.LabelName
import com.richodemus.reader.dto.UserId
import com.richodemus.reader.events_v2.FeedAddedToLabel
import com.richodemus.reader.events_v2.LabelCreated
import org.slf4j.LoggerFactory
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LabelService @Inject internal constructor(private val eventStore: EventStore) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private var labels = emptyList<Label>()

    init {
        eventStore.consume {
            when (it) {
                is LabelCreated -> add(it)
                is FeedAddedToLabel -> addFeedToLabel(it)
                else -> logger.debug("Event of type: ${it.javaClass} not handled")
            }
        }
    }

    fun create(name: LabelName, userId: UserId): LabelId {
        assertLabelDoesntExist(userId, name)

        val labelId = LabelId(UUID.randomUUID())

        eventStore.produce(LabelCreated(labelId, name, userId))

        return labelId
    }

    private fun add(label: LabelCreated) {
        logger.info("Creating label {}", label.labelName)
        if (labels.find { it.id == label.labelId } != null) {
            logger.warn("Label ${label.labelName} with id ${label.labelId} already exists...")
            return
        }

        labels = labels.plus(Label(label))
    }

    fun addFeedToLabel(id: LabelId, feedId: FeedId) {
        assertLabelExists(id) { "Can't add feed $feedId to non-existing label $id" }

        eventStore.produce(FeedAddedToLabel(id, feedId))
    }

    private fun addFeedToLabel(event: FeedAddedToLabel) {
        logger.info("Adding feed ${event.feedId} to ${event.labelId}")
        val label = labels.find { it.id == event.labelId }
        if (label == null) {
            logger.warn("Attempting to apply event $event but label doesn't exist")
            return
        }

        labels = labels.map { it.process(event) }
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
