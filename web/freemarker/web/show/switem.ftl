<#include "../header.ftl">

<h1 align="center">${TOOL.xpath(ITEM,"/data/name")}</h1>

<#assign who=TOOL.createUser(ITEM.owner)>
<p>
 Tuto polo¾ku vytvoøil <a href="/Profile/${who.id}">${who.nick?default(who.name)}</a>
 dne ${DATE.show(ITEM.created,"CZ_FULL")}.

 <a href="${URL.make("/edit?action=editItem&rid="+REL_ITEM.id)}" title="Uprav polo¾ku">
 <img src="/images/actions/pencil.png" alt="Uprav polo¾ku" border="0" width="22" height="22"></a>&nbsp;
 <#if USER?exists && USER.hasRole("move relation")>
  <a href="/SelectRelation?rid=${REL_ITEM.id}&prefix=/hardware&url=/EditRelation&action=move" title="Pøesunout">
  <img src="/images/actions/cut.png" alt="Pøesunout" class="ikona"></a> &nbsp;
 </#if>
 <#if USER?exists && USER.hasRole("remove relation")>
  <a href="${URL.noPrefix("/EditRelation?action=remove&prefix=/hardware&rid="+REL_ITEM.id)}" title="Sma¾">
  <img src="/images/actions/delete.png" alt="Sma¾" class="ikona"></a>
 </#if>
</p>

<p><b>Nástroje</b>: <a href="/forum/show/${RELATION.id}?varianta=print">Tisk</a></p>

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

<#assign RECORDS = CHILDREN.record>
<#list RECORDS as REL_RECORD>
 <#assign RECORD = REL_RECORD.child>
 <#assign who=TOOL.createUser(RECORD.owner)>
 <table cellspacing="0" border="1" cellpadding="5" class="siroka">
  <caption>Záznam èíslo ${REL_RECORD_index+1}</caption>
  <tr>
    <td colspan="2">Tento záznam pøidal <a href="/Profile/${who.id}">${who.name}</a>
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
   <td>${TOOL.render(TOOL.element(RECORD.data,"data/text"),USER?if_exists)}</a></td>
  </tr>
  <tr>
   <td colspan="2">
    Akce smí provádìt jen vlastník nebo admin:
    <a href="${URL.make("/edit?action=editRecord&rid="+RELATION.id+"&recordId="+RECORD.id)}"
    title="Uprav záznam"><img src="/images/actions/pencil.png" border="0" width="22" height="22"
    alt="Uprav záznam"></a>
    &nbsp;
    <#if RECORDS?size gt 1> <#assign cislo=REL_RECORD.id> <#else> <#assign cislo=REL_ITEM.id></#if>
    <a href="${URL.noPrefix("/EditRelation?action=remove&prefix=/software&rid="+cislo)}"
    title="Sma¾"><img src="/images/actions/delete.png" border="0" alt="Sma¾" width="32" height="32"></a>
   </td>
  </tr>
 </table>
 <br>
</#list>

<#include "../footer.ftl">
