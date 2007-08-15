<#assign who=TOOL.createUser(ITEM.owner)>
<#if USER?exists && TOOL.xpath(ITEM,"//monitor/id[text()='"+USER.id+"']")?exists>
    <#assign monitorState="Přestaň sledovat"><#else><#assign monitorState="Sleduj záznam">
</#if>

<#assign plovouci_sloupec>
    <div class="s_sekce">
        <ul>
            <li>
                <a href="/Profile/${who.id}">${who.nick?default(who.name)}</a><br />
                <span class="regular-color">(${DATE.show(ITEM.updated,"CZ_FULL")})</span>
            </li>
            <#if PARAMS.revize?exists>
                <li>
                    <a href="${RELATION.url?default("/ovladace/show/"+RELATION.id)}">Návrat na aktuální verzi</a>
                </li>
            <#else>
                <li><a href="${URL.make("/edit/"+RELATION.id+"?action=edit")}">Upravit</a></li>
                <li><a href="${URL.noPrefix("/EditRelated/"+RELATION.id)}">Související dokumenty</a></li>
                <li><a href="/revize?rid=${RELATION.id}&amp;prefix=/ovladace">Historie</a></li>
                <li><a href="${RELATION.url?default("/ovladace/show/"+RELATION.id)}?varianta=print">Tisk</a></li>
                <li>
                    <a href="${URL.make("/EditMonitor/"+RELATION.id+"?action=toggle"+TOOL.ticket(USER?if_exists, false))}">${monitorState}</a>
                    <span title="Počet lidí, kteří sledují tento dokument">(${TOOL.getMonitorCount(ITEM.data)})</span>
                    <a class="info" href="#">?<span class="tooltip">Zašle upozornění na váš email při úpravě dokumentu.</span></a>
                </li>
                <form action="/hledani"><input type="text" class="text" name="dotaz" value="${TOOL.xpath(ITEM,"/data/name")}">
                    <input type="submit" class="button" value="Hledej">
                </form>
                <#if USER?exists && USER.hasRole("remove relation")>
                    <li><a href="${URL.noPrefix("/EditRelation/"+RELATION.id+"?action=remove&amp;prefix=/ovladace")}">Smazat</a></li>
                </#if>
            </#if>
        </ul>
    </div>
</#assign>

<#include "../header.ftl">

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

<h3>Poznámka:</h3>

<div>${TOOL.render(TOOL.element(ITEM.data,"data/note"),USER?if_exists)}</div>

<@lib.showRelated ITEM/>

<hr/>

<h3>Přehled posledních změn:</h3>

<table border="1">
    <tr>
        <th>Verze</th>
        <th>Datum</th>
        <th>Popis změn</th>
    </tr>
    <#list HISTORY as info>
        <tr>
            <td>
                <a href="${RELATION.url}<#if info_index!=0>${REVISION_PARAM}${info.version}</#if>">${DRIVER_VERSIONS[info_index]}</a>
            </td>
            <td align="right">
                ${DATE.show(info.commited,"SMART")}
            </td>
            <td>${info.description?if_exists}</td>
        </tr>
    </#list>
</table>

<#include "../footer.ftl">
