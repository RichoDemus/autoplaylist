$(function()
{
	Authentication.setLoginFormBehaviour();
	Authentication.setSignupFormBehaviour();
	$('#adminModal').on('shown.bs.modal', function (e) {
	  alert("Modal opened")
	});

	Service.init();
});