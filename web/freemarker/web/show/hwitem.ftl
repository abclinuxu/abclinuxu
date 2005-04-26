<#include "../header.ftl">

<h1 class="st_nadpis">${TOOL.xpath(ITEM,"/data/name")}</h1>


<#assign who=TOOL.createUser(ITEM.owner)>
<p>
 Tuto polo¾ku vytvoøil <a href="/Profile/${who.id}">${who.nick?default(who.name)}</a>
 dne ${DATE.show(ITEM.created,"CZ_FULL")}.

 <a href="${URL.make("/edit?action=editItem&amp;rid="+REL_ITEM.id)}" title="Uprav polo¾ku">
 <img src="/images/actions/pencil.png" alt="Uprav polo¾ku" border="0" width="22" height="22"></a>&nbsp;
 <#if USER?exists && USER.hasRole("move relation")>
  <a href="/SelectRelation?rid=${REL_ITEM.id}&amp;prefix=/hardware&amp;url=/EditRelation&amp;action=move" title="Pøesunout">
  <img src="/images/actions/cut.png" alt="Pøesunout" class="ikona"></a> &nbsp;
 </#if>
 <#if USER?exists && USER.hasRole("remove relation")>
  <a href="${URL.noPrefix("/EditRelation?action=remove&amp;prefix=/hardware&amp;rid="+REL_ITEM.id)}" title="Sma¾">
  <img src="/images/actions/delete.png" alt="Sma¾" class="ikona"></a>
 </#if>
</p>

<p>
 Máte-li doplòující informace, mù¾ete
 <a href="${URL.make("/edit?action=addRecord&amp;rid="+REL_ITEM.id)}">pøidat</a> dal¹í záznam.
</p>

<p class="monitor"><b>AbcMonitor</b> vám emailem za¹le upozornìní pøi zmìnì.
 <#if USER?exists && TOOL.xpath(ITEM,"//monitor/id[text()='"+USER.id+"']")?exists>
  <#assign monitorState="Vypni">
 <#else>
  <#assign monitorState="Zapni">
 </#if>
 <a href="${URL.make("/edit?action=monitor&amp;rid="+REL_ITEM.id)}">${monitorState}</a>
 (${TOOL.getMonitorCount(ITEM.data)})
</p>

<p><b>Nástroje</b>: <a href="/forum/show/${RELATION.id}?varianta=print">Tisk</a>
</p>

<form action="/Search"><input type="text" name="query" value="${TOOL.xpath(ITEM,"/data/name")}" size="45">
<input type="submit" value="Hledej">
</form>

<div class="hw_item"><div class="hw_item">
<#assign RECORDS = CHILDREN.record>
<#list RECORDS as REL_RECORD>
 <#assign RECORD = REL_RECORD.child>
 <#assign who=TOOL.createUser(RECORD.owner)>
 <table>
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
    <td class="td01">Ovladaè je dodáván</td>
    <td class="td02">
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
    <td class="td01">Cena</td>
    <td class="td02">
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
    <td class="td01">Postup zprovoznìní pod Linuxem</td>
    <td class="td02">${TOOL.render(TOOL.element(RECORD.data,"data/setup"),USER?if_exists)}</td>
   </tr>
  </#if>
  <#if TOOL.xpath(RECORD,"data/params")?exists>
   <tr>
    <td class="td01">Technické parametry</td>
    <td class="td02">${TOOL.render(TOOL.element(RECORD.data,"data/params"),USER?if_exists)}</td>
   </tr>
  </#if>
  <#if TOOL.xpath(RECORD,"data/identification")?exists>
   <tr>
    <td class="td01">Linux jej identifikuje jako:</td>
    <td class="td02">${TOOL.render(TOOL.element(RECORD.data,"data/identification"),USER?if_exists)}</td>
   </tr>
  </#if>
  <#if TOOL.xpath(RECORD,"data/note")?exists>
   <tr>
    <td class="td01">Poznámka</td>
    <td class="td02">${TOOL.render(TOOL.element(RECORD.data,"data/note"),USER?if_exists)}</td>
   </tr>
  </#if>
  <tr>
   <td colspan="2">
    Akce smí provádìt jen vlastník nebo admin:
    <a href="${URL.make("/edit?action=editRecord&amp;rid="+RELATION.id+"&amp;recordId="+RECORD.id)}"
    title="Uprav záznam"><img src="/images/actions/pencil.png" border="0" width="22" height="22"
    alt="Uprav záznam"></a>
    &nbsp;
    <#if RECORDS?size gt 1> <#assign cislo=REL_RECORD.id> <#else> <#assign cislo=REL_ITEM.id></#if>
    <a href="${URL.noPrefix("/EditRelation?action=remove&amp;prefix=/hardware&amp;rid="+cislo)}"
    title="Sma¾"><img src="/images/actions/delete.png" border="0" alt="Sma¾" width="32" height="32"></a>
   </td>
  </tr>
 </table>
</#list>
</div></div>

<#include "../footer.ftl">
