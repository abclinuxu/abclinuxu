<#include "../header.ftl">
<#assign CATEGORY=RELATION.child>

<div class="no-col-ad">
    <@lib.advertisement id="hypertext2nahore" />
    <@lib.advertisement id="square" />
    <@lib.advertisement id="hypertext2dole" />
</div>


<h1>${CATEGORY.title} - často kladené otázky</h1>

<#if TOOL.xpath(CATEGORY,"data/note")??>
    ${TOOL.render(TOOL.element(CATEGORY.data,"data/note"),USER!)}
</#if>

<#if QUESTIONS.total==0>
    <p>V této sekci nejsou zatím žádné otázky.</p>
<#else>
    <ul>
    <#list QUESTIONS.data as relation>
        <li><a href="${relation.url}">${TOOL.childName(relation)}</a></li>
    </#list>
    </ul>
</#if>

<p>
<a href="${URL.make("/faq/edit?action=add&amp;rid="+RELATION.id)}">Vložit novou zodpovězenou otázku</a>
<#if QUESTIONS.prevPage?? >
    <br /><a href="${RELATION.url}?from=${QUESTIONS.prevPage.row}">Novější otázky</a>
</#if>
<#if nextPage?? >
    &nbsp;--&nbsp;<a href="${RELATION.url}?from=${QUESTIONS.nextPage.row}">Starší otázky</a>
</#if>
</p>

<#include "../footer.ftl">
