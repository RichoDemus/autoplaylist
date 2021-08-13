package com.richodemus.reader.backend

import com.richodemus.reader.backend.exception.NoSuchUserException
import com.richodemus.reader.backend.user.UserRepository
import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.Label
import com.richodemus.reader.dto.LabelId
import com.richodemus.reader.dto.LabelName
import com.richodemus.reader.dto.Username
import com.richodemus.reader.label_service.LabelService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.stream.Collectors

@Service
class LabelManager(private val userRepository: UserRepository,
                   private val labelService: LabelService) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Throws(NoSuchUserException::class)
    fun createLabelForUser(username: Username, labelName: String?): Label {
        logger.info("Creating label {} for user {}", labelName, username)
        val userId = userRepository.getUserId(username)
        val name = LabelName(labelName!!)
        val (id, name1) = labelService.create(name, userId!!)
        return Label(id, name1)
    }

    fun addFeedToLabel(labelId: LabelId?, feedId: FeedId?) {
        logger.debug("Adding feed {} to label {}", feedId, labelId)
        labelService.addFeedToLabel(labelId!!, feedId!!)
    }

    @Throws(NoSuchUserException::class)
    fun getLabels(username: Username): List<Label> {
        val userId = userRepository.getUserId(username)
        val labels = labelService.get(userId!!).stream()
                .map { (id, name, _, feeds): com.richodemus.reader.label_service.Label -> Label(id, name, feeds) }
                .collect(Collectors.toList())
        logger.info("Found {} labels for user {}", labels.size, userId)
        return labels
    }
}
