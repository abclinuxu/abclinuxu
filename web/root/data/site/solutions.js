jQuery(document).ready(function(){
	jQuery("a.ds_solutionToggle").click(function(e) {
		e.preventDefault();
		
		reverting = jQuery(this).hasClass("ds_solutionUnset");
		
		var link = this;
		
		jQuery.get(this.href+"&ajax=true", function(text) {
                        reseni = jQuery(link).parent().parent().find("div.ds_reseni");
                        if (text == "")
                                reseni.hide();
                        else {
                                reseni.show();
                                reseni.html(text);
                        }

                        if (!reverting) {
                                link.href = jQuery(link).attr("href").replace("setSolution", "unsetSolution");
                                link.innerHTML = "Není řešením";
                                jQuery(link).removeClass("ds_solutionSet");
                                jQuery(link).addClass("ds_solutionUnset");
                        } else {
                                link.href = jQuery(link).attr("href").replace("unsetSolution", "setSolution");
                                link.innerHTML = "Označit jako řešení";
                                jQuery(link).removeClass("ds_solutionUnset");
                                jQuery(link).addClass("ds_solutionSet");
                        }
		});
	});

});

function showCommentVoters(threadId) {
    jQuery.get("/ViewDiscussion/"+Page.relationID+"?threadId="+threadId+"&action=showVoters&fullList=yes", function(text) {
        reseni = jQuery("#showMore-"+threadId).parent();
        reseni.html(text);
    });
}