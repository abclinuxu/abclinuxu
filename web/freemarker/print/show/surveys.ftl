<#include "../header.ftl">

<@lib.showMessages/>

<p>
    <a href="${URL.noPrefix("/EditSurvey?action=add")}">vytvoř anketu</a>
</p>

<ul>
    <#list SURVEYS as survey>
        <li>
            <a href="${URL.noPrefix("/EditSurvey?action=edit&amp;surveyId="+survey.id)}">
                ${TOOL.xpath(survey.data, "/anketa/title")}
            </a>
        </li>
    </#list>
</ul>

<#include "../footer.ftl">
