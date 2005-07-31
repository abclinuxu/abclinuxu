<#include "../header.ftl">
<#assign CATEGORY=RELATION.child>

<h1>${TOOL.xpath(CATEGORY,"data/name")} - èasto kladené otázky</h1>

<#if TOOL.xpath(CATEGORY,"data/note")?exists>
    ${TOOL.render(TOOL.element(CATEGORY.data,"data/note"),USER?if_exists)}
</#if>

<#if QUESTIONS.total==0>
    <p>
        V této sekci nejsou zatím ¾ádné otázky.
    </p>
<#else>
    <ul>
    <#list QUESTIONS.data as relation>
        <li><a href="${relation.url}">${TOOL.childName(relation)}</a></li>
    </#list>
    </ul>
</#if>

<p>
<a href="${URL.make("/faq/edit?action=add&amp;rid="+RELATION.id)}">Vlo¾it novou zodpovìzenou otázku</a>
<#if QUESTIONS.prevPage?exists >
    <br /><a href="${RELATION.url}?from=${QUESTIONS.prevPage.row}">Novìj¹í otázky</a>
</#if>
<#if nextPage?exists >
    &nbsp;--&nbsp;<a href="${RELATION.url}?from=${QUESTIONS.nextPage.row}">Star¹í otázky</a>
</#if>
</p>

<#include "../footer.ftl">
