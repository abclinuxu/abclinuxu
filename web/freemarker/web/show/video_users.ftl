<#include "../header.ftl">

<#assign usedBy=ITEM.getProperty("favourited_by"), autor=TOOL.createUser(ITEM.owner)>

<h1>Seznam uživatelů, kterým se líbí video</h1>

<p><a href="${RELATION.url}">${ITEM.title?if_exists}</a> od uživatele <@lib.showUser autor/></p>

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
