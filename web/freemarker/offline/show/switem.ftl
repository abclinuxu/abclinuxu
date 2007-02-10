<#import "../macros.ftl" as lib>
<#include "../header.ftl">

<@showParents>

<#assign who=TOOL.createUser(ITEM.owner)>
<p>
 Tuto položku vytvořil <a href="http://www.abclinuxu.cz/Profile/${who.id}">${who.name}</a>
 dne ${DATE.show(ITEM.created,"CZ_FULL")}.
</p>

<p>
 Máte-li doplňující informace, můžete
 <a href="http://www.abclinuxu.cz/software/edit/${REL_ITEM.id}?action=addRecord">přidat</a>
 další záznam.
</p>

<#assign RECORDS = CHILDREN.record>
<#list RECORDS as REL_RECORD>
 <#assign RECORD = REL_RECORD.child>
 <#assign who=TOOL.createUser(RECORD.owner)>
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
   <td width="90">Návod či poznámka</td>
   <td>${TOOL.render(TOOL.xpath(RECORD,"data/text"),USER?if_exists)}</a></td>
  </tr>
 </table>
 <br>
</#list>

<#include "../footer.ftl">
