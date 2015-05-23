
$(function() {
     Api.getAllItems(function(result)
     {
        result.feeds.forEach(function(feed)
        {
            const items = feed.items;
            items.sort(comparator);
            items.reverse();
            items.forEach(function(item)
            {
                addItemToTable(feed.id, item);
            });
        });
     });
});

const comparator = function(a,b)
{
	const a_date = new Date(a.uploadDate);
	const b_date = new Date(b.uploadDate);
	return a_date - b_date;
}

var addItemToTable = function(feedId, item)
{
	$('#itemListTable > tbody:last').append(
	    "<tr data-feed-id=\"" + feedId + "\" data-item-id=\"" + item.id + "\" id=\"video-" + item.id + "\"><th>" + getMarkAsReadToggleButton(feedId, item) + "</th><th>" + getTitle(feedId, item) + "</th><th>" + item.description + "</th><th>" + item.uploadDate + "</th></th>");
}

var getTitle = function(feedId, item)
{
    return "<a id=\"" + item.id + "\" data-feed-id=\"" + feedId + "\" data-id=\"" + item.id + "\" href=\"" + item.url + "\" target=\"_blank\" onClick=\"itemTitleClicked(this)\">" + item.title + "</a>";
}

var getMarkAsReadToggleButton = function(feedId, item)
{
    return "<button id=\"markItemAsReadButton-" + item.id + "\" data-feed-id=\"" + feedId + "\" data-id=\"" + item.id + "\" type=\"button\" class=\"btn btn-xs btn-default\" onClick=\"markAsReadButtonPressed(this)\">Mark as read</button>" +
        "<button id=\"markItemAsUnreadButton-" + item.id + "\" data-feed-id=\"" + feedId + "\" data-id=\"" + item.id + "\" type=\"button\" class=\"btn btn-xs btn-default\" onClick=\"markAsUnreadButtonPressed(this)\" style=\"display:none;\" >Mark as unread</button>";
}

var itemTitleClicked = function(item)
{
    console.log("Clicked link to video: " + item.getAttribute("data-id"));
    Api.markAsRead(item.getAttribute("data-feed-id"), item.getAttribute("data-id"));
}

var markAsReadButtonPressed = function(item)
{
    $("#markItemAsReadButton-" + item.getAttribute("data-id")).hide();
    $("#markItemAsUnreadButton-" + item.getAttribute("data-id")).show();
    Api.markAsRead(item.getAttribute("data-feed-id"), item.getAttribute("data-id"));
}

var markAsUnreadButtonPressed = function(item)
{
    $("#markItemAsReadButton-" + item.getAttribute("data-id")).show();
    $("#markItemAsUnreadButton-" + item.getAttribute("data-id")).hide();
    Api.markAsUnread(item.getAttribute("data-feed-id"), item.getAttribute("data-id"));
}

var addFeed = function()
{
    const feedName = $("#addFeedButton").val();
    console.log("Add feed: " + feedName);
    Api.addFeed(feedName);
}
