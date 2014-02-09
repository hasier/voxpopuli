function upvote(id) {
	showLoading();
	$.post(
			"/requests/upvote.json",
			"service_request_id=" + id,
			function(json) {
				$("#up").unwrap();
				$("#up").css('color', 'Green');
				if (json[0].service_request_id === id) {
					$("#down").css('color', '');
					$("#down").removeAttr('style');
					if (!$("#down").parent().is('a')) {
						$("#down").wrap(
								'<a href="javascript:downvote(' + id
										+ ');"></a>');
					}
					$("#votes").text(json[0].votes);
				} else {
					showError(json[0].description);
				}
				hideLoading();
			}, "json").fail(function() {
		showServerError();
		hideLoading();
	});
}

function downvote(id) {
	showLoading();
	$.post(
			"/requests/downvote.json",
			"service_request_id=" + id,
			function(json) {
				$("#down").unwrap();
				$("#down").css('color', 'Green');
				if (json[0].service_request_id === id) {
					$("#up").css('color', '');
					$("#up").removeAttr('style');
					if (!$("#up").parent().is('a')) {
						$("#up")
								.wrap(
										'<a href="javascript:upvote(' + id
												+ ');"></a>');
					}
					$("#votes").text(json[0].votes);
				} else {
					showError(json[0].description);
				}
				hideLoading();
			}, "json").fail(function() {
		showServerError();
		hideLoading();
	});
}

function showLoading() {
	$("#up").hide();
	$("#down").hide();
	$("#loading").show();
}

function hideLoading() {
	$("#loading").hide();
	$("#up").show();
	$("#down").show();
}