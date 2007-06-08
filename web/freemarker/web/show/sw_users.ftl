<#include "../header.ftl">

<h1>Seznam uživatelů pro <a href="${RELATION.url}">${TOOL.xpath(ITEM,"/data/name")?if_exists}</a></h1>

<#assign usedby = ITEM.getProperty("used_by")>
<ul>
    <#list usedby as sw_user>
        <#assign who = TOOL.createUser(sw_user)>
        <li>
            <a href="/Profile/${sw_user}">${who.nick?default(who.name)}</a>
        </li>
    </#list>
</ul>

<#include "../footer.ftl">
