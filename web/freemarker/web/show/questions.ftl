<#import "../macros.ftl" as lib>
<#if PARAMS.rid!="0">
    <#assign questions=VARS.getFreshQuestions(PARAMS.questions?eval, PARAMS.rid?eval)>
<#else>
    <#assign questions=VARS.getFreshQuestions(PARAMS.questions?eval)>
</#if>
<#assign FORUM=TOOL.analyzeDiscussions(questions)>
<#list FORUM as diz>
<tr>
    <td><a href="${diz.url}">${TOOL.limit(diz.title,60,"...")}</a></td>
    <td class="td-meta"><@lib.showDiscussionState diz /></td>
    <td class="td-meta">${diz.responseCount}</td>
    <td class="td-datum">${DATE.show(diz.updated,"SMART")}</td>
</tr>
</#list>
