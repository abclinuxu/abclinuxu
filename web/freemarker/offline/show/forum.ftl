<#import "../macros.ftl" as lib>
<#include "../header.ftl">

<h1>Fórum ${TOOL.xpath(CATEGORY,"/data/name")}</h1>

<div class="ds">
    <table>
        <thead>
            <tr>
                <td class="td01">Dotaz</td>
                <td class="td02">Stav</td>
                <td class="td03">Reakcí</td>
                <td class="td04">Poslední</td>
            </tr>
        </thead>
        <tbody>
        <#list TOOL.analyzeDiscussions(RESULT.data) as diz>
            <tr>
                <td class="td01">
                    <a href="../../${DUMP.getFile(diz.relationId)}">${TOOL.limit(diz.title,60," ..")}</a>
                </td>
                <td class="td02">
                    <#if TOOL.isQuestionSolved(diz.discussion.data)>
                        <img src="../../../images/site2/vyreseno.gif" alt="V" title="Diskuse byla podle čtenářů vyřešena">
                    </#if>
                </td>
                <td class="td03">${diz.responseCount}</td>
                <td class="td04">${DATE.show(diz.updated,"CZ_FULL")}</td>
            </tr>
        </#list>
        </tbody>
    </table>
</div>

<#if (RESULT.pageCount>0)><@lib.listPages RESULT, RELATION.id /></#if>

<#include "../footer.ftl">
