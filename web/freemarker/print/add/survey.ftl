<#include "../header.ftl">

<@lib.showMessages/>

<h1>Úvod</h1>

<p>
    Nápovědu najdete ve <a href="https://wiki.stickfish.com/bin/view/Projects/SurveyFormat">wiki</a>
</p>

<h1>Anketa</h1>

<form action="${URL.make("/EditSurvey")}" method="POST">
    <table width="100%" border=0 cellpadding=5>
        <tr>
            <td width="120" class="required">Jméno ankety</td>
            <td>
                <input type="text" name="title" value="${PARAMS.title?if_exists}" size="40" tabindex="1">
                <div class="error">${ERRORS.title?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td width="120" align="middle">Volby</td>
            <td>
                <p>
                    Zde napište jména všech radio buttonů a check boxů z formulářů.
                    Každé jméno dejte na samostatný řádek.
                </p>
                <textarea name="choices" cols="40" rows="4" tabindex="2">${PARAMS.choices?if_exists}</textarea>
                <div class="error">${ERRORS.choices?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td width="120" class="required" align="middle">XML definice</td>
            <td>
                <textarea name="definition" cols="80" rows="20" tabindex="3">${PARAMS.definition?if_exists?html}</textarea>
                <div class="error">${ERRORS.definition?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td width="120">&nbsp;</td>
            <td><input type="submit" VALUE="Pokračuj" tabindex="4"></td>
        </tr>
    </table>

    <#if PARAMS.surveyId?exists>
        <input type="hidden" name="action" value="edit2">
        <input type="hidden" name="surveyId" value="${PARAMS.surveyId}">
    <#else>
        <input type="hidden" name="action" value="add2">
    </#if>
</form>


<#include "../footer.ftl">
