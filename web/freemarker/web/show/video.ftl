<#if USER?exists>
    <#assign plovouci_sloupec>
        <div class="s_nadpis">Úpravy</div>
        <div class="s_sekce">
            <ul>
                <#if TOOL.permissionsFor(USER, RELATION).canModify() || ITEM.owner == USER.id>
                    <li><a href="${URL.make("/edit/"+RELATION.id+"?action=edit")}">Upravit</a></li>
                </#if>
                <#if TOOL.permissionsFor(USER, RELATION).canDelete() || ITEM.owner == USER.id>
                    <li><a href="${URL.make("/edit/"+RELATION.id+"?action=remove")}">Smazat</a></li>
                </#if>
            </ul>
        </div>
    </#assign>
</#if>

<#include "../header.ftl">
<@lib.showMessages/>

<h1>${ITEM.title}</h1>
<p class="meta-vypis">Vytvořeno: ${DATE.show(ITEM.created,"SMART")}
    | <@lib.showUser TOOL.createUser(ITEM.owner) />
    | Zhlédnuto: <#assign reads = TOOL.getCounterValue(ITEM,"read")>${reads}&times;</p>

<@lib.showVideoPlayer RELATION, 500, 400, false />

<p>
  <form action="${URL.make("/videa/edit/"+RELATION.id)}">
    <#assign usedBy=ITEM.getProperty("favourited_by"), autor=TOOL.createUser(ITEM.owner)>
        <#if (usedBy?size > 0)>
            <a href="?action=users" title="Seznam uživatelů AbcLinuxu, kterým se líbí toto video">Oblíbenost: ${usedBy?size}</a>
        <#else>
            Oblíbenost: 0
        </#if>
        <#if USER?exists && usedBy.contains(""+USER.id)>
            <input type="submit" value="Odebrat se" class="button">
        <#else>
            <input type="submit" value="Přidat se" class="button">
        </#if>
        <input type="hidden" name="action" value="favourite">
        <input type="hidden" name="ticket" value="${TOOL.ticketValue(USER?if_exists)}">
  </form>
</p>

<#if CHILDREN.discussion?exists>
    <h3>Komentáře</h3>
    <@lib.showDiscussion CHILDREN.discussion[0]/>
</#if>

<#include "../footer.ftl">
