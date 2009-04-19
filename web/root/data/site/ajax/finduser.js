function findUserHandler(resultId, dialogId, nameId, surnameId) {
	if(! $('#'+dialogId).length)
		return false;
	
	var win = $('#'+dialogId);
	
	var name = $('#'+nameId).val() + ' ' + $('#'+surnameId).val();
	
	// this loads html requests
	win.load(
		'/ajax/findUser?action=select', 
		{'name':name}, 
		function() {
			win.dialog({width: 500,
				        modal: true,
				        resizable: false,
				        draggable: false,
				        title: 'Výběr uživatele'})
		    	.dialog('open');
	});	
}