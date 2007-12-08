<#if USER?exists>
    <#if USER.id==ITEM.owner || USER.hasRole("attachment admin")>
    <#assign plovouci_sloupec>

        <div class="s_nadpis">Nástroje</div>

        <div class="s_sekce">
            <ul>
                <li>
                    <a href="${URL.make("/edit/"+RELATION.id+"?action=edit")}" rel="nofollow">Upravit</a>
                </li>
                <li>
                    <a href="${URL.make("/edit/"+RELATION.id+"?action=rm2"+TOOL.ticket(USER, false))}" title="Smaž"
                    onClick="javascript:return confirm('Opravdu chcete smazat tento desktop?');">Smazat</a>
                </li>
            </ul>
        </div>
    </#assign>
    </#if>
</#if>

<#include "../header.ftl">

<@lib.showMessages/>

<div class="desktopy">

<h1>${TOOL.xpath(ITEM,"/data/title")}</h1>

<div>
    <a href="${TOOL.xpath(ITEM,"/data/image")}" title="${TOOL.xpath(ITEM,"/data/title")}" class="thumb">
        <img src="${TOOL.xpath(ITEM,"/data/detailThumbnail")}" alt="${TOOL.xpath(ITEM,"/data/title")}" border="0">
    </a>
</div>

<#assign desc=TOOL.xpath(ITEM, "/data/description")?default("UNDEFINED")>
<#if desc != "UNDEFINED">
    <p class="popis">${desc}</p>
</#if>

<p>
  <form action="${URL.make("/desktopy/edit/"+RELATION.id)}" class="meta-vypis">
    <#assign usedBy=ITEM.getProperty("favourited_by"), autor=TOOL.createUser(ITEM.owner)>
        <@lib.showUser autor/> |
        ${DATE.show(ITEM.created,"SMART_DMY")} |
        Zhlédnuto: <#assign reads = TOOL.getCounterValue(ITEM,"read")>${reads}&times; |
        <#if (usedBy?size > 0)>
            <a href="?action=users" title="Seznam uživatelů abclinuxu, kterým se líbí tento desktop">Oblíbenost: ${usedBy?size}</a>
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

</div>

<h3>Komentáře</h3>
<#if CHILDREN.discussion?exists>
    <@lib.showDiscussion CHILDREN.discussion[0]/>
<#else>
   <a href="${URL.make("/EditDiscussion?action=addDiz&amp;rid="+RELATION.id)}">Vložit první komentář</a>
</#if>

<#include "../footer.ftl">
