<%@ include file="/jspf/head.jsp" %>
<style type="text/css">
	.desc {padding:15px 15px 0 15px; color:#777777;}
	.btns {text-align:center;}
	.infos {padding:15px;}
	.infosTable {table-layout:fixed; width:100%; border-top:1px solid #DDDDDD; border-right:1px solid #DDDDDD; border-left:1px solid #DDDDDD;}
	.infosTable .label {background-color:#f8f8f8; width:100px; vertical-align:top; padding:10px 0 0 10px; border-bottom:1px solid #DDDDDD; font-weight:bold; color:#48495B;}
	.infosTable .content {padding:7px; border-bottom:1px solid #DDDDDD;}
</style>
<script type="text/javascript">
JSV.Block(function () {
	var descEditor = new TextFieldEditor(document.getElementById('descTd'));
	var isbn = '';
	var isbn_regx = /^[A-Za-z0-9]{1,13}$/i;
	
	<%-- Header	--%>
	var header	= new HeaderPopup(document.getElementById('headerTd'),'<fmt:message key="bms.label.005"/>');
	
	var ob = new KButton(document.getElementById('buttonTd'), <fmt:message key="btn.pub.ok"/>);
	ob.onclick = function() {
		isbn = descEditor.getValue();
		var key = JSV.getLang('IsbnFieldEditer', 'key');
		var url = '/ekp/bms/purchase/naver.xml.jsp?key=' + key + '&isbn=' + JSV.encode(isbn);
		if(isbn == ''){ //아무것도 입력하지 않은 경우
			alert(JSV.getLang('IsbnFieldEditer', 'inputNull'));
			return false;
		}
		if(!isbn_regx.test(isbn)){ //13자리의 숫자와 알파벳만 입력되었는지 확인
			alert('<fmt:message key="bms.002"/>');
			return false;
		}
		var xml = JSV.loadXml(url);
		var items = xml.selectNodes('//channel/item');
		if(items.length < 1){ //검색 결과가 없을 때
			alert(JSV.getLang('IsbnFieldEditer', 'notResult'));
			return false;
		}
		JSV.setModalReturnValue(descEditor.getValue());
		window.close();
	};
	var cb = new KButton(document.getElementById('buttonTd'), <fmt:message key="btn.pub.cancel"/>);
	cb.onclick = function() {
		JSV.setModalReturnValue(false);
		window.close();
	};
	descEditor.onkeydown = function(event){
		if (event.keyCode == 13){
			if(!descEditor.getValue() || descEditor.getValue() == '') {
				alert('<fmt:message key="pub.029"/>');
				return false;
			}
			ob.onclick();
		}
	}
});
</script>
<%@ include file="/jspf/body.jsp" %>
<table width="100%"	cellspacing="0" cellpadding="0"  style="table-layout:fixed;">
<tr id="headerTr" height="26px">
	<td	id="headerTd"></td>
</tr>
<tr>
	<td class="desc"><fmt:message key="bms.002"/></td>
</tr>
<tr>
	<td class="infos">
		<table class="infosTable" cellpadding="0" cellspacing="0">
			<tr>
				<td class="label"><fmt:message key="bms.label.005"/></td>
				<td class="content" id="descTd"></td>
			</tr>
		</table>
	</td>
</tr>
<tr id="buttonTr" height="25px">
	<td align="center" id="buttonTd"></td>
</tr>
</table>
<%@ include file="/jspf/tail.jsp" %>
