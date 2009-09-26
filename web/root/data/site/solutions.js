jQuery(document).ready(function(){
	jQuery("a.ds_solutionToggle").click(function(e) {
		e.preventDefault();
		
		reverting = jQuery(this).hasClass("ds_solutionUnset");
		
		var votes;
		var link = this;
		
		jQuery.get(this.href+"&ajax=true", function(text) {
			votes = text;
			
			if (votes != "-1") {
				reseni = jQuery(link).parent().parent().find("div.ds_reseni");
				if (votes == "0")
					reseni.hide();
				else {
					reseni.show();
					reseni.html("Řešení: "+votes+"&times;");
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
			}
		});
	});

});

function showCommentVoters(threadId) {
    jQuery.get("/EditDiscussion/"+Page.relationID+"?threadId="+threadId+"&action=showVoters", function(text) {

    });
}