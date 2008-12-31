<#include "../header.ftl">

<@lib.showMessages/>

<h1>Vložení velké ankety</h1>

<p>Nápovědu najdete ve <a href="https://wiki.stickfish.com/bin/view/Projects/SurveyFormat">wiki</a></p>

<h2>Anketa</h2>

<form action="${URL.make("/EditSurvey")}" method="POST">
    <table class="siroka" border=0 cellpadding=5>
        <tr>
            <td class="required">Jméno ankety</td>
            <td>
                <input type="text" name="title" value="${PARAMS.title!}" size="40" tabindex="1">
                <div class="error">${ERRORS.title!}</div>
            </td>
        </tr>
        <tr>
            <td align="middle">Volby</td>
            <td>
                <p>Zde napište jména všech radio buttonů a check boxů z formulářů.
                   Každé jméno dejte na samostatný řádek.</p>
                <textarea name="choices" cols="40" rows="4" tabindex="2">${PARAMS.choices!}</textarea>
                <div class="error">${ERRORS.choices!}</div>
            </td>
        </tr>
        <tr>
            <td class="required" align="middle">XML definice</td>
            <td>
                <textarea name="definition" class="siroka" rows="20" tabindex="3">${PARAMS.definition!?html}</textarea>
                <div class="error">${ERRORS.definition!}</div>
            </td>
        </tr>
        <tr>
            <td>&nbsp;</td>
            <td><input type="submit" VALUE="Pokračuj" tabindex="4"></td>
        </tr>
    </table>

    <#if PARAMS.surveyId??>
        <input type="hidden" name="action" value="edit2">
        <input type="hidden" name="surveyId" value="${PARAMS.surveyId}">
    <#else>
        <input type="hidden" name="action" value="add2">
    </#if>
</form>


<#include "../footer.ftl">
