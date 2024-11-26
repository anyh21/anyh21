this.form = $('<form>').appendTo('body').get(0);

var httpRequest = new XMLHttpRequest();
httpRequest.open('GET', document.location, false);
httpRequest.send(null);
$('<input type="hidden" name="_csrf">').val(httpRequest.getResponseHeader('X-CSRF-TOKEN')).appendTo(this.form);
