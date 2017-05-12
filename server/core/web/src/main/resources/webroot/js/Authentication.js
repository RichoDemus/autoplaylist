var Authentication = (function()
{
	var pub = {},
	//Private property
	greyFloorTile = null;

	//Public property
	pub.username = null;
	pub.token = null;

	//Public method
	pub.loggedIn = function(username_param, token_param)
	{
		Authentication.token = {};
		//Makes the token more compatible with future JWT
		Authentication.token.raw = token_param;
		Authentication.username = username_param;
		Authentication.token.toString = function(){return this.raw;};
		console.log("Logged in as " + Authentication.username + ", got token: " + Authentication.token);
		Navigation.switchToDiv("#mainPageDiv");
		Service.getAllItems();
	};

	pub.signedUp = function(data)
	{
		console.log("signed up");
		console.log(data);
		$("#signupDiv").hide();
		//todo prompt user to sign in
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
