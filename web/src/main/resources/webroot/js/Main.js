const UNLABELED_LABEL = {id: -1, name: "Unlabeled", feeds: []};
const ALL_LABEL = {id: -2, name: "All", feeds: []};

var feeds = null;
var labels = null;
var selectedFeed = null;
var selectedLabel = null;

const getAllItems = function()
{
     Api.getAllItems(username, token, function(result)
     {
     	feeds = result.feeds;
     	labels = result.labels;
     	labels.push(UNLABELED_LABEL);
     	labels.push(ALL_LABEL);
     	labels.sort(function(a,b)
     	{
     		return a.id - b.id;
     	});
     	updateEverything();
     });
};

const updateEverything = function()
{
	clearTables();
	addLabelsToTable();
	addFeedsToTable();
	addItemsToTable();
};

const clearTables = function()
{
	$("#feedListTable").find("tbody tr").remove();
	$("#itemListTable").find("tbody tr").remove();
	$("#labelListTable").find("tbody tr").remove();
};

const addLabelsToTable = function()
{
	labels.forEach(function(label)
	{
		addLabelToTable(label);
	});
};

const addLabelToTable = function(label)
{
	$('#labelListTable').find('> tbody:last')
		.append("<tr data-label-id=\"" + label.id + "\" onclick=\"labelClicked(this)\"><th>" + label.name + "(" + "x" + ")</th></tr>");
};

const addFeedsToTable = function()
{
	feeds.forEach(function(feed)
    {
 		if(selectedLabel && selectedLabel.id === UNLABELED_LABEL.id)
 		{
 			//add feed if it doesnt belong in any label
 			var hasLabel = false;
 			for(i = 0; i < labels.length; i++)
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
				addFeedToTable(feed);
			}
 		}
 		else if((selectedLabel && selectedLabel.feeds.indexOf(feed.id) > -1) || !selectedLabel)
 		{
 			addFeedToTable(feed);
 		}
 		else
 		{
 			console.log("label is [" + selectedLabel.name + "] will not add [" + feed.name + "]")
 		}
    });
};

const addItemsToTable = function()
{
	feeds.forEach(function(feed)
	{
		if((selectedFeed && feed.id === selectedFeed) || !selectedFeed)
		{
			const items = feed.items;
			items.sort(comparator);
			items.reverse();
			items.forEach(function(item)
			{
				addItemToTable(feed.id, item);
			});
		}
		else
		{
			console.log("filter is [" + selectedFeed + "] will not add [" + feed.name + "]");
		}
	});
};

const comparator = function(a,b)
{
	const a_date = new Date(a.uploadDate);
	const b_date = new Date(b.uploadDate);
	return a_date - b_date;
};

const addFeedToTable = function(feed)
{
    $('#feedListTable').find('> tbody:last')
    	.append("<tr data-feed-id=\"" + feed.id + "\" onclick=\"filterFeedButtonClicked(this)\"><th>" + feed.name + "(" + feed.items.length + ")</th></tr>");
};

const labelClicked = function(label)
{
	const newSelectedLabelLong = label.getAttribute("data-label-id");
	if(newSelectedLabelLong == ALL_LABEL.id)
	{
		selectedLabel = null;
		updateEverything();
		return;
	}
	const newSelectedLabel = labels.filter(function(e) {return e.id == newSelectedLabelLong;})[0];
	console.log(newSelectedLabel);
	selectedLabel = newSelectedLabel;
	console.log(selectedLabel);
	console.log("Now only showing feeds beloning to label: " + selectedLabel.name);
	updateEverything();
};

var addItemToTable = function(feedId, item)
{
	$('#itemListTable').find('> tbody:last').append(
	    "<tr data-feed-id=\"" + feedId + "\" data-item-id=\"" + item.id + "\" id=\"video-" + item.id + "\"><th>" + getMarkAsReadToggleButton(feedId, item) + "</th><th>" + getTitle(feedId, item) + "</th><th>" + item.description.substring(0,10) + "</th><th>" + item.uploadDate + "</th></tr>");
};

var getTitle = function(feedId, item)
{
    return "<a id=\"" + item.id + "\" data-feed-id=\"" + feedId + "\" data-id=\"" + item.id + "\" href=\"" + item.url + "\" target=\"_blank\" onClick=\"itemTitleClicked(this)\">" + item.title + "</a>";
};

var getMarkAsReadToggleButton = function(feedId, item)
{
    return "<button id=\"markItemAsReadButton-" + item.id + "\" data-feed-id=\"" + feedId + "\" data-id=\"" + item.id + "\" type=\"button\" class=\"btn btn-xs btn-default\" onClick=\"markAsReadButtonPressed(this)\">Mark as read</button>" +
        "<button id=\"markItemAsUnreadButton-" + item.id + "\" data-feed-id=\"" + feedId + "\" data-id=\"" + item.id + "\" type=\"button\" class=\"btn btn-xs btn-default\" onClick=\"markAsUnreadButtonPressed(this)\" style=\"display:none;\" >Mark as unread</button>";
};

var itemTitleClicked = function(item)
{
    console.log("Clicked link to video: " + item.getAttribute("data-id"));
    Api.markAsRead(item.getAttribute("data-feed-id"), item.getAttribute("data-id"));
};

var markAsReadButtonPressed = function(item)
{
    $("#markItemAsReadButton-" + item.getAttribute("data-id")).hide();
    $("#markItemAsUnreadButton-" + item.getAttribute("data-id")).show();
    Api.markAsRead(item.getAttribute("data-feed-id"), item.getAttribute("data-id"));
};

var markAsUnreadButtonPressed = function(item)
{
    $("#markItemAsReadButton-" + item.getAttribute("data-id")).show();
    $("#markItemAsUnreadButton-" + item.getAttribute("data-id")).hide();
    Api.markAsUnread(item.getAttribute("data-feed-id"), item.getAttribute("data-id"));
};

const filterFeedButtonClicked = function(feed)
{
	const newselectedFeed = feed.getAttribute("data-feed-id");
	if(selectedFeed === newselectedFeed)
	{
		selectedFeed = null;
	}
	else
	{
		selectedFeed = newselectedFeed;
	}
	updateEverything();
};

const addFeed = function()
{
    const feedName = $("#addFeedButton").val();
    console.log("Add feed: " + feedName);
    Api.addFeed(feedName);
};

const createLabel = function()
{
    const feedName = $("#createLabelInput").val();
    console.log("Create label: " + feedName);
    Api.createLabel(feedName, function(result)
    {
    	labels.push(result);
    	updateEverything();
    });
};

const addFeedToLabel = function()
{
    const feed = $("#feedToBeAddedToLabelInput").val();
    const labelName = $("#labelForFeedToBeAddedTo").val();
    const label = labels.filter(function(e) {return e.name == labelName;})[0];
    console.log("Add feed " + feed + "(" + label.id + ") to label: " + labelName);
    Api.addFeedToLabel(feed, label.id, function(result)
    {
    	console.log("Callback from label thing")
    	console.log(result);
    	label.feeds.push(feed);

    });
};
