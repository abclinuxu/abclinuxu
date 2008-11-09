function StitkyAdvertLink() {
	var div = document.getElementById("tagpicker");
	if (div) {
		this.clink = Toolkit.appendElement(div, "a", null, null, "Zobrazit štítky");
		this.clink.href = "#";
		Toolkit.addEventListener(this.clink, "click", "showTags", this, this.clink);
	}
}

StitkyAdvertLink.prototype = {
	visible: false,
	showTags: function(event, button) {
		if (!this.visible) {
			this.clink.innerHTML = "Skrýt štítky";
				
			this.div = document.getElementById("tagpicker");
			this.ndiv = Toolkit.appendElement(this.div, "div", null, null, null);
			new StitkyPick(Page.relationID, this.ndiv);
			this.visible = true;
		} else {
			this.clink.innerHTML = "Zobrazit štítky";
			this.div.removeChild(this.ndiv);
			this.visible = false;
		}
	}
}

function StitkyPick(nodeID, div) {
	this.nodeID = nodeID;
	this.div = div;
	
	var vyberStitku = Toolkit.appendElement(this.div, "table", "vyberStitku");
	var row = vyberStitku.insertRow(0);
	this.stitkySeznam = Toolkit.appendElement(row.insertCell(0), "select", "stitkySeznam");
	this.stitkySeznam.size = this.seznamSize;
	
	this.stitkyOblibene = Toolkit.appendElement(row.insertCell(1), "div", "stitkyOblibene");
	this.pridatButton = Toolkit.appendElement(this.div, "span", "pridatButton");
	this.pridatButton.setAttribute("style", "visibility: hidden");
	
	var sseznam = new SeznamStitkuAJAX(this, this.nodeID);
	var sfavorite = new OblibeneStitkyAJAX(this, this.nodeID);
	
	sseznam.pridatStitekMouse = this.pridatStitekMouseHandler;
	sseznam.pridatStitekKeyboard = this.pridatStitekKeyboardHandler;
	sfavorite.pridatStitek = this.pridatStitekHandler;
}

function addSearchTag(stitek) {
	var taglist = document.getElementById("tags");
	
	if (taglist.value.length > 0)
		taglist.value = taglist.value + " " + stitek;
	else
		taglist.value = stitek;
}

StitkyPick.prototype = {
	seznamSize: 12,
 
	pridatStitekMouseHandler: function(event, seznam) {
		if (seznam.selectedIndex >= 0 && !event.ctrlKey)
		addSearchTag(seznam.options[seznam.selectedIndex].value);
		return false;
	},
	
	pridatStitekHandler: function(event, stitekID) {
		addSearchTag(stitekID);
		return false;
	},
	
	pridatStitekKeyboardHandler: function(event, seznam) {
		if (seznam.selectedIndex >= 0 && (event.charCode == 32 || (event.charCode == 0 && event.keyCode == 13)) || (!event.charCode && (event.keyCode == 13 || event.keyCode == 32))) {
			addSearchTag(seznam.options[seznam.selectedIndex].value);
			return false;
		}
	}
}


