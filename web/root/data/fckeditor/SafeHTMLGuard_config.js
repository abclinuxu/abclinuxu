FCKConfig.LinkBrowser = false;
FCKConfig.ImageBrowser = false;
FCKConfig.FlashBrowser = false;
FCKConfig.LinkUpload = false;
FCKConfig.ImageUpload = false;
FCKConfig.FlashUpload = false;

FCKConfig.StylesXmlPath = '/data/fckeditor/SafeHTMLGuard_styles.xml';
FCKConfig.Plugins.Add( 'citation', null, '/data/fckeditor/editor/plugins/' );

FCKConfig.ToolbarSets['SafeHTMLGuard'] = [
	['Style'],['Bold','Italic','-','OrderedList','UnorderedList','Table','-','Link','Unlink','Anchor','-','SpecialChar'],
	['Undo','Redo','RemoveFormat','ShowBlocks'],['AbcCitation'],['Source'],['About']
] ;

FCKConfig.ToolbarSets['BlogGuard'] = [
	['Style'],['Bold','Italic','-','OrderedList','UnorderedList','Table','-','Link','Unlink','Anchor','-','Image','SpecialChar'],
	['Undo','Redo','RemoveFormat','ShowBlocks'],['AbcCitation'],['Source'],['About']
] ;

FCKConfig.ToolbarSets['WikiContentGuard'] = [
	['Style'],['Bold','Italic','-','OrderedList','UnorderedList','Table','-','Link','Unlink','Anchor','-','Image','SpecialChar'],
	['Undo','Redo','RemoveFormat','ShowBlocks'],['AbcCitation'],['Source'],['About']
] ;

