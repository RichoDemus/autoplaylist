var Persistence = (function()
{
	var pub = {},
	//Private property
	greyFloorTile = null;

	//Public property
	//pub.ingredient = "Bacon Strips";

	//Public method
	pub.restoreSession = function()
	{
		const session = JSON.parse(getStore().get("session"));
		console.log("got session: " + JSON.stringify(session));
		return session;
	};

	pub.storeSession = function(session)
	{
		console.log("storing session: " + JSON.stringify(session));
		console.log(session);
		getStore().set("session", JSON.stringify(session));
	};

	pub.clearSession = function()
	{
		console.log("clearing session");
		getStore().remove("session");
	};

	//Private method

	function getStore() {
		return new Persist.Store('Reader');
	}

	//Return just the public parts
	return pub;
}());

