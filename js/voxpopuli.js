function showServerError() {
	showError('An error occurred contacting the server. Please check internet connection and try again later');
}

function showError(text) {
	$('#error-text').html(text);
	$('#error').show();
}

function showMessage(text) {
	$('#alert-text').html(text);
	$('#alert').show();
}

function getLocation(successCallback) {
	// var success = function(location) {
	// lat = location.coords.latitude;
	// long = location.coords.longitude;
	// $('#lat').val(lat);
	// $('#long').val(long);
	// };
	var error = function(error) {
		// FIXME Handle location error
	};
	navigator.geolocation.getCurrentPosition(successCallback, error);
}