<#assign who=TOOL.createUser(ITEM.owner)>
<#assign plovouci_sloupec>
  <div class="s_sekce">
    <ul>
    <#if USER?exists>
        <#if USER.id==ITEM.owner || USER.hasRole("bazaar admin")>
            <li><a href="${URL.make("/edit/"+RELATION.id+"?action=edit")}">Uprav inzerát</a></li>
            <li><a href="${URL.make("/inset/"+RELATION.id+"?action=addScreenshot")}">Pøidej obrázek</a></li>
            <li><a href="${URL.make("/edit/"+RELATION.id+"?action=remove")}">Sma¾ inzerát</a></li>
            <#if USER.hasRole("attachment admin")>
                <li><a href="${URL.make("/inset/"+RELATION.id+"?action=manage")}">Správa pøíloh</a></li>
            </#if>
        </#if>
    <#else>
        <li><a href="${URL.noPrefix("/Profile?action=login&amp;url="+REQUEST_URI)}">Pøihlásit se</a></li>
    </#if>
    </ul>
  </div>
</#assign>
<#include "../header.ftl">
<#import "../misc/lib-bazar.ftl" as bazarlib>

<@lib.showMessages/>

<@bazarlib.showBazaarAd ITEM, who />

<#assign images = TOOL.screenshotsFor(ITEM)>
<#if (images?size > 0)>
    <h3>Obrázky</h3>

    <p class="galerie">
        <#list images as image>
            <#if image.thumbnailPath?exists>
                <a href="${image.path}"><img src="${image.thumbnailPath}" alt="Obrázek ${image_index}" border="0"></a>
            <#else>
                <img src="${image.path}" alt="Obrázek ${image_index}">
            </#if>
        </#list>
    </p>
</#if>

<h3>Komentáøe</h3>
<#if CHILDREN.discussion?exists>
    <@lib.showDiscussion CHILDREN.discussion[0]/>
<#else>
   <a href="${URL.make("/EditDiscussion?action=addDiz&amp;rid="+RELATION.id)}">Vlo¾it první komentáø</a>
</#if>


<#include "../footer.ftl">
