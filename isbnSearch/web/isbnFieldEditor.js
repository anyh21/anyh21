var isbn = false;
function IsbnFieldEditer(parent, style){
	this.style = style || {};	
	this.className = this.style.className || 'IsbnFieldEditer';
	this.widget = $('<table cellspacing="0" cellpadding="0"><tr><td class="firstTd"/><td class="lastTd"/></tr></table>').addClass(this.className).appendTo(parent).get(0);
	this.viewer = new TextFieldEditor($(this.widget).find('td:first').get(0));
	this.button = new KButton($(this.widget).find('td:last').get(0), JSV.getLang('IsbnFieldEditer', 'searchIcon'));
	this.button.viewer = this.viewer;
	this.button.isLibarary = this.style.isLibrary;
	
	this.viewer.button = this.button;
	this.viewer.component = this;
	this.viewer.onkeydown = function(event){
		if (event.keyCode == 13) {
			this.component.button.onclick();
		}
	}
	this.setEditable(false);
	this.button.onclick = function(event){
		IsbnFieldEditer.showDialog(null, null, null, function(search) {
			if (search != null) {
				isbn = search;
			}
			if(isbn == false){
				return false;
			}
			var key =  (style && style.key) ? style.key : JSV.getLang('IsbnFieldEditer', 'key');
			var url = '/ekp/bms/purchase/naver.xml.jsp?key=' + key + '&isbn=' + JSV.encode(isbn);
			//var url = 'http://openapi.naver.com/search?key='+ key +'&query=book&target=book_adv&d_isbn=' + isbn;
			if(isbn == ''){
				alert(JSV.getLang('IsbnFieldEditer', 'inputNull'));
			//}else if(isNaN(isbn)){
				//alert(JSV.getLang('IsbnFieldEditer', 'inputError'));
			}else{
				var xml = JSV.loadXml(url);
				var items = xml.selectNodes('//channel/item');
				if(items.length > 0){
					var btitle = $(items[0].selectSingleNode('title')).text();
					var company = $(items[0].selectSingleNode('publisher')).text();
					var pubDate = $(items[0].selectSingleNode('pubdate')).text();
					var authorName = $(items[0].selectSingleNode('author')).text();
					var imgPath = $(items[0].selectSingleNode('image')).text();
					var bookDesc = $(items[0].selectSingleNode('description')).text();
					
					if(pubDate != ''){
						var year = pubDate.substr(0,4);
						var month = pubDate.substr(4,2)-1;
						var day = pubDate.substr(6,2);
						
						pubDate = Date.parse(new Date(year, month, day));
					}
					if(this.isLibarary){
						t.getChild('title').setValue(btitle);
					}else{
						t.getChild('bookTitle').setValue(btitle);
					}
					t.getChild('isbn').setValue(isbn);
					t.getChild('imgPath').setValue(imgPath);
					t.getChild('bookDesc').setValue(bookDesc);
					if(this.isLibarary){
						t.getChild('content').setValue('<img src="'+imgPath+'">'+'<br><br>'+bookDesc);
					}
					t.getChild('company').setValue(company);
					t.getChild('pubDate').setValue(pubDate);
					t.getChild('authorName').setValue(authorName);
				}else{
					alert(JSV.getLang('IsbnFieldEditer', 'notResult'));
				}
			}
			
		});
	}
}
IsbnFieldEditer.prototype.setValue = function(value) {
	if(this.viewer) this.viewer.setValue(value);
}
IsbnFieldEditer.prototype.getValue = function() {
	return (this.viewer)? this.viewer.getValue():null;
}
IsbnFieldEditer.prototype.setEditable = function(value) {
	this.viewer.widget.attr('disabled', !value);
}
IsbnFieldEditer.showDialog = function(message, value, required, callBack) {
	var arg = {'message': message, 'value': value, 'required': required};
	var u = JSV.getContextPath('/ekp/bms/purchase/IsbnFieldEditorDialog.jsp');
	var f = 'dialogWidth:350px;dialogHeight:370px;scroll:auto;status:no;resizable:no';
	JSV.showModalDialog(u, [arg], f, function(search) {
		callBack(search);
	});
}
