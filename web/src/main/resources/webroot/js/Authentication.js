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
		login();
		event.preventDefault();
	});
};

const setSignupFormBehaviour = function()
{
	console.log("setting up signup form");
	$("#signupForm").submit(function(event)
	{
		console.log("Signing Up");
		signup();
		event.preventDefault();
	});
};

const login = function()
{
	const username = $("#nameInput").val();
	const password = $("#passwordInput").val();
	Api.login(username, password, function(token)
	{
		//todo username should come from server response
		loggedIn(username, token);
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
	switchToDiv("#mainPageDiv");
	getAllItems();
};

const signup = function()
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
		signedUp(token);
	});
};

const signedUp = function(data)
{
	console.log("signed up");
	console.log(data);
	$("#signupDiv").hide();
	//todo prompt user to sign in
};
