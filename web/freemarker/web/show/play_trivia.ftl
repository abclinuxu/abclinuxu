<#include "../header.ftl">

<@lib.showMessages/>

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
    <input type="submit" value="<#if POSITION==10>V�sledek<#elseif POSITION==9>Posledn� ot�zku<#else>Dal�� ot�zku</#if>" class="button">
    ${TOOL.saveParams(PARAMS, ["position","rid"])}
</form>


<#include "../footer.ftl">
