<#assign who=TOOL.createUser(ITEM.owner)>
<#assign plovouci_sloupec>

  <@lib.advertisement id="hypertext2nahore" />

  <div class="s_nadpis">Nástroje</div>
  <div class="s_sekce">
    <ul>
    <#if USER??>
        <#if USER.id==ITEM.owner || TOOL.permissionsFor(USER, RELATION).canModify()>
            <li><a href="${URL.noPrefix("/bazar/edit/"+RELATION.id+"?action=edit")}">Uprav inzerát</a></li>
            <li><a href="${URL.noPrefix("/bazar/inset/"+RELATION.id+"?action=addScreenshot")}">Přidej obrázek</a></li>
            <li><a href="${URL.noPrefix("/bazar/edit/"+RELATION.id+"?action=rm")}">Smaž inzerát</a></li>
            <li><a href="${URL.noPrefix("/bazar/inset/"+RELATION.id+"?action=manage")}">Správa příloh</a></li>
        </#if>
    <#else>
        <li><a href="${URL.noPrefix("/Profile?action=login&amp;url="+REQUEST_URI)}">Přihlásit se</a></li>
    </#if>
    </ul>

  </div>

  <@lib.advertisement id="square" />
  <@lib.advertisement id="hypertext2dole" />

</#assign>

<#include "../header.ftl">
<#import "../misc/lib-bazar.ftl" as bazarlib>

<@lib.showMessages/>

<div class="bazar">
<@bazarlib.showBazaarAd ITEM, who />

<@lib.showGallery ITEM />
</div> <!-- bazar -->

<@lib.showPageTools RELATION />

<h3>Komentáře</h3>
<#if CHILDREN.discussion??>
    <@lib.showDiscussion CHILDREN.discussion[0]/>
<#else>
   <a href="${URL.make("/EditDiscussion?action=addDiz&amp;rid="+RELATION.id)}">Vložit první komentář</a>
</#if>


<#include "../footer.ftl">
