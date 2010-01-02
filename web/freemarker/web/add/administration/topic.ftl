<#assign html_header>
    <link rel="stylesheet" type="text/css" media="all" href="/data/site/calendar/calendar-system.css" />
    <script type="text/javascript" src="/data/site/calendar/calendar.js"></script>
    <script type="text/javascript" src="/data/site/calendar/calendar-en.js"></script>
    <script type="text/javascript" src="/data/site/calendar/calendar-cs-utf8.js"></script>
    <script type="text/javascript" src="/data/site/calendar/calendar-setup.js"></script>
    <script type="text/javascript">
	$(document).ready(function() {
		/* disable all inputs */
		$('#topic-assigned-select').attr('disabled', 'disabled');
		$('#royalty-specified-input').attr('disabled', 'disabled');
	
		/* bind functions to radio buttons */
		$('#topic-assigned').bind('click', function() {
			$('#topic-assigned-select').attr('disabled', '');
		});	
		$('#topic-public').bind('click', function() {
			$('#topic-assigned-select').attr('disabled', 'disabled');
		});
		$('#royalty-specified').bind('click', function() {
			$('#royalty-specified-input').attr('disabled', '');
		});
		$('#royalty-casual').bind('click', function() {
			$('#royalty-specified-input').attr('disabled', 'disabled');
		});	
	
		/* automatically select enabled inputs */ 	
		if($('#topic-public').is(':checked')) {
			$('#dest-author-select').attr('disabled', 'disabled');
		}	
		if($('#royalty-specified').is(':checked')) {
			$('#royalty-specified-input').attr('disabled', '');
		}
});
</script>
</#assign>

<#include "../../header.ftl">

<@lib.showMessages/>

<#if EDIT_MODE??>
    <h2>Upravit námět</h2>
<#else>
    <h2>Vytvořit námět</h2>
</#if>

<p>
    Tento formulář slouží pro vytvoření a úpravu námětu. Pokud téma nepřiřadíte konkrétnímu autorovi, bude veřejné.
    Dále máte možnost určit termín odevzdání a případně mimořádnou výši honoráře.
</p>

<form action="${URL.noPrefix("/sprava/redakce/namety/edit")}" method="POST">
    <table class="siroka">
        <tr>
            <td class="required">Název:</td>
            <td>
                <input type="text" name="title" value="${(TOPIC.title)!?html}" size="60" class="siroka"/>
                <@lib.showError key="title"/>
            </td>
        </tr>
        <tr>
            <td>Termín:</td>
            <td>
            	<#if (TOPIC.deadline)??>
            		<#assign deadline_date=DATE.show(TOPIC.deadline, "ISO_DMY") />
            	<#else>
            		<#assign deadline_date="">
            	</#if>		
                <input type="text" id="addtopic-deadline" name="deadline" value="${deadline_date}" size="20"/>&nbsp;
                <input type="button" id="addtopic-deadline-btn" value="..." tabindex="3"/>
                    <script type="text/javascript">
                    	Calendar.setup({inputField: "addtopic-deadline", ifFormat:"%Y-%m-%d", showsTime:false,
                                        button:"addtopic-deadline-btn", singleClick:false, step:1, firstDay:1});
                    </script>&nbsp;formát 2005-01-25
                <@lib.showError key="deadline"/>
            </td>
        </tr>
        <tr>
            <td class="required" style="vertical-align: top;">Autor:</td>
            <td style="white-space: nowrap">
                <@lib.showOption6 param="public" value="0" id="topic-assigned" caption="Přiřazený k" type="radio" condition=((TOPIC.isPublic())!true)==false/>
                <select name="author" id="topic-assigned-select">
                    <#list AUTHORS as author>
                        <@lib.showOption5 "${author.relationId}", "${author.title}", author.relationId==(TOPIC.author.relationId)!(-1) />
                    </#list>
                </select>
                <br />
                <@lib.showOption6 param="public" value="1" id="topic-public" caption="Veřejný námět" type="radio" condition=(TOPIC.isPublic())!true==true/>
                <@lib.showError key="author"/>
            </td>
        </tr>
        <tr>
            <td class="required" style="vertical-align: top;">Honorář:</td>
            <td>
            	<@lib.showOption6 param="royalty-mod" value="1" id="royalty-casual" caption="Běžný honorář" type="radio" condition=((TOPIC.hasRoyalty())!false)==false/>
            	<br />
            	<div>
                    <@lib.showOption6 param="royalty-mod" value="0" id="royalty-specified" caption="Jiný" type="radio" condition=((TOPIC.hasRoyalty())!false)==true/>&nbsp;
                    <input type="text" name="royalty" id="royalty-specified-input" value="${(TOPIC.royalty)!}" size="20"/> celé číslo
            	</div>
                <@lib.showError key="royalty"/>
            </td>
        </tr>
        <tr>
            <td class="required">Popis:</td>
            <td>
                <textarea name="description" rows="10" class="siroka">${(TOPIC.description)!}</textarea>
                <@lib.showError key="description"/>
            </td>
        </tr>
        <tr>
            <td>&nbsp;</td>
            <td><input type="submit" value="Uložit"/></td>
        </tr>
    </table>
    <#if EDIT_MODE??>
        <input type="hidden" name="action" value="edit2" />
        <input type="hidden" name="rid" value="${TOPIC.relationId}" />
    <#else>
        <input type="hidden" name="action" value="add2" />
    </#if>
</form>
<#include "../../footer.ftl">