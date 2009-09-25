<#include "../../header.ftl">

<@lib.showMessages/>

<h2>Poslat notifikaci</h2>

<form action="${URL.noPrefix("/sprava/redakce/namety/notifikace")}" method="post">
	<div class="two-columns" style="width: 50%">
		<div class="left-column required" >Příjemce:&nbsp;</div>
		<div class="right-column" style="width: 80%">
			<div>
				<@lib.filterRadio filter=FILTER id="dest" value="conference" tabindex="1">Konference autorů</@lib.filterRadio>
			</div>
			<br/>			
			<div class="two-columns">
				<div class="left-column">
					<@lib.filterRadio filter=FILTER id="dest" value="author" tabindex="2">Autor:</@lib.filterRadio>
				</div>
				<div class="right-column">
					<select name="authorId" tabindex="3">
                		<#list AUTHORS as author>
					  		<@lib.filterOption filter=FILTER id="authorId" value="${author.id}">${author.title}</@lib.filterOption>
						</#list>
					</select>	
				</div>
			</div>
			<br/>
			<div class="two-columns">
				<div class="left-column">
					<@lib.filterRadio filter=FILTER id="dest" value="direct" tabindex="4">Jiný email</@lib.filterRadio>
				</div>
				<div class="right-column">
					<@lib.filterInput filter=FILTER id="email" tabindex="5" />
				</div>
			</div>
			<br/>
		</div>
	</div>
	<@lib.filterText filter=FILTER id="description" tabindex="5" class="siroka" style="height: 300px;" />
	<input type="submit" name="mail" value="Poslat" />
	<input type="submit" name="back" value="Zpět" />
	<@lib.filterHidden filter=FILTER id="filterTopicsByTitle" />
	<@lib.filterHidden filter=FILTER id="filterTopicsByOpened" />
	<@lib.filterHidden filter=FILTER id="filterTopicsByAuthor" />
	<@lib.filterHidden filter=FILTER id="filterTopicsByTerm" />
	<@lib.filterHidden filter=FILTER id="filterTopicsByAccepted" />
	<@lib.filterHidden filter=FILTER id="filterTopicsByRoyalty" />
	<#list FILTER.value("topicId") as topicId >
		<input type="hidden" name="topicId" value="${topicId}" />
	</#list>	
</form>

<#include "../../footer.ftl">
