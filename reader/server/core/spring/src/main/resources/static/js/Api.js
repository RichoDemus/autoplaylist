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
		if(!Authentication.token)
		{
			console.log("No token...");
			return;
		}
		console.log("Getting all feeds for " + username + " using token " + token);
		$.ajax(
			{
				url: "v1/feeds",
				type: "GET",
			}).done(callback);
	};

	pub.getFeed = function(feedId, callback) {
		console.log("Getting feed " + feedId + " for " + Authentication.username);
		$.ajax(
			{
				url: "v1/feeds/" + feedId + "/items",
				type: "GET"
			}).done(callback);
	};

	pub.markAsRead = function(feedId, itemId)
	{
		console.log("Marking item " + itemId + " in feed " + feedId + " as read");
		jQuery.ajax ({
			url: "v1/feeds/" + feedId + "/items/" + itemId,
			type: "POST",
			data: JSON.stringify({ action: "MARK_READ" }),
			dataType: "json",
			contentType: "application/json; charset=utf-8",
			headers: { 'x-token-jwt': Authentication.token.raw },
			success: function(){
				//
			},
			error: function()
			{
				alert("Marking item " + itemId + " as read failed");
			}
		});
	};

	pub.markOlderItemsAsRead = function(feedId, itemId)
	{
		console.log("Marking items in feed " + feedId + " older than item " + itemId + " as read");
		jQuery.ajax ({
			url: "v1/feeds/" + feedId + "/items/" + itemId,
			type: "POST",
			data: JSON.stringify({ action: "MARK_OLDER_ITEMS_AS_READ" }),
			dataType: "json",
			contentType: "application/json; charset=utf-8",
			headers: { 'x-token-jwt': Authentication.token.raw },
			success: function(){
				//
			},
			error: function()
			{
				alert("Marking all items older than item " + itemId + " as read failed");
			}
		});
	};

	pub.markAsUnread = function(feedId, itemId)
	{
		console.log("Marking item " + itemId + " in feed " + feedId + " as unread");
		jQuery.ajax ({
			url: "v1/feeds/" + feedId + "/items/" + itemId,
			type: "POST",
			data: JSON.stringify({ action: "MARK_UNREAD" }),
			dataType: "json",
			contentType: "application/json; charset=utf-8",
			headers: { 'x-token-jwt': Authentication.token.raw },
			success: function(){
				//
			},
			error: function()
			{
				alert("Marking item " + itemId + " as unread failed");
			}
		});
	};

	pub.addFeed = function(feedName)
	{
		jQuery.ajax ({
			url: "v1/feeds",
			type: "POST",
			data: JSON.stringify(feedName),
			dataType: "json",
			contentType: "application/json; charset=utf-8",
			success: function(){
				//
			},
			error: function(e)
			{
				alert("Adding feed " + feedName + " failed");
				console.log("add feed failed", e);
			}
		});
	};

	pub.login = function (username, password, callback) {
		console.log("Attempting to log in user " + username);
		jQuery.ajax({
			url: "v1/sessions",
			type: "POST",
			data: JSON.stringify({username, password}),
			dataType: "text",
			contentType: "application/json; charset=utf-8"
		}).done(callback)
			.fail(function (data) {
				alert("Unable to login");
				console.log(data);
			});
	};

	pub.signup = function (username, password, inviteCode, callback)
	{
		console.log("Attempting to sign up user " + username);
		jQuery.ajax({
			url: "v1/users",
			type: "POST",
			data: JSON.stringify({username: username, password: password, inviteCode: inviteCode}),
			dataType: "text",
			contentType: "application/json; charset=utf-8"
		}).done(callback)
			.fail(function (jqHXR, status, err)
			{
				alert("Signup failed")
			});
	};

	pub.refreshSession = function(session, callback)
	{
		console.log("Refreshing session");
		$.ajax({
			url: "v1/sessions",
			type: "GET",
			success: callback,
			error: function()
			{
				console.log("Unable to refresh session");
				callback(null);
			}
		});
	};

	pub.createLabel = function(label, callback)
	{
		console.log("Attepmting to create label " + label);
		$.ajax({
			url: "v1/labels",
			data: JSON.stringify(label),
			contentType: "application/json; charset=utf-8",
			type: "POST",
			success: callback,
			error: function()
			{
				alert("Creating label " + label + " failed");
			}
		});
	};

	pub.addFeedToLabel = function(feedId, label, callback)
	{
		console.log("Attempting to add feed " + feedId + " to label " + label);
		$.ajax({
			url: "v1/labels/" + label,
			contentType: "application/json; charset=utf-8",
			data: JSON.stringify(feedId),
			type: "POST",
			success: callback,
			error: function()
			{
				alert("Adding feed " + feedId + " to label " + label + " failed");
			}
		});
	};

	pub.getDownloadJobStatus = function (callback)
	{
		$.ajax(
			{
				url: "v1/admin/download",
				type: "GET",
				headers: { 'x-token-jwt': Authentication.token.raw }
			}).done(callback);
	};

	pub.runDownloadJob = function (callback)
	{
		$.ajax(
			{
				url: "v1/admin/download",
				type: "POST",
				headers: { 'x-token-jwt': Authentication.token.raw },
				success: callback,
				error: function()
				{
					alert("Failed running job");
				}
			});
	};

	return pub;
}());
