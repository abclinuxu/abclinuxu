<#include "../header.ftl">

<#assign who=TOOL.createUser(ITEM.owner)>
<p>
    <#if PARAMS.revize?exists>
        Pr�v� si prohl��te revizi ��slo ${PARAMS.revize}, kterou vytvo�il
        <a href="/Profile/${who.id}">${who.nick?default(who.name)}</a>
        (${DATE.show(ITEM.updated,"CZ_FULL")}).
        <a href="${RELATION.url?default("/ovladace/show/"+RELATION.id)}">N�vrat na aktu�ln� verzi</a>.
    <#else>
        Tuto polo�ku naposledy upravil <a href="/Profile/${who.id}">${who.nick?default(who.name)}</a>
        (${DATE.show(ITEM.updated,"CZ_FULL")}).
        Pokud chcete doplnit, opravit nebo aktualizovat ovlada�,
        <a href="${URL.make("/edit?action=edit&amp;rid="+RELATION.id)}">vlo�te novou verzi</a>
        nebo <a href="${URL.noPrefix("/EditRelated/"+RELATION.id)}">upravte souvisej�c� dokumenty</a>.

        K dispozici je i <a href="/revize?rid=${RELATION.id}&amp;prefix=/ovladace">archiv zm�n</a>
        tohoto ovlada�e, tak�e si m��ete prohl�dnout, jak�mi zm�nami ovlada� pro�el postupem �asu.
    </#if>
</p>
<#if USER?exists && USER.hasRole("remove relation")>
  <a href="${URL.noPrefix("/EditRelation?action=remove&amp;prefix=/ovladace&amp;rid="+RELATION.id)}">Smazat</a>
</#if>

<p><b>AbcMonitor</b> v�m emailem za�le upozorn�n� p�i zm�n�.
 <#if USER?exists && TOOL.xpath(ITEM,"//monitor/id[text()='"+USER.id+"']")?exists>
  <#assign monitorState="Vypni">
 <#else>
  <#assign monitorState="Zapni">
 </#if>
 <a href="${URL.make("/edit?action=monitor&amp;rid="+RELATION.id+"&amp;driverId="+ITEM.id)}">${monitorState}</a>
 (${TOOL.getMonitorCount(ITEM.data)})
</p>
<hr />

<h1>${TOOL.xpath(ITEM,"data/name")}</h1>

<table class="swdetail">
  <tr>
    <td><b>Verze:</b></td>
    <td>${TOOL.xpath(ITEM,"data/version")}</td>
  </tr>
  <tr>
    <td><b>Adresa:</b></td>
    <td><a href="${TOOL.xpath(ITEM,"data/url")}" rel="nofollow">${TOOL.limit(TOOL.xpath(ITEM,"data/url"),50," ..")}</a></td>
  </tr>
</table>

<h3>Pozn�mka:</h3>

<div>${TOOL.render(TOOL.element(ITEM.data,"data/note"),USER?if_exists)}</div>

<@lib.showRelated ITEM/>

<p><b>N�stroje:</b> <a href="${RELATION.url?default("/ovladace/show/"+RELATION.id)}?varianta=print<#if PARAMS.revize?exists>&amp;revize=${PARAMS.revize}</#if>">Tisk</a></p>

<#include "../footer.ftl">
