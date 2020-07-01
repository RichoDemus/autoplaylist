package com.richodemus.reader.backend;

import com.richodemus.reader.backend.exception.NoSuchUserException;
import com.richodemus.reader.backend.user.UserRepository;
import com.richodemus.reader.dto.FeedId;
import com.richodemus.reader.dto.Label;
import com.richodemus.reader.dto.LabelId;
import com.richodemus.reader.dto.LabelName;
import com.richodemus.reader.dto.UserId;
import com.richodemus.reader.dto.Username;
import com.richodemus.reader.label_service.LabelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class LabelManager
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final UserRepository userRepository;
	private final LabelService labelService;

	public LabelManager(final UserRepository userRepository,
						final LabelService labelService)
	{
		this.userRepository = userRepository;
		this.labelService = labelService;
	}

	public Label createLabelForUser(Username username, String labelName) throws NoSuchUserException
	{
		logger.info("Creating label {} for user {}", labelName, username);
		final UserId userId = userRepository.getUserId(username);
		final LabelName name = new LabelName(labelName);
		final com.richodemus.reader.label_service.Label label = labelService.create(name, userId);
		return new Label(label.getId(), label.getName());
	}

	public void addFeedToLabel(LabelId labelId, final FeedId feedId)
	{
		logger.debug("Adding feed {} to label {}", feedId, labelId);
		labelService.addFeedToLabel(labelId, feedId);
	}

	public List<Label> getLabels(Username username) throws NoSuchUserException
	{
		final UserId userId = userRepository.getUserId(username);
		final List<Label> labels = labelService.get(userId).stream()
				.map(label -> new Label(label.getId(), label.getName(), label.getFeeds()))
				.collect(toList());
		logger.info("Found {} labels for user {}", labels.size(), userId);
		return labels;
	}
}
