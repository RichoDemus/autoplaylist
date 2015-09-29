var Service = (function()
{
	var pub = {},
	//Private property
	greyFloorTile = null;

	//Public property
	//pub.ingredient = "Bacon Strips";

	//Public method
	//Loads token from storage, verifies it and goes to the logged-in state, if anything fails, show the login pane
	pub.init = function()
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
			loggedIn(session);
		});
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
		Table.clearTables();
		Table.addLabelsToTable();
		Table.addFeedsToTable();
		Table.addItemsToTable();
	};

	//Private method

	function loggedIn(session) {
		Authentication.loggedIn(session.username, session.token);
		Persistence.storeSession(session);
	}
	//Return just the public parts
	return pub;
}());

