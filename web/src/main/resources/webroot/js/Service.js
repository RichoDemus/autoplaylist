var Service = (function()
{
	var pub = {},
	//Private property
	greyFloorTile = null;

	//Public property
	//pub.ingredient = "Bacon Strips";

	//Public method
	pub.getAllItems = function()
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
	/*
	function privateWay() {
		console.log("private method");
	}
	*/
	//Return just the public parts
	return pub;
}());

