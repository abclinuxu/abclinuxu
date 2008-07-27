/*
 * Copyright (C) 2008 Leos Literak
 *
 * == BEGIN LICENSE ==
 *
 * Licensed under the terms of any of the following licenses at your
 * choice:
 *
 *  - GNU General Public License Version 2 or later (the "GPL")
 *    http://www.gnu.org/licenses/gpl.html
 * == END LICENSE ==
 *
 * This is a citation plugin.
 */

var AbcCiteCommand = function() {
        this.Name = 'AbcCite';
}

AbcCiteCommand.prototype.Execute = function() {
	if ( FCK.Config['AbcCitationContent'] != undefined && FCK.Config['AbcCitationContent'] != null ) {
        	FCK.InsertHtml('<blockquote>' + FCK.Config['AbcCitationContent'] + '</blockquote>');
	}
}

AbcCiteCommand.prototype.GetState = function() {
        if ( FCK.EditMode != FCK_EDITMODE_WYSIWYG )
                return FCK_TRISTATE_DISABLED;
	if ( FCK.Config['AbcCitationContent'] == undefined || FCK.Config['AbcCitationContent'] == null )
		return FCK_TRISTATE_DISABLED;
	else
		return FCK_TRISTATE_OFF;
}

FCKCommands.RegisterCommand( 'AbcCite', new AbcCiteCommand() );

// Create the toolbar button.
// var FCKToolbarButton = function( commandName, label, tooltip, style, sourceView, contextSensitive, icon )
var oFindItem = new FCKToolbarButton( 'AbcCite', 'Citace', 'Vlozi komentovany prispevek jako citaci', FCK_TOOLBARITEM_ICONTEXT, false, true, 73 );
FCKToolbarItems.RegisterItem( 'AbcCitation', oFindItem );

