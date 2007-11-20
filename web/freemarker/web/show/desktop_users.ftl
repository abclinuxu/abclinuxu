<#include "../header.ftl">

<div style="float:right">
    <a href="${RELATION.url}" title="${TOOL.xpath(ITEM,"/data/title")?if_exists}">
        <img src="${TOOL.xpath(ITEM,"/data/listingThumbnail")}" alt="${TOOL.xpath(ITEM,"/data/title")?if_exists}" border="0">
    </a>
</div>

<h1 class="st_nadpis">Seznam uživatelů, kterým se líbí desktop „<a href="${RELATION.url}">${TOOL.xpath(ITEM,"/data/title")?if_exists}</a>“</h1>

<#assign usedby = ITEM.getProperty("favourited_by")>
<ul>
    <#list usedby as a_user>
        <#assign who = TOOL.createUser(a_user)>
        <li>
            <@lib.showUser who/>
        </li>
    </#list>
</ul>

<#include "../footer.ftl">
