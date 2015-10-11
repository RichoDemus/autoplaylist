var Api = (function()
{
	var pub = {},
	//Private property
	greyFloorTile = null;

	//Public property
	//pub.ingredient = "Bacon Strips";

	//Public method
	pub.getAllItems = function(username, token, callback)
	{
		console.log("Getting all feeds for " + username + " using token " + token);
		$.ajax(
		{
			url: "api/users/" + username + "/feeds",
			type: "GET",
			headers: { 'x-token-jwt': token.raw }
		}).done(callback);
	};

	pub.getFeed = function(feedId, callback) {
		console.log("Getting feed " + feedId + " for " + Authentication.username);
		$.ajax(
		{
			url: "api/users/" + Authentication.username + "/feeds/" + feedId,
			type: "GET"
		}).done(callback);
	};

	pub.markAsRead = function(feedId, itemId)
	{
		console.log("Marking item " + itemId + " in feed " + feedId + " as read");
		jQuery.ajax ({
			url: "api/users/" + Authentication.username + "/feeds/" + feedId + "/items/" + itemId + "/",
			type: "POST",
			data: JSON.stringify({ action: "MARK_READ" }),
			dataType: "json",
			contentType: "application/json; charset=utf-8",
			success: function(){
				//
			}
		});
	};

	pub.markOlderItemsAsRead = function(feedId, itemId)
	{
		console.log("Marking items in feed " + feedId + " older than item " + itemId + " as read");
		jQuery.ajax ({
			url: "api/users/" + Authentication.username + "/feeds/" + feedId + "/items/" + itemId + "/",
			type: "POST",
			data: JSON.stringify({ action: "MARK_OLDER_ITEMS_AS_READ" }),
			dataType: "json",
			contentType: "application/json; charset=utf-8",
			success: function(){
				//
			}
		});
	};

	pub.markAsUnread = function(feedId, itemId)
	{
		console.log("Marking item " + itemId + " in feed " + feedId + " as unread");
		jQuery.ajax ({
			url: "api/users/" + Authentication.username + "/feeds/" + feedId + "/items/" + itemId + "/",
			type: "POST",
			data: JSON.stringify({ action: "MARK_UNREAD" }),
			dataType: "json",
			contentType: "application/json; charset=utf-8",
			success: function(){
				//
			}
		});
	};

	pub.addFeed = function(feedName)
	{
		jQuery.ajax ({
			url: "api/users/" + Authentication.username + "/feeds",
			type: "POST",
			data: feedName,
			dataType: "json",
			contentType: "application/json; charset=utf-8",
			success: function(){
				//
			}
		});
	};

	pub.login = function(username, password, callback)
	{
		console.log("Attempting to log in user " + username);
		$.post("api/users/" + username + "/sessions", username, callback).fail(function(data)
		{
			alert("Unable to login");
			console.log(data);
		});
	};

	pub.signup = function(username, password, callback)
	{
		console.log("Attempting to sign up user " + username);
		$.post("api/users", username, callback);
	};

	pub.refreshSession = function(session, callback)
	{
		console.log("refresh session is not implemented");
		callback(session);
	};

	pub.createLabel = function(label, callback)
	{
		console.log("Attepmting to create label " + label);
		$.post("api/users/" + Authentication.username + "/labels", label, callback);
	};

	pub.addFeedToLabel = function(feed, label, callback)
	{
		console.log("Attempting to add feed " + feed + " to label " + label);
		$.ajax({
			url: "api/users/" + Authentication.username + "/labels/" + label,
			data: feed,
			type: "PUT",
			success: callback
		});
	};

	//Private method
	/*
	function privateWay() {
		console.log("private method");
	}
	*/
	//Return just the public parts
	return pub;
}());

