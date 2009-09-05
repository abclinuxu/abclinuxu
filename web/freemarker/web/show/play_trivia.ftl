<#include "../header.ftl">

<@lib.showMessages/>

<@lib.advertisement id="arbo-sq" />

<h1>${POSITION}. ${QUESTION}</h1>

<form action="${RELATION.url}" method="POST">
<p>
    <#list CHOICES as choice>
         <label>
             <input type="radio" name="q${POSITION}" value="${choice.id}">&nbsp;${choice.text}
         </label><br />
    </#list>
</p>
    <input type="hidden" name="position" value="${POSITION}">
    <input type="submit" value="<#if POSITION==10>Výsledek<#elseif POSITION==9>Poslední otázku<#else>Další otázku</#if>" class="button">
    ${TOOL.saveParams(PARAMS, ["position","rid"])}
</form>


<#include "../footer.ftl">
