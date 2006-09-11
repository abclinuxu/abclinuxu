<#assign who=TOOL.createUser(ITEM.owner)>
<#if USER?exists && TOOL.xpath(ITEM,"//monitor/id[text()='"+USER.id+"']")?exists>
    <#assign monitorState="P�esta� sledovat"><#else><#assign monitorState="Sleduj ot�zku">
</#if>
<#assign plovouci_sloupec>
    <div class="s_sekce">
        <ul>
            <li>
                <a href="/Profile/${who.id}">${who.nick?default(who.name)}</a>
                (${DATE.show(ITEM.updated,"CZ_FULL")})
            </li>
            <#if PARAMS.revize?exists>
                <li>
                    <a href="${RELATION.url}">N�vrat na aktu�ln� verzi</a>
                </li>
            <#else>
                <li><a href="${URL.make("/edit/"+RELATION.id+"?action=edit")}">Upravit</a></li>
                <li><a href="${URL.noPrefix("/EditRelated/"+RELATION.id)}">Souvisej�c� dokumenty</a></li>
                <li><a href="/revize?rid=${RELATION.id}&amp;prefix=/faq">Historie</a></li>
                <li><a href="${RELATION.url}?varianta=print">Tisk ot�zky</a></li>
                <li>
                    <a href="${URL.make("/monitor/"+RELATION.id+"?action=toggle")}">${monitorState}</a>
                    <span title="Po�et lid�, kte�� sleduj� tuto ot�zku">(${TOOL.getMonitorCount(ITEM.data)})</span>
                    <a class="info" href="#">?<span class="tooltip">Za�le upozorn�n� na v� email p�i �prav� ot�zky</span></a>
                </li>
                <#if USER?exists && USER.hasRole("root")>
                    <li>
                        <a href="${URL.noPrefix("/EditRelation?action=remove&amp;rid="+RELATION.id+"&amp;prefix=/faq")}">Smazat ot�zku</a>
                    </li>
                </#if>
                <#if USER?exists && USER.hasRole("move relation")>
                    <li>
                        <a href="${URL.noPrefix("/SelectRelation?rid="+RELATION.id+"&amp;prefix="+URL.prefix+"&amp;url=/EditRelation&amp;action=move")}">P�esunout</a>
			<a href="${URL.noPrefix("/SelectRelation?rid="+RELATION.id+"&amp;url=/EditRelation&amp;action=add&amp;prefix="+URL.prefix)}">Vytvo�it link</a>
                    </li>
                </#if>
            </#if>
        </ul>
    </div>
</#assign>

<#include "../header.ftl">

<h1 style="margin-bottom:1em;">${TOOL.xpath(ITEM, "/data/title")}</h1>

<div>
    ${TOOL.render(TOOL.xpath(ITEM.data,"data/text"), USER?if_exists)}
</div>

<@lib.showRelated ITEM/>

<#include "../footer.ftl">
