<#include "/offline/macros.ftl">
<#call showParents>

<#global who=TOOL.createUser(ITEM.owner)>
<p>
 Tuto polo¾ku vytvoøil <a href="http://abclinuxu.cz/Profile?userId=${who.id}">${who.name}</a>
 dne ${DATE.show(ITEM.created,"CZ_FULL")}.
</p>

<p>
 Máte-li doplòující informace, mù¾ete
 <a href="http://abclinuxu.cz/EditItem?action=addRecord&relationId=${REL_ITEM.id}">pøidat</a> 
 dal¹í záznam.
</p>

<#global RECORDS = CHILDREN.record>
<#list RECORDS as REL_RECORD>
 <#global RECORD = REL_RECORD.child>
 <#global who=TOOL.createUser(RECORD.owner)>
 <table cellspacing="0" border="1" cellpadding="5" width="100%">
  <caption>Záznam èíslo ${REL_RECORD_index+1}</caption>
  <tr>
    <td colspan="2">Tento záznam pøidal <a href="http://abclinuxu.cz/Profile?userId=${who.id}">${who.name}</a>
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
    <td>${TOOL.render(TOOL.xpath(RECORD,"data/setup"))}</td>
   </tr>
  </#if>
  <#if TOOL.xpath(RECORD,"data/params")?exists>
   <tr>
    <td width="90">Technické parametry</td>
    <td>${TOOL.render(TOOL.xpath(RECORD,"data/params"))}</td>
   </tr>
  </#if>
  <#if TOOL.xpath(RECORD,"data/identification")?exists>
   <tr>
    <td width="90">Linux jej identifikuje jako:</td>
    <td>${TOOL.render(TOOL.xpath(RECORD,"data/identification"))}</td>
   </tr>
  </#if>
  <#if TOOL.xpath(RECORD,"data/note")?exists>
   <tr>
    <td width="90">Poznámka</td>
    <td>${TOOL.render(TOOL.xpath(RECORD,"data/note"))}</td>
   </tr>
  </#if>
 </table>
 <br>
</#list>
