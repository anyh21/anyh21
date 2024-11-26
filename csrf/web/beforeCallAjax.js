$.ajax({
		'url':JSV.getContextPath('/jsl/MessageSelector.UsableMsg.json'),
		'dataType':'json',
		'async':true,
		'context':this,
		'beforeSend':function(xhr) {
			var httpRequest = new XMLHttpRequest();
			httpRequest.open('GET', document.location, false);
			httpRequest.send(null);
			xhr.setRequestHeader('X-CSRF-TOKEN', httpRequest.getResponseHeader('X-CSRF-TOKEN'));
		},
		'success':function(data) {
			// ...
		},
		'error':function(e) {
		}
	});
