package com.richo.reader.backend;

import com.richo.reader.backend.exception.NoSuchLabelException;
import com.richo.reader.backend.exception.NoSuchUserException;
import com.richo.reader.backend.model.User;
import com.richo.reader.backend.subscription.SubscriptionRepository;
import com.richodemus.reader.dto.FeedId;
import com.richodemus.reader.dto.Label;
import com.richodemus.reader.dto.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class LabelManager
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final SubscriptionRepository subscriptionRepository;

	@Inject
	public LabelManager(SubscriptionRepository subscriptionRepository)
	{
		this.subscriptionRepository = subscriptionRepository;
	}

	public Label createLabelForUser(UserId username, String labelName) throws NoSuchUserException
	{
		logger.info("Creating label {} for user {}", labelName, username);
		final User user = subscriptionRepository.get(username);
		final Label label = createLabel(user, labelName);
		user.addLabel(label);
		subscriptionRepository.update(user);
		return label;
	}

	private Label createLabel(User user, String labelName)
	{
		return new Label(user.incrementAndGetNextLabelId(), labelName, new ArrayList<>());
	}

	public void addFeedToLabel(UserId username, long labelId, final FeedId feedId) throws NoSuchUserException, NoSuchLabelException
	{
		logger.debug("Adding feed {} to label {} for user {}", feedId, labelId, username);
		final User user = subscriptionRepository.get(username);
		final Label label = user.getLabels().stream()
				.filter(l -> l.getId() == labelId)
				.findAny()
				.orElseThrow(() -> new NoSuchLabelException("User " + user.getName() + " does not have a label with the id " + labelId));

		//todo validate that the feed actually exists
		if (label.getFeeds().contains(feedId))
		{
			logger.info("Feed {} already in label {}, skipping...", feedId, label.getName());
			return;
		}
		label.getFeeds().add(feedId);
		subscriptionRepository.update(user);
	}

	public List<Label> getLabels(UserId username) throws NoSuchUserException
	{
		logger.info("Getting labels for user {}", username);
		final User user = subscriptionRepository.get(username);
		return user.getLabels();
	}
}
