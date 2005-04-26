<#include "../header.ftl">

<@lib.showMessages/>

<ul>
    <#list SURVEYS as survey>
        <li>
            <a href="${URL.noPrefix("/EditSurvey?action=edit&amp;surveyId="+survey.id)}">
                ${TOOL.xpath(survey.data, "/anketa/title")?default("Bezejmenná anketa")}
            </a>
        </li>
    </#list>
</ul>

<#include "../footer.ftl">
