package com.richo.reader.web;

import com.richo.reader.model.Label;

import java.util.List;
import java.util.stream.Collectors;

public class LabelConverter
{
	public Label toWebLabel(com.richo.reader.backend.model.Label labelForUser)
	{
		return new Label(labelForUser.getId(), labelForUser.getName(), labelForUser.getFeeds());
	}

	public List<Label> convert(List<com.richo.reader.backend.model.Label> labels)
	{
		return labels.stream().map(this::toWebLabel).collect(Collectors.toList());
	}
}
