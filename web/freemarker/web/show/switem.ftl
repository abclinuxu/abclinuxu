<#include "../header.ftl">

<h1 align="center">${TOOL.xpath(ITEM,"/data/name")}</h1>

<#assign who=TOOL.createUser(ITEM.owner)>
<p>
 Tuto polo�ku vytvo�il <a href="/Profile/${who.id}">${who.nick?default(who.name)}</a>
 dne ${DATE.show(ITEM.created,"CZ_FULL")}.

 <a href="${URL.make("/edit?action=editItem&rid="+REL_ITEM.id)}" title="Uprav polo�ku">
 <img src="/images/actions/pencil.png" alt="Uprav polo�ku" border="0" width="22" height="22"></a>&nbsp;
 <#if USER?exists && USER.hasRole("move relation")>
  <a href="/SelectRelation?rid=${REL_ITEM.id}&prefix=/hardware&url=/EditRelation&action=move" title="P�esunout">
  <img src="/images/actions/cut.png" alt="P�esunout" class="ikona"></a> &nbsp;
 </#if>
 <#if USER?exists && USER.hasRole("remove relation")>
  <a href="${URL.noPrefix("/EditRelation?action=remove&prefix=/hardware&rid="+REL_ITEM.id)}" title="Sma�">
  <img src="/images/actions/delete.png" alt="Sma�" class="ikona"></a>
 </#if>
</p>

<p><b>N�stroje</b>: <a href="/forum/show/${RELATION.id}?varianta=print">Tisk</a></p>

<p>
 M�te-li dopl�uj�c� informace, m��ete
 <a href="${URL.make("/edit?action=addRecord&rid="+REL_ITEM.id)}">p�idat</a> dal�� z�znam.
</p>

<p class="monitor"><b>AbcMonitor</b> v�m emailem za�le upozorn�n� p�i zm�n�.
 <#if USER?exists && TOOL.xpath(ITEM,"//monitor/id[text()='"+USER.id+"'")?exists>
  <#assign monitorState="Vypni">
 <#else>
  <#assign monitorState="Zapni">
 </#if>
 <a href="${URL.make("/edit?action=monitor&rid="+REL_ITEM.id)}">${monitorState}</a>
 (${TOOL.getMonitorCount(ITEM.data)})
</p>

<#assign RECORDS = CHILDREN.record>
<#list RECORDS as REL_RECORD>
 <#assign RECORD = REL_RECORD.child>
 <#assign who=TOOL.createUser(RECORD.owner)>
 <table cellspacing="0" border="1" cellpadding="5" class="siroka">
  <caption>Z�znam ��slo ${REL_RECORD_index+1}</caption>
  <tr>
    <td colspan="2">Tento z�znam p�idal <a href="/Profile/${who.id}">${who.name}</a>
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
   <td>${TOOL.render(TOOL.element(RECORD.data,"data/text"),USER?if_exists)}</a></td>
  </tr>
  <tr>
   <td colspan="2">
    Akce sm� prov�d�t jen vlastn�k nebo admin:
    <a href="${URL.make("/edit?action=editRecord&rid="+RELATION.id+"&recordId="+RECORD.id)}"
    title="Uprav z�znam"><img src="/images/actions/pencil.png" border="0" width="22" height="22"
    alt="Uprav z�znam"></a>
    &nbsp;
    <#if RECORDS?size gt 1> <#assign cislo=REL_RECORD.id> <#else> <#assign cislo=REL_ITEM.id></#if>
    <a href="${URL.noPrefix("/EditRelation?action=remove&prefix=/software&rid="+cislo)}"
    title="Sma�"><img src="/images/actions/delete.png" border="0" alt="Sma�" width="32" height="32"></a>
   </td>
  </tr>
 </table>
 <br>
</#list>

<#include "../footer.ftl">
