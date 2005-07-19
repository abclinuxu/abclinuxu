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
    <#list QUESTIONS.data as relation>
        <p>
            <a href="${relation.url}">${TOOL.childName(relation)}</a>
        </p>
    </#list>
</#if>

<ul>
    <li>
        <a href="${URL.make("/faq/edit?action=add&amp;rid="+RELATION.id)}">Vlo�it novou zodpov�zenou ot�zku</a>
    </li>
    <#if QUESTIONS.prevPage?exists >
        <li>
            <a href="${RELATION.url}?from=${QUESTIONS.prevPage.row}">Nov�j�� ot�zky</a>
        </li>
    </#if>
    <#if nextPage?exists >
        <li>
            <a href="${RELATION.url}?from=${QUESTIONS.nextPage.row}">Star�� ot�zky</a>
        </li>
    </#if>
</ul>

<#include "../footer.ftl">
