FCKConfig.StylesXmlPath = '/data/fckeditor/SafeHTMLGuard_styles.xml';
FCKConfig.Plugins.Add( 'citation', null, '/data/fckeditor/editor/plugins/' );

FCKConfig.ToolbarSets['SafeHTMLGuard'] = [
	['Undo','Redo','Bold','Italic','-','Link','Unlink','-','OrderedList','UnorderedList','Table','Style','-','SpecialChar'],
	['RemoveFormat','ShowBlocks'],['AbcCitation'],['Source'],['About']
] ;

FCKConfig.ToolbarSets['BlogGuard'] = [
	['Undo','Redo','Bold','Italic','-','Link','Unlink','-','Image','OrderedList','UnorderedList','Table','Style','-','SpecialChar'],
	['RemoveFormat','ShowBlocks'],['Source'],['About']
] ;

FCKConfig.ToolbarSets['WikiContentGuard'] = [
	['Undo','Redo','Bold','Italic','-','Link','Unlink','Anchor','-','Image','OrderedList','UnorderedList','Table','Style','-','SpecialChar'],
	['JustifyLeft','JustifyCenter','JustifyRight','JustifyFull'],
	['RemoveFormat','ShowBlocks'],['Source'],['About']
] ;

