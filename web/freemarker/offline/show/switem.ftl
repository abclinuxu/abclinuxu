<#include "../macros.ftl">
<#include "../header.ftl">

<#call showParents>

<#global who=TOOL.createUser(ITEM.owner)>
<p>
 Tuto polo�ku vytvo�il <a href="http://www.abclinuxu.cz/Profile/${who.id}">${who.name}</a>
 dne ${DATE.show(ITEM.created,"CZ_FULL")}.
</p>

<p>
 M�te-li dopl�uj�c� informace, m��ete
 <a href="http://www.abclinuxu.cz/software/edit/${REL_ITEM.id}?action=addRecord">p�idat</a>
 dal�� z�znam.
</p>

<#global RECORDS = CHILDREN.record>
<#list RECORDS as REL_RECORD>
 <#global RECORD = REL_RECORD.child>
 <#global who=TOOL.createUser(RECORD.owner)>
 <table cellspacing="0" border="1" cellpadding="5" width="100%">
  <caption>Z�znam ��slo ${REL_RECORD_index+1}</caption>
  <tr>
    <td colspan="2">Tento z�znam p�idal <a href="http://www.abclinuxu.cz/Profile/${who.id}">${who.name}</a>
     dne ${DATE.show(RECORD.created,"CZ_FULL")}.
     <#if RECORD.updated.after(RECORD.created)>
      Posledn� �prava prob�hla dne ${DATE.show(RECORD.updated,"CZ_FULL")}.
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
    <td width="90">Tento z�znam se t�k� verze</td>
    <td>${TOOL.xpath(RECORD,"data/version")}</a></td>
   </tr>
  </#if>
  <tr>
   <td width="90">N�vod �i pozn�mka</td>
   <td>${TOOL.render(TOOL.xpath(RECORD,"data/text"),USER?if_exists)}</a></td>
  </tr>
 </table>
 <br>
</#list>

<#include "../footer.ftl">
