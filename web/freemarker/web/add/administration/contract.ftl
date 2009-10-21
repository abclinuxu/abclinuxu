<#assign html_header>
<link rel="stylesheet" type="text/css" media="all" href="/data/site/calendar/calendar-system.css" />
<script type="text/javascript" src="/data/site/calendar/calendar.js"></script>
<script type="text/javascript" src="/data/site/calendar/calendar-en.js"></script>
<script type="text/javascript" src="/data/site/calendar/calendar-cs-utf8.js"></script>
<script type="text/javascript" src="/data/site/calendar/calendar-setup.js"></script>
<script type="text/javascript" src="/data/site/jquery/ui.core.js"></script>
<script type="text/javascript" src="/data/site/jquery/ui.dialog.js"></script>
<script type="text/javascript" src="/data/site/ajax/finduser.js"></script>
<script type="text/javascript">
$(document).ready(function() {	
	var buttonParent = $('#findUser');
    buttonParent.append('<input type="button" value="Vyhledat uživatele" id="findUserButton" tabindex="8"/>');
    $('#findUserButton').bind('click', function() {
        findUserHandler('findUserResult-login', 'findUserDialog', 'addcontract-empty', 'addcontract-empty', 'id');
    });	
});
</script>
</#assign>

<#include "../../header.ftl">

<@lib.showMessages/>

<#if EDIT_MODE??>
<h2>Upravit šablonu smlouvy</h2>
<p>Zde můžete upravit šablonu smlouvu.
<#else>
<h2>Vytvořit šablonu smlouvy</h2>
<p>Tento formulář slouží pro vytvoření šablony smlouvy.</p>
</#if>
<form action="${URL.noPrefix("/sprava/redakce/smlouvy/edit")}" method="POST">
    <table class="siroka">
        <tr>
            <td class="required">Název:</td>
            <td>
                <input type="text" name="title" value="${(CONTRACT.title)!?html}" size="60" class="siroka" tabindex="1"/>
                <div class="error">${ERRORS.title!}</div>
            </td>
        </tr>
        <tr>
            <td class="required">Verze:</td>
            <td>
                <input type="text" name="version" value="${(CONTRACT.version)!?html}" size="60" class="siroka" tabindex="2"/>
                <div class="error">${ERRORS.version!}</div>
            </td>
        </tr>
        <tr>
            <td class="required">Očekáváné datum platnosti:</td>
            <td>
            	<#if (CONTRACT.proposedDate)??>
            		<#assign proposed_date=DATE.show(CONTRACT.proposedDate, "ISO_DMY") />
            	<#else>
            		<#assign proposed_date="">
            	</#if>		
                <input type="text" id="addcontract-proposed-date" name="proposedDate" value="${proposed_date}" size="20" tabindex="3" />&nbsp;
                <input type="button" id="addcontract-proposed-btn" value="..." tabindex="4"/>
                    <script type="text/javascript">
                    	Calendar.setup({inputField: "addcontract-proposed-date", ifFormat:"%Y-%m-%d", showsTime:false, button:"addcontract-proposed-btn", singleClick:false, step:1, firstDay:1});
                    </script>&nbsp;Formát 2005-01-25
                <div class="error">${ERRORS.proposedDate!}</div>
            </td>
        </tr>
        <tr>
            <td class="required">Stručný popis:</td>
            <td>
                <input type="text" name="description" value="${(CONTRACT.description)!?html}" size="60" class="siroka" tabindex="5"/>
                <div class="error">${ERRORS.description!}</div>
            </td>
        </tr>
        <tr>
            <td class="required">Obsah šablony:</td>
            <td><textarea name="content" rows="20" cols="60" tabindex="6">${(CONTRACT.content)!}</textarea>
            	<div class="error">${ERRORS.content!}</div>
            </td>
        </tr>
        <tr>
            <td class="required">Jednatelovo id:</td>
            <td style="white-space: nowrap">
                <div id="findUser">
                    <input type="text" id="findUserResult-id" name="employer" value="${(CONTRACT.employer.id)!}" size="24" tabindex="7"/>&nbsp;
                    <input type="hidden" id="addcontract-empty" value="" />       
                </div>
                <div id="findUserDialog"></div>
                <div class="error">${ERRORS.employer!}</div>
            </td>
        </tr>
        <#--
        <tr>
        	<td>Jednatelův podpis:</td>
            <td></td>
        </tr>
        -->    
        <tr>
            <td>&nbsp;</td>
            <td><input type="submit" value="Uložit" tabindex="9"/></td>
        </tr>
    </table>
    <#if EDIT_MODE??>
    <input type="hidden" name="action" value="edit2" />
    <input type="hidden" name="contractId" value="${(CONTRACT.id)!}" />
    <#else>
    <input type="hidden" name="action" value="add2" />
    </#if>
</form>
<#include "../../footer.ftl">