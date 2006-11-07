<#include "../header.ftl">

<@lib.showMessages/>

<h1>${POSITION}. ${QUESTION}</h1>

<form action="${RELATION.url}" method="POST">
    <table border="0">
        <#list CHOICES as choice>
            <tr>
                <td>
                    <label>
                        <input type="radio" name="q${POSITION}" value="${choice.id}">${choice.text}
                    </label>
                </td>
            </tr>
        </#list>
    </table>
    <input type="hidden" name="position" value="${POSITION}">
    <input type="submit" value="<#if POSITION==10>Výsledek<#elseif POSITION==9>Poslední otázku<#else>Dal¹í otázku</#if>">
    ${TOOL.saveParams(PARAMS, ["position","rid"])}
</form>


<#include "../footer.ftl">
