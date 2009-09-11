<#assign html_header>
    <link rel="stylesheet" type="text/css" media="all" href="/data/site/calendar/calendar-system.css" />
    <script type="text/javascript" src="/data/site/calendar/calendar.js"></script>
    <script type="text/javascript" src="/data/site/calendar/calendar-en.js"></script>
    <script type="text/javascript" src="/data/site/calendar/calendar-cs-utf8.js"></script>
    <script type="text/javascript" src="/data/site/calendar/calendar-setup.js"></script>
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
            			<@lib.showOption6 param="public" value="1" caption="Veřejný námět" type="radio" condition=(TOPIC.isPublic())!true==true tabindex=6 />
            			<br />
	            		<@lib.showOption6 param="public" value="0" caption="Přiřazený k" type="radio" condition=((TOPIC.isPublic())!true)==false tabindex=6 />
					</div>
					<div class="right-column">
						<select name="author" id="authors-list">
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
            	<@lib.showOption6 param="royalty-mod" value="1" caption="Běžný honorář" type="radio" condition=((TOPIC.hasRoyalty())!false)==false tabindex=6 />
            	<br />
            	<div>
            	<@lib.showOption6 param="royalty-mod" value="0" caption="Jiný" type="radio" condition=((TOPIC.hasRoyalty())!false)==true tabindex=6 />&nbsp;                
            	<input type="text" name="royalty" value="${(TOPIC.royalty)!}" size="20" tabindex="7"/>
            	</div>
            	<div class="error">${ERRORS.royalty!}</div>
            </td>
        </tr>
        <tr>
            <td class="required">Popis:</td>
            <td><textarea name="description" size="60" class="siroka" tabindex="8">${(TOPIC.description)!}</textarea>
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