<#import "../macros.ftl" as lib>
<#include "../header.ftl">

<@lib.showParents />

<#assign who=TOOL.createUser(ITEM.owner)>
<p>
 Tuto položku vytvořil <a href="http://www.abclinuxu.cz/Profile/${who.id}">${who.name}</a>
 dne ${DATE.show(ITEM.created,"CZ_FULL")}.
</p>

<#assign RECORDS = CHILDREN.record>
<#list RECORDS as REL_RECORD>
 <#assign RECORD = REL_RECORD.child, who=TOOL.createUser(RECORD.owner)>
 <table cellspacing="0" border="1" cellpadding="5" width="100%">
  <caption>Záznam číslo ${REL_RECORD_index+1}</caption>
  <tr>
    <td colspan="2">Tento záznam přidal <a href="http://www.abclinuxu.cz/Profile/${who.id}">${who.name}</a>
     dne ${DATE.show(RECORD.created,"CZ_FULL")}.
     <#if RECORD.updated.after(RECORD.created)>
      Poslední úprava proběhla dne ${DATE.show(RECORD.updated,"CZ_FULL")}.
     </#if>
    </td>
  </tr>
  <tr>
    <td width="90">Ovladač je dodáván</td>
    <td>
    <#switch TOOL.xpath(RECORD,"data/driver")?if_exists>
     <#case "kernel">v jádře<#break>
     <#case "xfree">v XFree86<#break>
     <#case "maker">výrobcem<#break>
     <#case "other">někým jiným<#break>
     <#case "none">neexistuje<#break>
     <#default>Netuším
    </#switch>
    </td>
  </tr>
  <tr>
    <td width="90">Cena</td>
    <td>
    <#switch TOOL.xpath(RECORD,"data/price")?if_exists>
     <#case "verylow">velmi nízká<#break>
     <#case "low">nízká<#break>
     <#case "good">přiměřená<#break>
     <#case "high">vysoká<#break>
     <#case "toohigh">přemrštěná<#break>
     <#default>Nehodnotím
    </#switch>
    </td>
  </tr>
  <#if TOOL.xpath(RECORD,"data/setup")?exists>
   <tr>
    <td width="90">Postup zprovoznění pod Linuxem</td>
    <td>${TOOL.render(TOOL.xpath(RECORD,"data/setup"),USER?if_exists)}</td>
   </tr>
  </#if>
  <#if TOOL.xpath(RECORD,"data/params")?exists>
   <tr>
    <td width="90">Technické parametry</td>
    <td>${TOOL.render(TOOL.xpath(RECORD,"data/params"),USER?if_exists)}</td>
   </tr>
  </#if>
  <#if TOOL.xpath(RECORD,"data/identification")?exists>
   <tr>
    <td width="90">Linux jej identifikuje jako:</td>
    <td>${TOOL.render(TOOL.xpath(RECORD,"data/identification"),USER?if_exists)}</td>
   </tr>
  </#if>
  <#if TOOL.xpath(RECORD,"data/note")?exists>
   <tr>
    <td width="90">Poznámka</td>
    <td>${TOOL.render(TOOL.xpath(RECORD,"data/note"),USER?if_exists)}</td>
   </tr>
  </#if>
 </table>
 <br>
</#list>

<#include "../footer.ftl">
