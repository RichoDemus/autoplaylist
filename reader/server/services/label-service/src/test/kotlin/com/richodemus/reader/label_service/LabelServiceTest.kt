package com.richodemus.reader.label_service

import com.richodemus.reader.common.google_cloud_storage_adapter.InMemoryEventStore
import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.LabelId
import com.richodemus.reader.dto.LabelName
import com.richodemus.reader.dto.UserId
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Before
import org.junit.Test
import java.util.UUID

class LabelServiceTest {
    private val userId = UserId("user-id")
    private val feedId = FeedId("user-id")
    private val labelName = LabelName("a-label")

    private var target: LabelService? = null
    private fun target() = target!!

    @Before
    fun setUp() {
        target = LabelService(InMemoryEventStore())
    }

    @Test
    fun `Should return an empty list if there are no labels for that user`() {
        assertThat(target().get(userId)).isEmpty()
    }

    @Test
    fun `Should create label`() {
        val id = target().create(labelName, userId)

        val result = target().get(userId)

        assertThat(result).isNotEmpty
        assertThat(result.single().id).isEqualTo(id.id)
        assertThat(result.single().name).isEqualTo(labelName)
    }

    @Test
    fun `Shouldn't be possible to create two labels with the same name`() {
        target().create(labelName, userId)
        assertThatThrownBy { target().create(labelName, userId) }.isInstanceOf(IllegalStateException::class.java).hasMessageContaining("already has a label named")
    }

    @Test
    fun `Newly created label should be empty`() {
        target().create(labelName, userId)

        val result = target().get(userId).single().feeds

        assertThat(result).isEmpty()
    }

    @Test
    fun `Should add feed to label`() {
        val id = target().create(labelName, userId)
        target().addFeedToLabel(id.id, feedId)

        val result = target().get(userId).single().feeds.single()

        assertThat(result).isEqualTo(feedId)
    }

    @Test
    fun `Should not be possible to add feed to non existing label`() {
        assertThatThrownBy { target().addFeedToLabel(LabelId(UUID.randomUUID()), feedId) }.isInstanceOf(IllegalStateException::class.java).hasMessageContaining("to non-existing label")
    }
}
