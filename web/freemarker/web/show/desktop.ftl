<#import "../macros.ftl" as lib>

<#assign plovouci_sloupec>
  <#if USER??>
    <#assign permissions = TOOL.permissionsFor(USER, RELATION)>
    <#if USER.id==ITEM.owner || permissions.canModify()>
        <div class="s_nadpis">Nástroje</div>
        <div class="s_sekce">
            <ul>
                <li><a href="${URL.make("/edit/"+RELATION.id+"?action=edit")}">Upravit</a></li>
                <#if USER.id==ITEM.owner || permissions.canDelete()>
                    <li>
                        <a href="${URL.make("/edit/"+RELATION.id+"?action=rm2"+TOOL.ticket(USER, false))}" title="Smaž"
                        onClick="javascript:return confirm('Opravdu chcete smazat tento desktop?');">Smazat</a>
                    </li>
                </#if>
            </ul>
        </div>
    </#if>
  </#if>

  <#if MY_OLDER_DESKTOPS!?has_content>
     <div class="s_nadpis">Mé další desktopy</div>
     <div class="s_sekce" align="center">
         <#list MY_OLDER_DESKTOPS as rel>
             <@lib.showTopDesktop rel />
         </#list>
     </div>
  </#if>
  <div class="s_nadpis">Nejoblíbenější desktopy</div>
  <div class="s_sekce" align="center">
    <#list TOOL.sublist(VARS.mostPopularDesktops.keySet(), 0, 5) as rel>
        <@lib.showTopDesktop rel />
    </#list>
  </div>
  &nbsp;<a href="/nej">další&nbsp;&raquo;</a>
</#assign>

<#include "../header.ftl">

<@lib.showMessages/>
<#assign desktop = TOOL.createScreenshot(RELATION)>

<div class="desktopy">

<h1>${desktop.title?html}</h1>

<div>
    <a href="${desktop.imageUrl}" title="${desktop.title?html}" class="thumb">
        <img src="${desktop.thumbnailDetailUrl}" alt="${desktop.title?html}" border="0">
    </a>
</div>

<#assign themeUrl=TOOL.xpath(ITEM, "/data/theme_url")!"UNDEFINED">
<#if themeUrl != "UNDEFINED">
    <p>Adresa tématu či pozadí: <a href="${themeUrl}">${themeUrl}</a></p>
</#if>

<#assign desc=TOOL.xpath(ITEM, "/data/description")!"UNDEFINED">
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
        <#if USER?? && usedBy.contains(""+USER.id)>
            <input type="submit" value="Odebrat se" class="button">
        <#else>
            <input type="submit" value="Přidat se" class="button">
        </#if>
        <input type="hidden" name="action" value="favourite">
        <input type="hidden" name="ticket" value="${TOOL.ticketValue(USER!)}">
  </form>
</p>

</div>

<@lib.showPageTools RELATION />

<h3>Komentáře</h3>
<#if CHILDREN.discussion??>
    <@lib.showDiscussion CHILDREN.discussion[0]/>
<#else>
   <a href="${URL.make("/EditDiscussion?action=addDiz&amp;rid="+RELATION.id)}">Vložit první komentář</a>
</#if>

<#include "../footer.ftl">
