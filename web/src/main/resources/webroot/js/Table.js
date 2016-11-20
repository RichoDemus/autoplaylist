var Table = (function()
{
	var pub = {},
	//Private property
	greyFloorTile = null;

	//Public property
	//pub.ingredient = "Bacon Strips";

	//Public method
	pub.init = function ()
	{
		$('#itemListTable').DataTable({
			"paging": false,
			"searching": false,
			"ordering": true,
			"order": [[ 3, "asc" ]]
		});
	};

	pub.addItemToTable = function (feedId, item)
	{
		$('#itemListTable').DataTable().row.add([
			Table.getMarkAsReadToggleButton(feedId, item),
			Table.getTitle(feedId, item),
			item.description.substring(0, 10),
			item.uploadDate,
			getMarkOlderItemsAsReadButton(feedId, item)
		]).draw(false);
	};

	pub.addFeedToTable = function(feed)
	{
		const count = getNumberOfItems(feed);
		if(feed.id === selectedFeed)
		{
			$('#feedListTable').find('> tbody:last')
				.append("<tr data-feed-id=\"" + feed.id + "\" onclick=\"Buttons.filterFeedButtonClicked(this)\"><td bgcolor=\"#FF0000\">" + feed.name + "(" + count + ")</td></tr>");
		}
		else
		{
			$('#feedListTable').find('> tbody:last')
				.append("<tr data-feed-id=\"" + feed.id + "\" onclick=\"Buttons.filterFeedButtonClicked(this)\"><td>" + feed.name + "(" + count + ")</td></tr>");
		}
	};

	pub.addItemsToTable = function()
	{
		if(!selectedFeed)
		{
			console.log("No feed selected");
			return;
		}
		feeds.forEach(function(feed)
		{
			if(feed.id === selectedFeed)
			{
				feed.items.forEach(function(item)
				{
					Table.addItemToTable(feed.id, item);
				});
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
		});
	};

	pub.clearTables = function()
	{
		$("#feedListTable").find("tbody tr").remove();
		$("#labelListTable").find("tbody tr").remove();
		$('#itemListTable').dataTable().fnClearTable();
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

	//todo this should be a method of the feed object
	function getNumberOfItems(feed)
	{
		if(feed.items.length > 0)
			return feed.items.length;
		return feed.numberOfAvailableItems;
	}
	/*
	function privateWay() {
		console.log("private method");
	}
	*/
	//Return just the public parts
	return pub;
}());
