<#include "../header.ftl">

<@lib.showMessages/>

<p>
    <a href="${URL.noPrefix("/EditSurvey?action=add")}">vytvoř anketu</a>
</p>

<ul>
    <#list SURVEYS as survey>
        <li>
            <a href="${URL.noPrefix("/EditSurvey?action=edit&amp;surveyId="+survey.id)}">${survey.title}</a>
        </li>
    </#list>
</ul>

<#include "../footer.ftl">
