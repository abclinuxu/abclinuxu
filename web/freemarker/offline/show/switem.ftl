<#include "../macros.ftl">
<#include "../header.ftl">

<#call showParents>

<#global who=TOOL.createUser(ITEM.owner)>
<p>
 Tuto polo¾ku vytvoøil <a href="http://www.abclinuxu.cz/Profile/${who.id}">${who.name}</a>
 dne ${DATE.show(ITEM.created,"CZ_FULL")}.
</p>

<p>
 Máte-li doplòující informace, mù¾ete
 <a href="http://www.abclinuxu.cz/software/edit/${REL_ITEM.id}?action=addRecord">pøidat</a>
 dal¹í záznam.
</p>

<#global RECORDS = CHILDREN.record>
<#list RECORDS as REL_RECORD>
 <#global RECORD = REL_RECORD.child>
 <#global who=TOOL.createUser(RECORD.owner)>
 <table cellspacing="0" border="1" cellpadding="5" width="100%">
  <caption>Záznam èíslo ${REL_RECORD_index+1}</caption>
  <tr>
    <td colspan="2">Tento záznam pøidal <a href="http://www.abclinuxu.cz/Profile/${who.id}">${who.name}</a>
     dne ${DATE.show(RECORD.created,"CZ_FULL")}.
     <#if RECORD.updated.after(RECORD.created)>
      Poslední úprava probìhla dne ${DATE.show(RECORD.updated,"CZ_FULL")}.
     </#if>
    </td>
  </tr>
  <#if TOOL.xpath(RECORD,"data/url")?exists>
   <tr>
    <td width="90">Adresa softwaru</td>
    <td><a href="${TOOL.xpath(RECORD,"data/url")}">${TOOL.limit(TOOL.xpath(RECORD,"data/url"),40,"..")}</a></td>
   </tr>
  </#if>
  <#if TOOL.xpath(RECORD,"data/version")?exists>
   <tr>
    <td width="90">Tento záznam se týká verze</td>
    <td>${TOOL.xpath(RECORD,"data/version")}</a></td>
   </tr>
  </#if>
  <tr>
   <td width="90">Návod èi poznámka</td>
   <td>${TOOL.render(TOOL.xpath(RECORD,"data/text"),USER?if_exists)}</a></td>
  </tr>
 </table>
 <br>
</#list>

<#include "../footer.ftl">
