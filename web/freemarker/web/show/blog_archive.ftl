<#import "../macros.ftl" as lib>
<#assign intro=TOOL.xpath(BLOG,"//custom/intro")?default("UNDEF")>
<#assign title=TOOL.xpath(BLOG,"//custom/title")?default("UNDEF")>
<#assign owner=TOOL.createUser(BLOG.owner)>

<#assign plovouci_sloupec>

    <div class="s_nadpis">
        <@lib.showUser owner/>
        <#if title!="UNDEF"> - <a href="/blog/${BLOG.subType}">${title}</a></#if>
    </div>

    <div class="s_sekce">
        <#if intro!="UNDEF">${intro}</#if>
    </div>

    <div class="s_nadpis">
        <#if USER?exists && USER.id==BLOG.owner>
            <a href="${URL.noPrefix("/blog/edit/"+REL_BLOG.id+"?action=categories")}">Kategorie zápisů</a>
        <#else>
            Kategorie zápisů
        </#if>
    </div>

    <div class="s_sekce">
    <ul>
        <#list CATEGORIES as cat>
            <#if cat.url?exists>
                <li><a href="/blog/${BLOG.subType + "/" + cat.url}">${cat.name}</a></li>
            </#if>
        </#list>
    </div>

    <#if UNPUBLISHED_STORIES?exists>
        <div class="s_nadpis">Rozepsané zápisy</div>

        <div class="s_sekce">
            <ul>
            <#list UNPUBLISHED_STORIES as relation>
                <#assign story=relation.child, url=TOOL.getUrlForBlogStory(relation)>
                <li>
                    <a href="${url}">${TOOL.xpath(story, "/data/name")}</a>
                </li>
            </#list>
            </ul>
        </div>
    </#if>

    <div class="s_nadpis">Aktuální zápisy</div>

    <div class="s_sekce">
        <ul>
        <#list CURRENT_STORIES as relation>
            <#assign story=relation.child, url=TOOL.getUrlForBlogStory(relation)>
            <li>
                <a href="${url}">${TOOL.xpath(story, "/data/name")}</a>
            </li>
        </#list>
        </ul>
    </div>

    <#assign links = BLOG_XML.data.custom.links.link>
    <#if links?size!=0>
        <div class="s_nadpis">
            <a class="info" href="#">?<span class="tooltip">Seznam mých oblíbených stránek, které pravidelně navštěvuji.</span></a>
            Oblíbené stránky
        </div>

        <div class="s_sekce">
            <ul>
            <#list links as link>
                <li><a href="${link}" rel="nofollow">${link.@caption}</a></li>
            </#list>
            </ul>
        </div>
    </#if>

    <#if LAST_DESKTOP?exists>
        <div class="s_nadpis">
            <a class="info" href="#">?<span class="tooltip">Poslední screenshot mého desktopu.</span></a>
            Současný desktop
        </div>

        <div class="s_sekce" align="center">
            <#assign desktop_title=TOOL.xpath(LAST_DESKTOP.child,"/data/title")>
            <a href="${LAST_DESKTOP.url}" title="${desktop_title?html}">
                <img src="${TOOL.xpath(LAST_DESKTOP.child,"/data/listingThumbnail")}" alt="${desktop_title?html}" border="0">
            </a>
        </div>
    </#if>

    <div class="s_nadpis">
        <a class="info" href="#">?<span class="tooltip">Přístup na osobní hlavní stranu a na hlavní stranu všech blogů.</span></a>
        Navigace
    </div>

    <div class="s_sekce">
        <ul>
            <#if title!="UNDEF">
                <li><a href="/blog/${BLOG.subType}">${title}, hlavní strana</a></li>
            </#if>
            <li><a href="/blog/${BLOG.subType}/souhrn"><#if title!="UNDEF">${title}, </#if>stručný souhrn</a></li>
            <li><a href="/auto/blog/${BLOG.subType}.rss">RSS kanál</a></li>
            <li><a href="/blog">Všechny blogy</a></li>
	    <li><a href="/blog/souhrn">Všechny blogy, stručný souhrn</a></li>
        </ul>
    </div>


    <#if (USER?exists && USER.id==BLOG.owner) || (! USER?exists)>
        <div class="s_nadpis">
            <a class="info" href="#">?<span class="tooltip">Tato sekce sdružuje akce pro majitele blogu.</span></a>
            Nastavení
        </div>
    </#if>

  <div class="s_sekce">
    <ul>
    <#if USER?exists>
        <#if USER.id==BLOG.owner>
            <li><a href="${URL.noPrefix("/blog/edit/"+REL_BLOG.id+"?action=add")}">Vlož nový zápis</a></li>
            <li><a href="${URL.noPrefix("/blog/edit/"+REL_BLOG.id+"?action=custom")}">Nastavit blog</a></li>
            <li><a href="${URL.noPrefix("/blog/edit/"+REL_BLOG.id+"?action=rename")}">Přejmenovat blog</a></li>
            <li><a href="${URL.noPrefix("/blog/edit/"+REL_BLOG.id+"?action=links")}">Upravit oblíbené stránky</a></li>
        </#if>
    <#else>
        <li><a href="${URL.noPrefix("/Profile?action=login&amp;url="+REQUEST_URI)}">Přihlásit se</a></li>
    </#if>
    </ul>
  </div>

  <@lib.advertisement id="gg-sq-blog" />

</#assign>

<#include "../header.ftl">
<@lib.showMessages/>

<h1>Archiv</h1>

<#list BLOG_XML.data.archive.year?reverse as year>
    <h2>Rok ${year.@value}</h2>
    <ul>
    <#list year.month as month>
        <li>
            <a href="/blog/${BLOG.subType}/${year.@value}/${month.@value}/"><@lib.month month=month.@value/> (${month})</a>
        </li>
    </#list>
    </ul>
</#list>

<#include "../footer.ftl">
