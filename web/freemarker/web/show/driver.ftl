<#import "../macros.ftl" as lib>
<#assign who=TOOL.createUser(ITEM.owner)>

<#assign plovouci_sloupec>
    <div class="s_sekce">
        <ul>
            <#if PARAMS.revize??>
                <li>
                    <a href="${RELATION.url!("/ovladace/show/"+RELATION.id)}">Návrat na aktuální verzi</a>
                </li>
            <#else>
            <#if USER?? && TOOL.permissionsFor(USER, RELATION).canModify()>
                <li><a href="${URL.make("/edit/"+RELATION.id+"?action=edit")}">Upravit</a></li>
            </#if>
                <li><a href="${URL.noPrefix("/EditRelated/"+RELATION.id)}">Související dokumenty</a></li>
                <li>
                    <@lib.showMonitor RELATION />
                </li>
                <form action="/hledani"><input type="text" class="text" name="dotaz" value="${ITEM.title}">
                    <input type="submit" class="button" value="Hledej">
                </form>
                <#if USER?? && TOOL.permissionsFor(USER, RELATION).canDelete()>
                    <li><a href="${URL.noPrefix("/EditRelation/"+RELATION.id+"?action=remove&amp;prefix=/ovladace")}">Smazat</a></li>
                </#if>
            </#if>
        </ul>
    </div>
</#assign>

<#include "../header.ftl">

<@lib.advertisement id="arbo-sq" />

<h1>${ITEM.title}</h1>

<table class="swdetail">
  <tr>
    <td><b>Verze:</b></td>
    <td>${TOOL.xpath(ITEM,"data/version")}</td>
  </tr>
  <tr>
    <td><b>Adresa:</b></td>
    <td><a href="${TOOL.xpath(ITEM,"data/url")?html}" rel="nofollow">${TOOL.limit(TOOL.xpath(ITEM,"data/url"),50," ..")?html}</a></td>
  </tr>
</table>

<h3>Poznámka:</h3>

<div>${TOOL.render(TOOL.element(ITEM.data,"data/note"),USER!)}</div>

<@lib.showRelated ITEM/>

<@lib.showRevisions RELATION, REVISIONS/>

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
            <td>${info.description!}</td>
        </tr>
    </#list>
</table>

<@lib.showPageTools RELATION />

<#include "../footer.ftl">
