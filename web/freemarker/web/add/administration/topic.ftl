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
		if($('#topic-assigned').is(':checked')) {
			$('#dest-author-select').attr('disabled', '');
		}	
		if($('#royalty-specified').is(':checked')) {
			$('#dest-author-input').attr('disabled', '');
		}
});
</script>
</#assign>

<#include "../../header.ftl">

<@lib.showMessages/>

<#if EDIT_MODE??>
<h2>Upravit námět</h2>
<p>Zde můžete upravit námět.
<#else>
<h2>Vytvořit námět</h2>
<p>Tento formulář slouží pro vytvoření námětu. Všechny položky jsou povinné.</p>
</#if>
<form action="${URL.noPrefix("/sprava/redakce/namety/edit")}" method="POST">
    <table class="siroka">
        <tr>
            <td class="required">Název:</td>
            <td>
                <input type="text" name="title" value="${(TOPIC.title)!?html}" size="60" class="siroka" tabindex="1"/>
                <div class="error">${ERRORS.title!}</div>
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
                <input type="text" id="addtopic-deadline" name="deadline" value="${deadline_date}" size="20" tabindex="2" />&nbsp;
                <input type="button" id="addtopic-deadline-btn" value="..." tabindex="3"/>
                    <script type="text/javascript">
                    	Calendar.setup({inputField: "addtopic-deadline", ifFormat:"%Y-%m-%d", showsTime:false, button:"addtopic-deadline-btn", singleClick:false, step:1, firstDay:1});
                    </script>&nbsp;Formát 2005-01-25
                <div class="error">${ERRORS.deadline!}</div>
            </td>
        </tr>
        <tr>
            <td class="required">Autor:</td>
            <td style="white-space: nowrap">
            	<div class="two-columns">
            		<div class="left-column">
            			<@lib.showOption6 param="public" value="1" id="topic-public" caption="Veřejný námět" type="radio" condition=(TOPIC.isPublic())!true==true tabindex="6" />
            			<br />
	            		<@lib.showOption6 param="public" value="0" id="topic-assigned" caption="Přiřazený k" type="radio" condition=((TOPIC.isPublic())!true)==false tabindex="7" />
					</div>
					<div class="right-column">
						<select name="author" id="topic-assigned-select">
							<#list AUTHORS as author>
								<@lib.showOption5 "${author.id}", "${author.title}", author.id==(TOPIC.author.id)!(-1) />
							</#list>
						</select>	
					</div>
				</div>            	                
				<div class="error">${ERRORS.author!}</div>
            </td>
        </tr>
        <tr>
            <td class="required">Honorář:</td>
            <td>
            	<@lib.showOption6 param="royalty-mod" value="1" id="royalty-casual" caption="Běžný honorář" type="radio" condition=((TOPIC.hasRoyalty())!false)==false tabindex=6 />
            	<br />
            	<div>
            	<@lib.showOption6 param="royalty-mod" value="0" id="royalty-specified" caption="Jiný" type="radio" condition=((TOPIC.hasRoyalty())!false)==true tabindex=6 />&nbsp;                
            	<input type="text" name="royalty" id="royalty-specified-input" value="${(TOPIC.royalty)!}" size="20" tabindex="7"/>
            	</div>
            	<div class="error">${ERRORS.royalty!}</div>
            </td>
        </tr>
        <tr>
            <td class="required">Popis:</td>
            <td><textarea name="description" rows="10" cols="60" tabindex="8">${(TOPIC.description)!}</textarea>
            	<div class="error">${ERRORS.description!}</div>
            </td>
        </tr>
        <tr>
            <td>&nbsp;</td>
            <td><input type="submit" value="Uložit" tabindex="9"/></td>
        </tr>
    </table>
    <#if EDIT_MODE??>
    <input type="hidden" name="action" value="edit2" />
    <input type="hidden" name="topicId" value="${(TOPIC.id)!}" />
    <#else>
    <input type="hidden" name="action" value="add2" />
    </#if>
</form>
<#include "../../footer.ftl">