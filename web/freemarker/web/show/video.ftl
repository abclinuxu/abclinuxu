<#if USER?exists>
    <#assign plovouci_sloupec>
        <div class="s_nadpis">Úpravy</div>
        <div class="s_sekce">
            <ul>
                <#if TOOL.permissionsFor(USER, RELATION).canModify() || ITEM.owner == USER.id>
                    <li><a href="${URL.make("/edit/"+RELATION.id+"?action=edit")}">Upravit</a></li>
                </#if>
                <#if TOOL.permissionsFor(USER, RELATION).canDelete() || ITEM.owner == USER.id>
                    <li><a href="${URL.make("/edit/"+RELATION.id+"?action=remove"+TOOL.ticket(USER,false))}">Smazat</a></li>
                </#if>
            </ul>
        </div>
    </#assign>
</#if>

<#include "../header.ftl">
<@lib.showMessages/>

<h1>${ITEM.title}</h1>

<@lib.showVideo RELATION, 500, 400, false />

<p><i>K přehrávání videa je nutné mít Adobe Flash plugin.</i></p>


<#if CHILDREN.discussion?exists>
    <h3>Komentáře</h3>
    <@lib.showDiscussion CHILDREN.discussion[0]/>
</#if>

<#include "../footer.ftl">
