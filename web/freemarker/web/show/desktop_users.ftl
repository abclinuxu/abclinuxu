<#include "../header.ftl">

<#assign desktop = TOOL.createScreenshot(RELATION)>
<div class="desktopy">
<div class="thumb uzivatele">
    <a href="${desktop.imageUrl}" title="${desktop.title}" class="thumb">
        <img src="${desktop.thumbnailDetailUrl}" alt="${desktop.title}" border="0">
    </a>
</div>

<#assign usedBy=ITEM.getProperty("favourited_by"), autor=TOOL.createUser(ITEM.owner)>

<h1>Seznam uživatelů, kterým se líbí desktop</h1>

<p><a href="${RELATION.url}">${TOOL.xpath(ITEM,"/data/title")?if_exists}</a> od uživatele <@lib.showUser autor/></p>

<ul>
    <#list usedBy as a_user>
        <#assign who=TOOL.createUser(a_user)>
        <li>
            <@lib.showUser who/>
        </li>
    </#list>
</ul>

</div>

<#include "../footer.ftl">
