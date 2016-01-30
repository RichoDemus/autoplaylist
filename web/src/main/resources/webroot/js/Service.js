var Service = (function()
{
	var pub = {},
	//Private property
	sortingAlgorithm = function(a,b)
	{
		var nameA=a.name.toLowerCase(), nameB=b.name.toLowerCase();
		if (nameA < nameB) //sort string ascending
			return -1;
		if (nameA > nameB)
			return 1;
		return 0; //default return value (no sorting)
	};

	//Public property
	pub.sortOrder = null;

	//Public method
	//Loads token from storage, verifies it and goes to the logged-in state, if anything fails, show the login pane
	pub.init = function()
	{
		restoreSession();
		initSortOrderToggle();
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
			feeds.sort(sortingAlgorithm);
			labels = result.labels;
			labels.push(UNLABELED_LABEL);
			labels.push(ALL_LABEL);
			labels.sort(function(a,b)
			{
				return a.id - b.id;
			});
			Service.updateEverything();
		});
	};

	pub.updateEverything = function()
	{
		//todo make better
		if(!feeds)
		{
			Service.getAllItems();
			return;
		}
		Table.clearTables();
		Table.addLabelsToTable();
		Table.addFeedsToTable();
		Table.addItemsToTable();
	};

	pub.toggleSortOrder = function()
	{
		console.log("Toggle sort order, current is: " + Service.sortOrder);
		if(Service.sortOrder === SortOrder.OLDEST_FIRST)
		{
			setSortOrder(SortOrder.NEWEST_FIRST);
		}
		else
		{
			setSortOrder(SortOrder.OLDEST_FIRST);
		}
	};

	pub.selectFeed = function(feedId)
	{
		Api.getFeed(feedId, function(feed)
		{
			const targetFeed = feeds.filter(function(f) {return f.id == feedId;})[0];
			targetFeed.items = feed.items;
			Service.updateEverything();
		});
	};

	//Private method

	function loggedIn(session)
	{
		Authentication.loggedIn(session.username, session.token);
		Persistence.storeSession(session);
	}

	function restoreSession()
	{
		const session = Persistence.restoreSession();
		if(!session)
		{
			console.log("No session stored");
			Authentication.username = null;
			Authentication.token = null;
			return;
		}
		console.log("found session: " + session.username + "/" + session.token);
		Api.refreshSession(session, function(newSession)
		{
			console.log("got new session: " + newSession.username + "/" + newSession.token);
			if(newSession)
			{
				loggedIn(session);
			}
		});
	}

	function initSortOrderToggle()
	{
		//save sort order on the server
		var sortOrder = SortOrder.OLDEST_FIRST;

		setSortOrder(sortOrder);
	}

	function setSortOrder(sortOrder)
	{
		console.log("Setting sortoder to " + sortOrder);
		$("#sortOrderSpan").text("Currently sorting by " + SortOrder.properties[sortOrder].name);
		Service.sortOrder = sortOrder;
		Service.updateEverything();
	}
	//Return just the public parts
	return pub;
}());

