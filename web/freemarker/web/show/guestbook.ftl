<#include "../header.ftl">

<@lib.showMessages/>

<#if (ENTRIES.currentPage.row == 0) >
    <h1>Kniha návštěv</h1>

    <p>Kniha návštěv je určena návštěvníkům, kteří chtějí zanechat
    vzkaz či zprávu provozovatelům a administrátorům portálu. Jedná
    se o obdobu návštěvních knih, jaké vídáte na hradech a zámcích,
    prostě napište zde, jak se vám na našem portále líbilo či nelíbilo.
    Pro hlášení chyb či námětů na vylepšení je určena
    <a href="${URL.noPrefix("/pozadavky")}">tato</a> stránka.
    </p>

    <p>
        <a href="${URL.noPrefix("/kniha_navstev/edit")}">Přidat vzkaz</a>
    </p>
</#if>

<#list ENTRIES.data as relation>
    <#assign entry = relation.child>
    <fieldset style="margin-top: 1em">
        <legend>${DATE.show(entry.created, "CZ_SHORT")}</legend>
        <b>${TOOL.xpath(entry.data,"/data/author")}</b> -
        ${TOOL.render(TOOL.xpath(entry.data,"/data/message"), USER!)}
        <#if USER?? && USER.hasRole("root")>
            <a href="${URL.noPrefix("/kniha_navstev/edit/"+relation.id+"?action=edit")}">upravit</a>,
            <a href="${URL.noPrefix("/EditRelation?action=remove&rid="+relation.id)}">smazat</a>
        </#if>
    </fieldset>
</#list>

<p>
  <#if (ENTRIES.currentPage.row > 0) >
   <#assign start=ENTRIES.currentPage.row-ENTRIES.pageSize><#if (start<0)><#assign start=0></#if>
   <a href="/kniha_navstev?from=${start}&amp;count=${ENTRIES.pageSize}">Novější vzkazy</a>
  </#if>
  <#assign start=ENTRIES.currentPage.row + ENTRIES.pageSize>
  <#if (start < ENTRIES.total) >
   <a href="/kniha_navstev?from=${start}&amp;count=${ENTRIES.pageSize}">Starší vzkazy</a>
  </#if>
</p>


<#include "../footer.ftl">
