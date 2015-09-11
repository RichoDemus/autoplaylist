var username = null;
var token = null;

$(function()
{
	setLoginFormBehaviour();
	setSignupFormBehaviour();
});

const setLoginFormBehaviour = function()
{
	console.log("setting up login form");
	$("#loginForm").submit(function(event)
	{
		console.log("logging in");
		Buttons.login();
		event.preventDefault();
	});
};

const setSignupFormBehaviour = function()
{
	console.log("setting up signup form");
	$("#signupForm").submit(function(event)
	{
		console.log("Signing Up");
		Buttons.signup();
		event.preventDefault();
	});
};

const loggedIn = function(username_param, token_param)
{
	token = {};
	//Makes the token more compatible with future JWT
	token.raw = token_param;
	username = username_param;
	token.toString = function(){return this.raw;};
	console.log("Logged in as " + username + ", got token: " + token);
	Navigation.switchToDiv("#mainPageDiv");
	Service.getAllItems();
};

const signedUp = function(data)
{
	console.log("signed up");
	console.log(data);
	$("#signupDiv").hide();
	//todo prompt user to sign in
};
