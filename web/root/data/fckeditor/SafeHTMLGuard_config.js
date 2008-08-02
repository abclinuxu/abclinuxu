FCKConfig.StylesXmlPath = '/data/fckeditor/SafeHTMLGuard_styles.xml';
FCKConfig.Plugins.Add( 'citation', null, '/data/fckeditor/editor/plugins/' );

FCKConfig.ToolbarSets['SafeHTMLGuard'] = [
	['Bold','Italic','-','Link','Unlink','Anchor','-','OrderedList','UnorderedList','Table','Style','-','SpecialChar'],
	['Undo','Redo','RemoveFormat','ShowBlocks'],['AbcCitation'],['Source'],['About']
] ;

FCKConfig.ToolbarSets['BlogGuard'] = [
	['Bold','Italic','-','Link','Unlink','Anchor','-','Image','OrderedList','UnorderedList','Table','Style','-','SpecialChar'],
	['Undo','Redo','RemoveFormat','ShowBlocks'],['AbcCitation'],['Source'],['About']
] ;

FCKConfig.ToolbarSets['WikiContentGuard'] = [
	['Bold','Italic','-','Link','Unlink','Anchor','-','Image','OrderedList','UnorderedList','Table','Style','-','SpecialChar'],
	['Undo','Redo','RemoveFormat','ShowBlocks'],['AbcCitation'],['Source'],['About']
] ;

