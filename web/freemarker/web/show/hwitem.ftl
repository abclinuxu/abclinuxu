<#include "../header.ftl">

<h1 align="center">${TOOL.xpath(ITEM,"/data/name")}</h1>

<@lib.showParents PARENTS />

<#assign who=TOOL.createUser(ITEM.owner)>
<p>
 Tuto polo¾ku vytvoøil <a href="/Profile/${who.id}">${who.nick?default(who.name)}</a>
 dne ${DATE.show(ITEM.created,"CZ_FULL")}.

 <form name="hwitem" method="post" action="${URL.make("/edit")}">
  <input type="submit" name="Submit" value="Uprav polo¾ku" class="buton">
  <input name="action" type="hidden" value="editItem">
  <input name="rid" type="hidden" value="${REL_ITEM.id}">
 </form>

 <#if USER?exists && USER.hasRole("move relation")>
  <a href="/SelectRelation?rid=${REL_ITEM.id}&prefix=/hardware&url=/EditRelation&action=move" title="Pøesunout">
  <img src="/images/actions/cut.png" alt="Pøesunout" class="ikona"></a> &nbsp;
 </#if>
 <#if USER?exists && USER.hasRole("remove relation")>
  <a href="${URL.noPrefix("/EditRelation?action=remove&prefix=/hardware&rid="+REL_ITEM.id)}" title="Sma¾">
  <img src="/images/actions/delete.png" alt="Sma¾" class="ikona"></a>
 </#if>
</p>

<p>
 Máte-li doplòující informace, mù¾ete
 <a href="${URL.make("/edit?action=addRecord&rid="+REL_ITEM.id)}">pøidat</a> dal¹í záznam.
</p>

<p class="monitor"><b>AbcMonitor</b> vám emailem za¹le upozornìní pøi zmìnì.
 <#if USER?exists && TOOL.xpath(ITEM,"//monitor/id[text()='"+USER.id+"'")?exists>
  <#assign monitorState="Vypni">
 <#else>
  <#assign monitorState="Zapni">
 </#if>
 <a href="${URL.make("/edit?action=monitor&rid="+REL_ITEM.id)}">${monitorState}</a>
 (${TOOL.getMonitorCount(ITEM.data)})
</p>

<p><b>Nástroje</b>: <a href="/forum/show/${RELATION.id}?varianta=print">Tisk</a>
</p>

<form action="/Search"><input type="text" name="query" value="${TOOL.xpath(ITEM,"/data/name")}" size="45" class="pole">
<input type="submit" value="Hledej" class="buton">
</form>

<#assign RECORDS = CHILDREN.record>
<#list RECORDS as REL_RECORD>
 <#assign RECORD = REL_RECORD.child>
 <#assign who=TOOL.createUser(RECORD.owner)>
 <table cellspacing="0" border="0" cellpadding="5" width="95%" class="tabulka">
  <caption>Záznam èíslo ${REL_RECORD_index+1}</caption>
  <tr>
    <td colspan="2">Tento záznam pøidal <a href="/Profile/${who.id}">${who.name}</a>
     dne ${DATE.show(RECORD.created,"CZ_FULL")}.
     <#if RECORD.updated.after(RECORD.created)>
      Poslední úprava probìhla dne ${DATE.show(RECORD.updated,"CZ_FULL")}.
     </#if>
    </td>
  </tr>
  <tr>
    <td width="90">Ovladaè je dodáván</td>
    <td>
    <#switch TOOL.xpath(RECORD,"data/driver")?if_exists>
     <#case "kernel">v jádøe<#break>
     <#case "xfree">v XFree86<#break>
     <#case "maker">výrobcem<#break>
     <#case "other">nìkým jiným<#break>
     <#case "none">neexistuje<#break>
     <#default>Netu¹ím
    </#switch>
    </td>
  </tr>
  <tr>
    <td width="90">Cena</td>
    <td>
    <#switch TOOL.xpath(RECORD,"data/price")?if_exists>
     <#case "verylow">velmi nízká<#break>
     <#case "low">nízká<#break>
     <#case "good">pøimìøená<#break>
     <#case "high">vysoká<#break>
     <#case "toohigh">pøemr¹tìná<#break>
     <#default>Nehodnotím
    </#switch>
    </td>
  </tr>
  <#if TOOL.xpath(RECORD,"data/setup")?exists>
   <tr>
    <td width="90">Postup zprovoznìní pod Linuxem</td>
    <td>${TOOL.render(TOOL.element(RECORD.data,"data/setup"),USER?if_exists)}</td>
   </tr>
  </#if>
  <#if TOOL.xpath(RECORD,"data/params")?exists>
   <tr>
    <td width="90">Technické parametry</td>
    <td>${TOOL.render(TOOL.element(RECORD.data,"data/params"),USER?if_exists)}</td>
   </tr>
  </#if>
  <#if TOOL.xpath(RECORD,"data/identification")?exists>
   <tr>
    <td width="90">Linux jej identifikuje jako:</td>
    <td>${TOOL.render(TOOL.element(RECORD.data,"data/identification"),USER?if_exists)}</td>
   </tr>
  </#if>
  <#if TOOL.xpath(RECORD,"data/note")?exists>
   <tr>
    <td width="90">Poznámka</td>
    <td>${TOOL.render(TOOL.element(RECORD.data,"data/note"),USER?if_exists)}</td>
   </tr>
  </#if>
  <tr>
   <td colspan="2">
    Akce smí provádìt jen vlastník nebo admin:


    <form name="hwitem" method="post" action="${URL.make("/edit")}" style="float: left">
     <input type="submit" name="Submit" value="Uprav" class="buton">
     <input name="action" type="hidden" value="editRecord">
     <input name="rid" type="hidden" value="${RELATION.id}">
     <input name="recordId" type="hidden" value="${RECORD.id}">
    </form>

    <#if RECORDS?size gt 1> <#assign cislo=REL_RECORD.id> <#else> <#assign cislo=REL_ITEM.id></#if>
    <form name="hwitem" method="post" action="/EditRelation" style="float: left; margin-left: 2px;">
     <input type="submit" name="Submit" value="Sma¾" class="buton">
     <input name="action" type="hidden" value="remove">
     <input name="prefix" type="hidden" value="/hardware">
     <input name="rid" type="hidden" value="${cislo}">
    </form>

   </td>
  </tr>
 </table>
 <br>
</#list>

<#include "../footer.ftl">
