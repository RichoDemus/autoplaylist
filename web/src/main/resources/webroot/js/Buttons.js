var Buttons = (function()
{
	var pub = {},
	//Private property
	greyFloorTile = null;

	//Public property
	//pub.ingredient = "Bacon Strips";

	//Public method
	pub.addFeed = function()
	{
		const feedName = $("#addFeedButton").val();
		console.log("Add feed: " + feedName);
		Api.addFeed(feedName);
	};

	pub.login = function()
	{
		const username = $("#nameInput").val();
		const password = $("#passwordInput").val();
		Service.login(username, password);
	};

	pub.signup = function()
	{
		const username = $("#signupNameInput").val();
		const password = $("#signupPasswordInput").val();
		const password2 = $("#signupPasswordInput2").val();
		if(password != password2)
		{
			alert("Passwords do not match");
			return;
		}
		Api.signup(username, password, function(token)
		{
			Authentication.signedUp(token);
		});
	};

	pub.createLabel = function()
	{
		const feedName = $("#createLabelInput").val();
		console.log("Create label: " + feedName);
		Api.createLabel(feedName, function(result)
		{
			labels.push(result);
			Service.updateEverything();
		});
	};

	pub.addFeedToLabel = function()
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

	pub.labelClicked = function(label)
	{
		const newSelectedLabelLong = label.getAttribute("data-label-id");
		if(newSelectedLabelLong == ALL_LABEL.id)
		{
			selectedLabel = ALL_LABEL;
			Service.updateEverything();
			return;
		}
		const newSelectedLabel = labels.filter(function(e) {return e.id == newSelectedLabelLong;})[0];
		console.log(newSelectedLabel);
		selectedLabel = newSelectedLabel;
		console.log(selectedLabel);
		console.log("Now only showing feeds beloning to label: " + selectedLabel.name);
		Service.updateEverything();
	};

	pub.filterFeedButtonClicked = function(feed)
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
		Service.updateEverything();
	};

	pub.markAsReadButtonPressed = function(item)
	{
		$("#markItemAsReadButton-" + item.getAttribute("data-id")).hide();
		$("#markItemAsUnreadButton-" + item.getAttribute("data-id")).show();
		Api.markAsRead(item.getAttribute("data-feed-id"), item.getAttribute("data-id"));
	};

	pub.markAsUnreadButtonPressed = function(item)
	{
		$("#markItemAsReadButton-" + item.getAttribute("data-id")).show();
		$("#markItemAsUnreadButton-" + item.getAttribute("data-id")).hide();
		Api.markAsUnread(item.getAttribute("data-feed-id"), item.getAttribute("data-id"));
	};

	pub.markOlderItemsAsReadButtonPressed = function(item)
	{
		Api.markOlderItemsAsRead(item.getAttribute("data-feed-id"), item.getAttribute("data-id"));
	};

	pub.itemTitleClicked = function(item)
	{
		console.log("Clicked link to video: " + item.getAttribute("data-id"));
		Api.markAsRead(item.getAttribute("data-feed-id"), item.getAttribute("data-id"));
	};

	pub.toggleSortOrder = function()
	{
		Service.toggleSortOrder();
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