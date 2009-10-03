<#assign html_header>
<script type="text/javascript">
$(document).ready(function() {
	/* disable all inputs */
	$('#dest-author-select').attr('disabled', 'disabled');
	$('#dest-direct-input').attr('disabled', 'disabled');
	
	/* bind functions to radio buttons */
	$('#dest-author').bind('click', function() {
		$('#dest-author-select').attr('disabled', '');
		$('#dest-direct-input').attr('disabled', 'disabled');
	});	
	$('#dest-direct').bind('click', function() {
		$('#dest-author-select').attr('disabled', 'disabled');
		$('#dest-direct-input').attr('disabled', '');
	});
	$('#dest-conference').bind('click', function() {
		$('#dest-author-select').attr('disabled', 'disabled');
		$('#dest-direct-input').attr('disabled', 'disabled');
	});	
	
	/* automatically select enabled inputs */ 	
	if($('#dest-author').is(':checked')) {
		$('#dest-author-select').attr('disabled', '');
	}
	if($('#dest-direct').is(':checked')) {
		$('#dest-author-input').attr('disabled', '');
	}
});
</script>
</#assign>

<#include "../../header.ftl">

<@lib.showMessages/>

<h2>Poslat notifikaci</h2>

<form action="${URL.noPrefix("/sprava/redakce/namety/notifikace")}" method="POST">
	<div class="two-columns" style="width: 50%">
		<div class="left-column required" >Příjemce:&nbsp;</div>
		<div class="right-column" style="width: 80%">
			<div>
				<@lib.filterRadio filter=FILTER name="dest" id="dest-conference" value="conference" tabindex="1">Konference autorů</@lib.filterRadio>
			</div>
			<br/>			
			<div class="two-columns">
				<div class="left-column">
					<@lib.filterRadio filter=FILTER name="dest" id="dest-author" value="author" tabindex="2">Autor:</@lib.filterRadio>
				</div>
				<div class="right-column">
					<select name="authorId" tabindex="3" id="dest-author-select">
                		<#list AUTHORS as author>
					  		<@lib.filterOption filter=FILTER name="authorId" value="${author.id}">${author.title}</@lib.filterOption>
						</#list>
					</select>	
				</div>
			</div>
			<br/>
			<div class="two-columns">
				<div class="left-column">
					<@lib.filterRadio filter=FILTER name="dest" id="dest-direct" value="direct" tabindex="4">Jiný email</@lib.filterRadio>
				</div>
				<div class="right-column">
					<@lib.filterInput filter=FILTER name="email" id="dest-direct-input" tabindex="5" />
				</div>
			</div>
			<br/>
		</div>
	</div>
	<@lib.filterText filter=FILTER name="description" tabindex="5" rows="15" cols="60" class="siroka" style="height: 300px;" />
	<input type="submit" name="mail" value="Poslat" />
	<input type="submit" name="back" value="Zpět" />
	<@lib.filterHidden filter=FILTER name="filterTopicsByTitle" />
	<@lib.filterHidden filter=FILTER name="filterTopicsByOpened" />
	<@lib.filterHidden filter=FILTER name="filterTopicsByAuthor" />
	<@lib.filterHidden filter=FILTER name="filterTopicsByTerm" />
	<@lib.filterHidden filter=FILTER name="filterTopicsByAccepted" />
	<@lib.filterHidden filter=FILTER name="filterTopicsByRoyalty" />
	<#list FILTER.value("topicId") as topicId >
		<input type="hidden" name="topicId" value="${topicId}" />
	</#list>	
</form>

<#include "../../footer.ftl">
