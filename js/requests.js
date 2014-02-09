var map_canvas = document.getElementById('map');
var map_options = {
	center : new google.maps.LatLng(43.260050, -2.924598),
	zoom : 13,
	mapTypeId : google.maps.MapTypeId.HYBRID
}
var map = new google.maps.Map(map_canvas, map_options);

function addMarker(location, votes, service, desc, link, img) {
	// var icon =
	// 'https://maps.google.com/mapfiles/kml/shapes/schools_maps.png';
	// var icon = '/images/construction.png';
	var marker = new google.maps.Marker({
		position : location,
		map : map,
		icon : img
	});
	var contentString = '<div class="infowindow"><p class="lead">' + service
			+ ' </p><p>' + desc + ' </p><p><a href="' + link
			+ '">View</a></p></div>';
	var infowindow = new google.maps.InfoWindow({
		content : contentString
	});
	google.maps.event.addListener(marker, 'click', function() {
		// window.location.href = link;
		infowindow.open(map, marker);
	});
}

$('#filter').submit(function() {
	var errorStr = 'If you want to see close events you must provide your location';
	var isLocation = ($("#order").val() == "location");
	var hasLocation = ($("#location_hidden").length > 0);
	if (isLocation) {
		if (hasLocation) {
			return true;
		} else {
			showError(errorStr);
			return false;
		}
	} else {
		return true;
	}
});

function locationSelected(value) {
	$("#location_hidden").remove();
	if (value == "location") {
		getLocation(setLocation);
	}
}

function setLocation(location) {
	lat = location.coords.latitude;
	long = location.coords.longitude;
	appendHiddenLocation(lat + ',' + long);
}

function appendHiddenLocation(locationStr) {
	var html = '<input name="location" id="location_hidden" type="hidden" value=' + locationStr + ' />';
	$("#filter").append(html);
}