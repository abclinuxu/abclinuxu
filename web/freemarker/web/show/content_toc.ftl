<#if SUBPORTAL??>
    <#import "../macros.ftl" as lib>
    <#assign plovouci_sloupec>
      <@lib.showSubportal SUBPORTAL, true/>
    </#assign>
</#if>

<#include "../header.ftl">

<@lib.advertisement id="square" />

<h1>Obsah</h1>

<#assign first = TOC.firstChapter>
<p><a href="${first.relation.url}">${TOOL.childName(first.relation)}</a></p>

<@listChapters first.chapters />

<#macro listChapters chapters>
    <#if (chapters?size==0)><#return></#if>
    <ul>
        <#list chapters as chapter>
            <li><a href="${chapter.relation.url}">${TOOL.childName(chapter.relation)}</a>
                <@listChapters chapter.chapters /></li>
        </#list>
    </ul>
</#macro>

<#include "../footer.ftl">
