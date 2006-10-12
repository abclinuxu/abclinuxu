<#assign who=TOOL.createUser(ITEM.owner)>
<#assign plovouci_sloupec>
  <div class="s_sekce">
    <ul>
    <#if USER?exists>
        <#if USER.id==ITEM.owner || USER.hasRole("bazaar admin")>
            <li><a href="${URL.make("/edit/"+RELATION.id+"?action=edit")}">Uprav inzer�t</a></li>
            <li><a href="${URL.make("/inset/"+RELATION.id+"?action=addScreenshot")}">P�idej obr�zek</a></li>
            <li><a href="${URL.make("/edit/"+RELATION.id+"?action=remove")}">Sma� inzer�t</a></li>
            <#if USER.hasRole("attachment admin")>
                <li><a href="${URL.make("/inset/"+RELATION.id+"?action=manage")}">Spr�va p��loh</a></li>
            </#if>
        </#if>
    <#else>
        <li><a href="${URL.noPrefix("/Profile?action=login&amp;url="+REQUEST_URI)}">P�ihl�sit se</a></li>
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
    <h3>Obr�zky</h3>

    <p class="galerie">
        <#list images as image>
            <#if image.thumbnailPath?exists>
                <a href="${image.path}"><img src="${image.thumbnailPath}" alt="Obr�zek ${image_index}" border="0"></a>
            <#else>
                <img src="${image.path}" alt="Obr�zek ${image_index}">
            </#if>
        </#list>
    </p>
</#if>

<h3>Koment��e</h3>
<#if CHILDREN.discussion?exists>
    <@lib.showDiscussion CHILDREN.discussion[0]/>
<#else>
   <a href="${URL.make("/EditDiscussion?action=addDiz&amp;rid="+RELATION.id)}">Vlo�it prvn� koment��</a>
</#if>


<#include "../footer.ftl">
