<#include "../header.ftl">
<#assign CATEGORY=RELATION.child>

<h1>${TOOL.xpath(CATEGORY,"data/name")} - �asto kladen� ot�zky</h1>

<#if TOOL.xpath(CATEGORY,"data/note")?exists>
    ${TOOL.render(TOOL.element(CATEGORY.data,"data/note"),USER?if_exists)}
</#if>

<#if QUESTIONS.total==0>
    <p>
        V t�to sekci nejsou zat�m ��dn� ot�zky.
    </p>
<#else>
    <ul>
    <#list QUESTIONS.data as relation>
        <li><a href="${relation.url}">${TOOL.childName(relation)}</a></li>
    </#list>
    </ul>
</#if>

<p>
<a href="${URL.make("/faq/edit?action=add&amp;rid="+RELATION.id)}">Vlo�it novou zodpov�zenou ot�zku</a>
<#if QUESTIONS.prevPage?exists >
    <br /><a href="${RELATION.url}?from=${QUESTIONS.prevPage.row}">Nov�j�� ot�zky</a>
</#if>
<#if nextPage?exists >
    &nbsp;--&nbsp;<a href="${RELATION.url}?from=${QUESTIONS.nextPage.row}">Star�� ot�zky</a>
</#if>
</p>

<#include "../footer.ftl">
