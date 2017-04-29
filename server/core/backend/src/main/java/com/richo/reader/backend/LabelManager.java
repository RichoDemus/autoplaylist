package com.richo.reader.backend;

import com.richo.reader.backend.exception.NoSuchLabelException;
import com.richo.reader.backend.exception.NoSuchUserException;
import com.richodemus.reader.dto.FeedId;
import com.richodemus.reader.dto.Label;
import com.richodemus.reader.dto.LabelId;
import com.richodemus.reader.dto.LabelName;
import com.richodemus.reader.dto.Username;
import com.richodemus.reader.label_service.LabelService;
import com.richodemus.reader.user_service.User;
import com.richodemus.reader.user_service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class LabelManager
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final UserService userService;
	private final LabelService labelService;

	@Inject
	public LabelManager(final UserService userService,
						final LabelService labelService)
	{
		this.userService = userService;
		this.labelService = labelService;
	}

	public Label createLabelForUser(Username username, String labelName) throws NoSuchUserException
	{
		logger.info("Creating label {} for user {}", labelName, username);
		final User user = userService.find(username);
		if (user == null)
		{
			throw new NoSuchUserException("No such user: " + username);
		}
		final LabelName name = new LabelName(labelName);
		final LabelId labelId = labelService.create(name, user.getId());
		return new Label(labelId, name);
	}

	public void addFeedToLabel(LabelId labelId, final FeedId feedId) throws NoSuchUserException, NoSuchLabelException
	{
		logger.debug("Adding feed {} to label {}", feedId, labelId);
		labelService.addFeedToLabel(labelId, feedId);
	}

	public List<Label> getLabels(Username username) throws NoSuchUserException
	{
		final User user = userService.find(username);
		if (user == null)
		{
			throw new NoSuchUserException("No such user: " + username);
		}
		final List<Label> labels = labelService.get(user.getId()).stream()
				.map(label -> new Label(label.getId(), label.getName(), label.getFeeds()))
				.collect(toList());
		logger.info("Found {} labels for user {} ({})", labels.size(), user.getUsername(), user.getId());
		return labels;
	}
}
