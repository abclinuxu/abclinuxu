<#assign who=TOOL.createUser(ITEM.owner)>
<#if USER?exists && TOOL.xpath(ITEM,"//monitor/id[text()='"+USER.id+"']")?exists>
    <#assign monitorState="Pøestaò sledovat"><#else><#assign monitorState="Sleduj otázku">
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
                    <a href="${RELATION.url}">Návrat na aktuální verzi</a>
                </li>
            <#else>
                <li><a href="${URL.make("/faq/edit?action=edit&amp;rid="+RELATION.id)}">Upravit</a></li>
                <li><a href="/revize?rid=${RELATION.id}&amp;prefix=/faq">Historie</a></li>
                <li><a href="${RELATION.url}?varianta=print">Tisk otázky</a></li>
                <li>
                    <a href="${URL.make("/faq/edit?action=monitor&amp;rid="+RELATION.id)}">${monitorState}</a>
                    <span title="Poèet lidí, kteøí sledují tuto otázku">(${TOOL.getMonitorCount(ITEM.data)})</span>
                    <a class="info" href="#">?<span class="tooltip">Za¹le upozornìní na vá¹ email pøi úpravì otázky</span></a>
                </li>
                <#if USER?exists && USER.hasRole("root")>
                    <li>
                        <a href="${URL.noPrefix("/EditRelation?action=remove&amp;rid="+RELATION.id+"&amp;prefix=/faq")}">Sma¾ otázku</a>
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

<#if XML.data.links[0]?exists>
<div class="cl_perex">
  <h3>Související odkazy</h3>
    <div class="s_sekce">
        <ul>
	    <#list XML.data.links.link as link>
    	        <li>
        	    <a href="${link.@url}">${link}</a>
        	</li>
    	    </#list>
	</ul>
    </div>
</div>
</#if>

<#include "../footer.ftl">
