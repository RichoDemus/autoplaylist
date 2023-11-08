var Service = (function()
{
	var pub = {},
	//Private property
	byFeedName = function(a,b)
	{
		var nameA=a.name.toLowerCase(), nameB=b.name.toLowerCase();
		if (nameA < nameB) //sort string ascending
			return -1;
		if (nameA > nameB)
			return 1;
		return 0; //default return value (no sorting)
	};

	//Public property

	//Public method
	//Loads token from storage, verifies it and goes to the logged-in state, if anything fails, show the login pane
	pub.init = function()
	{
		restoreSession();
		Table.init();
	};

	pub.login = function(username, password)
	{
		Api.login(username, password, function(session)
		{
			loggedIn(session);
		});
	};

	pub.getAllItems = function()
	{
		Api.getAllItems(Authentication.username, Authentication.token, function(result)
		{
			feeds = result.feeds;
			feeds.sort(byFeedName);
			labels = result.labels;
			labels.push(UNLABELED_LABEL);
			labels.push(ALL_LABEL);
			labels.sort(function(a,b)
			{
				return a.id - b.id;
			});
			initializeFeeds();
			Service.updateEverything();
		});
	};

	pub.updateEverything = function()
	{
		//todo make better
		if(!feeds)
		{
			console.log("No feeds, fetching from server...");
			Service.getAllItems();
			return;
		}
		Table.clearTables();
		Table.addLabelsToTable();
		Table.addFeedsToTable();
		Table.addItemsToTable();
	};

	pub.selectFeed = function(feedId)
	{
		const selectedFeed = feeds.filter(function(f) {return f.id == feedId;})[0];
		if (selectedFeed && selectedFeed.items)
		{
			console.log("Feed Already downloaded..");
			Service.updateEverything();
			return;
		}

		if(!selectedFeed)
		{
			console.log("Deselecting feed");
			Service.updateEverything();
			return;
		}

		const feed = document.getElementById(feedId);
		feed.style.backgroundColor = "#ff8e90";
		Api.getFeed(feedId, function(items)
		{
			selectedFeed.items = items;
			Service.updateEverything();
		});
	};

	//Private method

	function loggedIn(session)
	{
		Authentication.loggedIn(session.username, session.token);
		console.log("storing session: " + JSON.stringify(session));
		Persistence.storeSession(session);
	}

	function restoreSession()
	{
//		const session = Persistence.restoreSession();
//		console.log("got session: " + JSON.stringify(session));
//		if(!session)
//		{
//			console.log("No session stored");
//			Authentication.username = null;
//			Authentication.token = null;
//			return;
//		}
//		console.log("found session: " + session.username + "/" + session.token);
		Api.refreshSession(function(newSession)
		{
			console.log("got new session: " + newSession);
			if(newSession)
			{
				loggedIn(newSession);
			}
		});
	}

	function initializeFeeds()
	{
		"use strict";
		feeds.forEach(feed =>
		{
			feed.availableItems = () =>
			{
				if (feed.items && feed.items.length > 0)
					return feed.items.length;
				return feed.numberOfAvailableItems;
			}
		});
	}

	//Return just the public parts
	return pub;
}());

