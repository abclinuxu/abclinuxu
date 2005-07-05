<#include "../header.ftl">

<@lib.showMessages/>

<#if (ENTRIES.currentPage.row == 0) >
    <h1>Kniha n�v�t�v</h1>

    <p>Kniha n�v�t�v je ur�ena n�v�t�vn�k�m, kte�� cht�j� zanechat
    vzkaz �i zpr�vu provozovatel�m a administr�tor�m port�lu. Jedn�
    se o obdobu n�v�t�vn�ch knih, jak� v�d�te na hradech a z�mc�ch,
    prost� napi�te zde, jak se v�m na na�em port�le l�bilo �i nel�bilo.
    Pro hl�en� chyb �i n�m�t� na vylep�en� je ur�ena
    <a href="${URL.noPrefix("/hardware/dir/3500")}">tato</a> str�nka.
    </p>

    <p>
        <a href="${URL.noPrefix("/kniha_navstev/edit")}">P�idat vzkaz</a>
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
   <a href="/kniha_navstev?from=${start}&amp;count=${ENTRIES.pageSize}">Nov�j�� vzkazy</a>
  </#if>
  <#assign start=ENTRIES.currentPage.row + ENTRIES.pageSize>
  <#if (start < ENTRIES.total) >
   <a href="/kniha_navstev?from=${start}&amp;count=${ENTRIES.pageSize}">Star�� vzkazy</a>
  </#if>
</p>


<#include "../footer.ftl">
