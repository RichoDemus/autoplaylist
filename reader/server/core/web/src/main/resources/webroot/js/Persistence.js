var Persistence = (function()
{
	var pub = {},
	//Private property
	greyFloorTile = null;

	//Public property
	//pub.ingredient = "Bacon Strips";

	//Public method
	pub.restoreSession = ()=>JSON.parse(getStore().get("session"));

	pub.storeSession = session=>getStore().set("session", JSON.stringify(session));

	pub.clearSession = ()=>getStore().remove("session");

	pub.storeSortOrder = sortOrder => getStore().set("sorting", JSON.stringify(sortOrder));
	pub.loadSortOrder = () => JSON.parse(getStore().get("sorting"));

	//Private method

	function getStore() {
		return new Persist.Store('Reader');
	}

	//Return just the public parts
	return pub;
}());

