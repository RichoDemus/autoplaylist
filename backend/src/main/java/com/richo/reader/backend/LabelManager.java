package com.richo.reader.backend;

import com.richo.reader.backend.exception.NoSuchLabelException;
import com.richo.reader.backend.exception.NoSuchUserException;
import com.richo.reader.backend.model.Label;
import com.richo.reader.backend.model.User;
import com.richo.reader.backend.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class LabelManager
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final UserService userService;

	@Inject
	public LabelManager(UserService userService)
	{
		this.userService = userService;
	}

	public Label createLabelForUser(String username, String labelName) throws NoSuchUserException
	{
		logger.info("Creating label {} for user {}", labelName, username);
		final User user = userService.get(username);
		final Label label = createLabel(user, labelName);
		user.addLabel(label);
		userService.update(user);
		return label;
	}

	private Label createLabel(User user, String labelName)
	{
		return new Label(user.incrementAndGetNextLabelId(), labelName, new ArrayList<>());
	}

	public void addFeedToLabel(String username, long labelId, String feedId) throws NoSuchUserException, NoSuchLabelException
	{
		logger.debug("Adding feed {} to label {} for user {}", feedId, labelId, username);
		final User user = userService.get(username);
		final Label label = user.getLabels().stream()
				.filter(l -> l.getId() == labelId)
				.findAny()
				.orElseThrow(() -> new NoSuchLabelException("User " + user.getName() + " does not have a label with the id " + labelId));

		//todo validate that the feed actually exists
		label.getFeeds().add(feedId);
		userService.update(user);
	}

	public List<Label> getLabels(String username) throws NoSuchUserException
	{
		logger.info("Getting labels for user {}", username);
		final User user = userService.get(username);
		return user.getLabels();
	}
}
