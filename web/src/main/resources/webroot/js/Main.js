
$(function() {
    console.log( "ready!" );
    console.log($("#video-1").data("id"));

     Api.getAllItems(function(result)
     {
        result.feeds.forEach(function(feed)
        {
            feed.items.forEach(function(item)
            {
                addItemToTable(item);
            });
        });
     });


});

var addItemToTable = function(item)
{
	$('#itemListTable > tbody:last').append(
	    "<tr data-id=" + item.id + " id=\"video-" + item.id + "\"><th>" + getMarkAsReadToggleButton(item) + "</th><th>" + getTitle(item) + "</th><th>" + item.description + "</th><th>" + item.uploadDate + "</th></th>");
}

var getTitle = function(item)
{
    return "<a id=\"" + item.id + "\" data-id=\"" + item.id + "\" href=\"" + item.url + "\" target=\"_blank\" onClick=\"itemTitleClicked(this)\">" + item.title + "</a>";
}

var getMarkAsReadToggleButton = function(item)
{
    return "<button id=\"markItemAsReadButton-" + item.id + "\" data-id=\"" + item.id + "\" type=\"button\" class=\"btn btn-xs btn-default\" onClick=\"markAsReadButtonPressed(this)\">Mark as read</button>" +
        "<button id=\"markItemAsUnreadButton-" + item.id + "\" data-id=\"" + item.id + "\" type=\"button\" class=\"btn btn-xs btn-default\" onClick=\"markAsUnreadButtonPressed(this)\" style=\"display:none;\" >Mark as unread</button>";
}

var itemTitleClicked = function(item)
{
    console.log("Clicked link to video: " + item.getAttribute("data-id"));
    Api.markAsRead(item.getAttribute("data-id"));
}

var markAsReadButtonPressed = function(item)
{
    console.log("Clicked the mark as read button to video: " + item.getAttribute("data-id"));
    $("#markItemAsReadButton-" + item.getAttribute("data-id")).hide();
    $("#markItemAsUnreadButton-" + item.getAttribute("data-id")).show();
    Api.markAsRead(item.getAttribute("data-id"));
}

var markAsUnreadButtonPressed = function(item)
{
    console.log("Clicked the mark as unread button to video: " + item.getAttribute("data-id"));
    $("#markItemAsReadButton-" + item.getAttribute("data-id")).show();
    $("#markItemAsUnreadButton-" + item.getAttribute("data-id")).hide();
    Api.markAsUnread(item.getAttribute("data-id"));
}
