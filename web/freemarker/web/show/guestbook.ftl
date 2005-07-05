<#include "../header.ftl">

<@lib.showMessages/>

<#if (ENTRIES.currentPage.row == 0) >
    <h1>Kniha náv¹tìv</h1>

    <p>Kniha náv¹tìv je urèena náv¹tìvníkùm, kteøí chtìjí zanechat
    vzkaz èi zprávu provozovatelùm a administrátorùm portálu. Jedná
    se o obdobu náv¹tìvních knih, jaké vídáte na hradech a zámcích,
    prostì napi¹te zde, jak se vám na na¹em portále líbilo èi nelíbilo.
    Pro hlá¹ení chyb èi námìtù na vylep¹ení je urèena
    <a href="${URL.noPrefix("/hardware/dir/3500")}">tato</a> stránka.
    </p>

    <p>
        <a href="${URL.noPrefix("/kniha_navstev/edit")}">Pøidat vzkaz</a>
    </p>
</#if>

<#list ENTRIES.data as relation>
    <#assign entry = relation.child>
    <fieldset style="margin-top: 1em">
        <legend>${DATE.show(entry.created, "CZ_SHORT")}</legend>
        <b>${TOOL.xpath(entry.data,"/data/author")}</b> -
        ${TOOL.render(TOOL.xpath(entry.data,"/data/message"), USER?if_exists)}
        <#if USER?exists && USER.hasRole("root")>
            <a href="${URL.noPrefix("/kniha_navstev/edit/"+relation.id+"?action=edit")}">upravit</a>,
            <a href="${URL.noPrefix("/EditRelation?action=remove&rid="+relation.id)}">smazat</a>
        </#if>
    </fieldset>
</#list>

<p>
  <#if (ENTRIES.currentPage.row > 0) >
   <#assign start=ENTRIES.currentPage.row-ENTRIES.pageSize><#if (start<0)><#assign start=0></#if>
   <a href="/kniha_navstev?from=${start}&amp;count=${ENTRIES.pageSize}">Novìj¹í vzkazy</a>
  </#if>
  <#assign start=ENTRIES.currentPage.row + ENTRIES.pageSize>
  <#if (start < ENTRIES.total) >
   <a href="/kniha_navstev?from=${start}&amp;count=${ENTRIES.pageSize}">Star¹í vzkazy</a>
  </#if>
</p>


<#include "../footer.ftl">
