package com.richo.reader.web;

import com.richo.reader.web.model.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class LabelConverter
{
	private final Logger logger = LoggerFactory.getLogger(getClass());

	public Label toWebLabel(com.richo.reader.backend.model.Label labelForUser)
	{
		return new Label(labelForUser.getId(), labelForUser.getName(), labelForUser.getFeeds());
	}

	public List<Label> convert(List<com.richo.reader.backend.model.Label> labels)
	{
		return labels.stream().map(this::toWebLabel).collect(Collectors.toList());
	}
}
