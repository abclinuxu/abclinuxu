<#include "../header.ftl">

<h1 class="st_nadpis">${TOOL.xpath(ITEM,"/data/name")}</h1>


<#assign who=TOOL.createUser(ITEM.owner)>
<p>
 Tuto polo�ku vytvo�il <a href="/Profile/${who.id}">${who.nick?default(who.name)}</a>
 dne ${DATE.show(ITEM.created,"CZ_FULL")}.

 <a href="${URL.make("/edit?action=editItem&amp;rid="+REL_ITEM.id)}" title="Uprav polo�ku">
 <img src="/images/actions/pencil.png" alt="Uprav polo�ku" border="0" width="22" height="22"></a>&nbsp;
 <#if USER?exists && USER.hasRole("move relation")>
  <a href="/SelectRelation?rid=${REL_ITEM.id}&amp;prefix=/hardware&amp;url=/EditRelation&amp;action=move" title="P�esunout">
  <img src="/images/actions/cut.png" alt="P�esunout" class="ikona"></a> &nbsp;
 </#if>
 <#if USER?exists && USER.hasRole("remove relation")>
  <a href="${URL.noPrefix("/EditRelation?action=remove&amp;prefix=/hardware&amp;rid="+REL_ITEM.id)}" title="Sma�">
  <img src="/images/actions/delete.png" alt="Sma�" class="ikona"></a>
 </#if>
</p>

<p>
 M�te-li dopl�uj�c� informace, m��ete
 <a href="${URL.make("/edit?action=addRecord&amp;rid="+REL_ITEM.id)}">p�idat</a> dal�� z�znam.
</p>

<p class="monitor"><b>AbcMonitor</b> v�m emailem za�le upozorn�n� p�i zm�n�.
 <#if USER?exists && TOOL.xpath(ITEM,"//monitor/id[text()='"+USER.id+"']")?exists>
  <#assign monitorState="Vypni">
 <#else>
  <#assign monitorState="Zapni">
 </#if>
 <a href="${URL.make("/edit?action=monitor&amp;rid="+REL_ITEM.id)}">${monitorState}</a>
 (${TOOL.getMonitorCount(ITEM.data)})
</p>

<p><b>N�stroje</b>: <a href="/forum/show/${RELATION.id}?varianta=print">Tisk</a>
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
  <caption>Z�znam ��slo ${REL_RECORD_index+1}</caption>
  <tr>
    <td colspan="2">Tento z�znam p�idal <a href="/Profile/${who.id}">${who.name}</a>
     dne ${DATE.show(RECORD.created,"CZ_FULL")}.
     <#if RECORD.updated.after(RECORD.created)>
      Posledn� �prava prob�hla dne ${DATE.show(RECORD.updated,"CZ_FULL")}.
     </#if>
    </td>
  </tr>
  <tr>
    <td class="td01">Ovlada� je dod�v�n</td>
    <td class="td02">
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
    <td class="td01">Cena</td>
    <td class="td02">
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
    <td class="td01">Postup zprovozn�n� pod Linuxem</td>
    <td class="td02">${TOOL.render(TOOL.element(RECORD.data,"data/setup"),USER?if_exists)}</td>
   </tr>
  </#if>
  <#if TOOL.xpath(RECORD,"data/params")?exists>
   <tr>
    <td class="td01">Technick� parametry</td>
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
    <td class="td01">Pozn�mka</td>
    <td class="td02">${TOOL.render(TOOL.element(RECORD.data,"data/note"),USER?if_exists)}</td>
   </tr>
  </#if>
  <tr>
   <td colspan="2">
    Akce sm� prov�d�t jen vlastn�k nebo admin:
    <a href="${URL.make("/edit?action=editRecord&amp;rid="+RELATION.id+"&amp;recordId="+RECORD.id)}"
    title="Uprav z�znam"><img src="/images/actions/pencil.png" border="0" width="22" height="22"
    alt="Uprav z�znam"></a>
    &nbsp;
    <#if RECORDS?size gt 1> <#assign cislo=REL_RECORD.id> <#else> <#assign cislo=REL_ITEM.id></#if>
    <a href="${URL.noPrefix("/EditRelation?action=remove&amp;prefix=/hardware&amp;rid="+cislo)}"
    title="Sma�"><img src="/images/actions/delete.png" border="0" alt="Sma�" width="32" height="32"></a>
   </td>
  </tr>
 </table>
</#list>
</div></div>

<#include "../footer.ftl">
