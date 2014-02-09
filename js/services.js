$('#request').submit(function() {
	var errorStr = 'You must provide the next fields: ';
	var error = false;
	for (var i = 0; i < requested.length; i++) {
		if ($('#' + requested[i].replace('[','\\[')
				.replace(']','\\]')).val().trim() === '') {
			errorStr = errorStr + requestedLabels[i] + ', ';
			error = true;
		}
	}
	if (error) {
		errorStr = errorStr.substring(0, errorStr.length - 2);
		showError(errorStr);
		return false;
	} else {
		$('#error').hide();
		$('#lat').prop("disabled", false);
		$('#long').prop("disabled", false);
		return true;
	}
});

var id = 0;
var lat = 0.0;
var long = 0.0;
var requested;
var requestedLabels;

function loadService(code, name, hasMetadata) {
	requested = new Array('lat', 'long');
	requestedLabels = new Array('latitude', 'longitude');
	$('#request').find(':submit').prop("disabled", true);
	$('#' + id).toggleClass('active');
	$('#extra-attributes').html('');
	id = code;
	if (hasMetadata) {
		$('#loading').show();
		$.getJSON('/services/' + id + '.json', function(json) {
			if (json.service_code == id) {
				var attrs = json.attributes;
				for (var i = 0; i < attrs.length; i++) {
					var html;
					if (attrs[i].variable === false) {
						html = '<h4>'+ attrs[i].description + '</h4>';
					} else {
						var ai = attrs[i].code;
						var attr = 'attribute[' + ai + ']';
						html = '<div class="control-group"><label class="control-label" for="'
							+ attr
							+ '">'
							+ attrs[i].description;
							if (attrs[i].required === true) {
								html += ' <span class="red">*</span>';
							}
							html += '</label><div class="controls">';
						var dt = attrs[i].datatype;
						if (dt == 'text') {
							html = html + '<textarea id="'
								+ attr
								+ '" name="'
								+ attr
								+ '" maxlength="4000"></textarea>';
						} else if (dt == 'singlevaluelist' || dt == 'multivaluelist') {
							html = html + '<select id="'
							+ attr
							+ '" name="'
							+ attr
							+ '" class="input-xlarge"';
							if (dt == 'multivaluelist') {
								html = html + ' multiple="multiple"';
							}
							html = html + '>';
							var vals = attrs[i].values;
							for (var j = 0; j < vals.length; j++) {
								html = html + '<option value="'
									+ vals[j].key
									+ '">'
									+ vals[j].name
									+ '</option>';
							}
							html = html + '</select>';
						} else {
							html = html +'<input id="'
								+ attr
								+ '" name="'
								+ attr
								+ '" placeholder="" class="input-xlarge" type="';
							if (dt == 'string') {
								html = html + 'text';
							} else if (dt == 'number') {
								html = html + 'number';
							} else if (dt == 'datetime') {
								html = html + 'datetime-local';
							} else {
								continue;
							}
							html = html + '" />';
						}
						html = html + '<p class="help-block">'
							+ attrs[i].datatype_description
							+ '</p></div></div>';
						if (attrs[i].required === true) {
							requested.push(attr);
							requestedLabels.push(attrs[i].description);
						}
					}
					$('#extra-attributes').append(html);
				}
				$('#request').find(':submit').prop("disabled", false);
				//$('#request').find(':submit').removeAttr("disabled", 'false');
			} else if (json.length) {
				showError(json[0].description);
			}
		}).fail(function(error){
			showServerError();
		}).always(function(){
			$('#loading').hide();
		});
	} else {
		$('#request').find(':submit').prop("disabled", false);
	}
	$('#' + id).toggleClass('active');
	$('#request-issue').text(name);
	$('#service_code').val(id);
	$('#request').show(700);
	$('html, body').animate({
		scrollTop : $("#request").offset().top - $("#navbar").height()
	}, 500);
}

function onLocationSuccess(location) {
	lat = location.coords.latitude;
	long = location.coords.longitude;
	$('#lat').val(lat);
	$('#long').val(long);
}