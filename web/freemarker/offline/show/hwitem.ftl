<#include "/offline/macros.ftl">
<#call showParents>

<#global who=TOOL.createUser(ITEM.owner)>
<p>
 Tuto polo�ku vytvo�il <a href="http://abclinuxu.cz/Profile?userId=${who.id}">${who.name}</a>
 dne ${DATE.show(ITEM.created,"CZ_FULL")}.
</p>

<p>
 M�te-li dopl�uj�c� informace, m��ete
 <a href="http://abclinuxu.cz/EditItem?action=addRecord&relationId=${REL_ITEM.id}">p�idat</a> 
 dal�� z�znam.
</p>

<#global RECORDS = CHILDREN.record>
<#list RECORDS as REL_RECORD>
 <#global RECORD = REL_RECORD.child>
 <#global who=TOOL.createUser(RECORD.owner)>
 <table cellspacing="0" border="1" cellpadding="5" width="100%">
  <caption>Z�znam ��slo ${REL_RECORD_index+1}</caption>
  <tr>
    <td colspan="2">Tento z�znam p�idal <a href="http://abclinuxu.cz/Profile?userId=${who.id}">${who.name}</a>
     dne ${DATE.show(RECORD.created,"CZ_FULL")}.
     <#if RECORD.updated.after(RECORD.created)>
      Posledn� �prava prob�hla dne ${DATE.show(RECORD.updated,"CZ_FULL")}.
     </#if>
    </td>
  </tr>
  <tr>
    <td width="90">Ovlada� je dod�v�n</td>
    <td>
    <#switch TOOL.xpath(RECORD,"data/driver")?if_exists>
     <#case "kernel">v j�d�e<#break>
     <#case "xfree">v XFree86<#break>
     <#case "maker">v�robcem<#break>
     <#case "other">n�k�m jin�m<#break>
     <#case "none">neexistuje<#break>
     <#default>Netu��m
    </#switch>
    </td>
  </tr>
  <tr>
    <td width="90">Cena</td>
    <td>
    <#switch TOOL.xpath(RECORD,"data/price")?if_exists>
     <#case "verylow">velmi n�zk�<#break>
     <#case "low">n�zk�<#break>
     <#case "good">p�im��en�<#break>
     <#case "high">vysok�<#break>
     <#case "toohigh">p�emr�t�n�<#break>
     <#default>Nehodnot�m
    </#switch>
    </td>
  </tr>
  <#if TOOL.xpath(RECORD,"data/setup")?exists>
   <tr>
    <td width="90">Postup zprovozn�n� pod Linuxem</td>
    <td>${TOOL.render(TOOL.xpath(RECORD,"data/setup"))}</td>
   </tr>
  </#if>
  <#if TOOL.xpath(RECORD,"data/params")?exists>
   <tr>
    <td width="90">Technick� parametry</td>
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
    <td width="90">Pozn�mka</td>
    <td>${TOOL.render(TOOL.xpath(RECORD,"data/note"))}</td>
   </tr>
  </#if>
 </table>
 <br>
</#list>
