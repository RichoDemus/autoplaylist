var Table = (function()
{
	var pub = {},
	//Private property
	greyFloorTile = null;

	//Public property
	//pub.ingredient = "Bacon Strips";

	//Public method
	pub.addItemToTable = function(feedId, item)
	{
		$('#itemListTable').find('> tbody:last').append(
			"<tr data-feed-id=\"" + feedId + "\" data-item-id=\"" + item.id + "\" id=\"video-" + item.id + "\"><th>" + Table.getMarkAsReadToggleButton(feedId, item) + "</th><th>" + Table.getTitle(feedId, item) + "</th><th>" + item.description.substring(0,10) + "</th><th>" + item.uploadDate + "</th><th>" + getMarkOlderItemsAsReadButton(feedId, item) + "</th></tr>");
	};

	pub.addFeedToTable = function(feed)
	{
		if(feed.id === selectedFeed)
		{
			$('#feedListTable').find('> tbody:last')
				.append("<tr data-feed-id=\"" + feed.id + "\" onclick=\"Buttons.filterFeedButtonClicked(this)\"><td bgcolor=\"#FF0000\">" + feed.name + "(" + feed.items.length + ")</td></tr>");
		}
		else
		{
			$('#feedListTable').find('> tbody:last')
				.append("<tr data-feed-id=\"" + feed.id + "\" onclick=\"Buttons.filterFeedButtonClicked(this)\"><td>" + feed.name + "(" + feed.items.length + ")</td></tr>");
		}
	};

	pub.addItemsToTable = function()
	{
		if(!selectedFeed)
		{
			console.log("No feed selected")
			return;
		}
		feeds.forEach(function(feed)
		{
			if(feed.id === selectedFeed)
			{
				const items = feed.items;
				items.sort(function(a,b)
				{
					const a_date = new Date(a.uploadDate);
					const b_date = new Date(b.uploadDate);
					return a_date - b_date;
				});
				if(Service.sortOrder == SortOrder.OLDEST_FIRST)
					console.log("Order is oldest first, already sorted that way");
				else
					items.reverse();
				items.forEach(function(item)
				{
					Table.addItemToTable(feed.id, item);
				});
			}
			else
			{
				console.log("filter is [" + selectedFeed + "] will not add [" + feed.name + "]");
			}
		});
	};

	pub.addFeedsToTable = function()
	{
		feeds.forEach(function(feed)
		{
			if(selectedLabel && selectedLabel.id === UNLABELED_LABEL.id)
			{
				//add feed if it doesnt belong in any label
				var hasLabel = false;
				for(var i = 0; i < labels.length; i++)
				{
					var label = labels[i];
					console.log(label);
					if(label.feeds.indexOf(feed.id) > -1)
					{
						//this feed belong to a label
						console.log("Feed [" + feed.name + "] is in label [" + label.name + "]");
						hasLabel = true;
						break;
					}
				}
				if(!hasLabel)
				{
					console.log("Feed [" + feed.name + "] is not in any label");
					Table.addFeedToTable(feed);
				}
			}
			else if((selectedLabel && selectedLabel.feeds.indexOf(feed.id) > -1) || !selectedLabel)
			{
				Table.addFeedToTable(feed);
			}
			else if(selectedLabel && selectedLabel === ALL_LABEL)
			{
				Table.addFeedToTable(feed);
			}
			else
			{
				console.log("label is [" + selectedLabel.name + "] will not add [" + feed.name + "]")
			}
		});
	};

	pub.clearTables = function()
	{
		$("#feedListTable").find("tbody tr").remove();
		$("#itemListTable").find("tbody tr").remove();
		$("#labelListTable").find("tbody tr").remove();
	};

	pub.addLabelToTable = function(label)
	{
		if(selectedLabel && label.id === selectedLabel.id)
		{
			$('#labelListTable').find('> tbody:last')
				.append("<tr data-label-id=\"" + label.id + "\" onclick=\"Buttons.labelClicked(this)\"><td bgcolor=\"#FF0000\">" + label.name + "(" + "x" + ")</td></tr>");
		}
		else
		{
			$('#labelListTable').find('> tbody:last')
				.append("<tr data-label-id=\"" + label.id + "\" onclick=\"Buttons.labelClicked(this)\"><td>" + label.name + "(" + "x" + ")</td></tr>");
		}
	};

	pub.addLabelsToTable = function()
	{
		labels.forEach(function(label)
		{
			Table.addLabelToTable(label);
		});
	};

	pub.getMarkAsReadToggleButton = function(feedId, item)
	{
		return "<button id=\"markItemAsReadButton-" + item.id + "\" data-feed-id=\"" + feedId + "\" data-id=\"" + item.id + "\" type=\"button\" class=\"btn btn-xs btn-default\" onClick=\"Buttons.markAsReadButtonPressed(this)\">Mark as read</button>" +
			"<button id=\"markItemAsUnreadButton-" + item.id + "\" data-feed-id=\"" + feedId + "\" data-id=\"" + item.id + "\" type=\"button\" class=\"btn btn-xs btn-default\" onClick=\"Buttons.markAsUnreadButtonPressed(this)\" style=\"display:none;\" >Mark as unread</button>";
	};

	pub.getTitle = function(feedId, item)
	{
		return "<a id=\"" + item.id + "\" data-feed-id=\"" + feedId + "\" data-id=\"" + item.id + "\" href=\"" + item.url + "\" target=\"_blank\" onClick=\"Buttons.itemTitleClicked(this)\">" + item.title + "</a>";
	};

	//Private method
	function getMarkOlderItemsAsReadButton(feedId, item)
	{
		return "<button id=\"markAllOlderItemsAsReadButton-" + item.id + "\" data-feed-id=\"" + feedId + "\" data-id=\"" + item.id + "\" type=\"button\" class=\"btn btn-xs btn-default\" onClick=\"Buttons.markOlderItemsAsReadButtonPressed(this)\">Mark older as read</button>"
	}
	/*
	function privateWay() {
		console.log("private method");
	}
	*/
	//Return just the public parts
	return pub;
}());
