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

<h1>Poslat notifikaci</h1>

<p>
    Zde můžete poslat email s informacemi o námětech vybranému autorovi nebo konferenci autorů. Text je vygenerován
    z vybraných notifikací. Pokud byly všechny přiřazeny stejnému autorovi, byl automaticky zvolen jako příjemce.
</p>

<form action="${URL.make("/sprava/redakce/namety")}" method="POST">
    <table class="siroka">
        <tr>
            <td class="required" rowspan="3" style="width:6em">Příjemce:</td>
            <td colspan="2">
                <@lib.filterRadio filter=FILTER name="dest" id="dest-conference" value="conference">Konference autorů</@lib.filterRadio>
            </td>
        </tr>
        <tr>
            <td style="width:8em">
                <@lib.filterRadio filter=FILTER name="dest" id="dest-author" value="author">Autor:</@lib.filterRadio>
            </td>
            <td>
                <select name="authorId" tabindex="3" id="dest-author-select">
                    <#list AUTHORS as author>
                          <@lib.filterOption filter=FILTER name="authorId" value="${author.relationId}">${author.title}</@lib.filterOption>
                    </#list>
                </select>
            </td>
        </tr>
        <tr>
            <td style="width:8em">
                <@lib.filterRadio filter=FILTER name="dest" id="dest-direct" value="direct">Jiný email:</@lib.filterRadio>
            </td>
            <td width="*">
                <@lib.filterInput filter=FILTER name="email" id="dest-direct-input"/>                
            </td>
        </tr>
        <tr>
            <td class="required" style="width:6em">Zpráva</td>
            <td colspan="2">
                <@lib.filterText filter=FILTER name="description" rows="15" cols="60" class="siroka" style="height: 300px;" />

            </td>
        </tr>
        <tr>
            <td style="width:6em">&nbsp;</td>
            <td colspan="2">
                <input type="submit" name="notify2" value="Poslat" />
                <input type="submit" name="back" value="Zpět" />
            </td>
        </tr>
    </table>
	<@lib.filterHidden filter=FILTER name="filterTopicsByTitle" />
	<@lib.filterHidden filter=FILTER name="filterTopicsByOpened" />
	<@lib.filterHidden filter=FILTER name="filterTopicsByAuthor" />
	<@lib.filterHidden filter=FILTER name="filterTopicsByTerm" />
	<@lib.filterHidden filter=FILTER name="filterTopicsByAccepted" />
	<@lib.filterHidden filter=FILTER name="filterTopicsByRoyalty" />
</form>

<#include "../../footer.ftl">
