<#import "../macros.ftl" as lib>
<#assign intro=TOOL.xpath(BLOG,"//custom/intro")!"UNDEF">
<#assign title=BLOG.title!"UNDEF">
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
        <#if USER?? && USER.id==BLOG.owner>
            <a href="${URL.noPrefix("/blog/edit/"+REL_BLOG.id+"?action=categories")}">Kategorie zápisů</a>
        <#else>
            Kategorie zápisů
        </#if>
    </div>

    <div class="s_sekce">
    <ul>
        <#list CATEGORIES as cat>
            <#if cat.url??>
                <li><a href="/blog/${BLOG.subType + "/" + cat.url}">${cat.name}</a></li>
            </#if>
        </#list>
    </div>

    <#if UNPUBLISHED_STORIES??>
        <div class="s_nadpis">Rozepsané zápisy</div>

        <div class="s_sekce">
            <ul>
            <#list UNPUBLISHED_STORIES as relation>
                <#assign story=relation.child, url=TOOL.getUrlForBlogStory(relation)>
                <li>
                    <a href="${url}">${story.title}</a>
                </li>
            </#list>
            </ul>
        </div>
    </#if>

    <#if WAITING_STORIES??>
        <div class="s_nadpis">Čekající zápisky</div>

        <div class="s_sekce">
            <ul>
            <#list WAITING_STORIES as relation>
                <#assign story=relation.child, url=TOOL.getUrlForBlogStory(relation)>
                <li>
                    <a href="${url}">${story.title}</a>
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
                <a href="${url}">${story.title}</a>
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

    <#if LAST_DESKTOP??>
        <div class="s_nadpis">
            <a class="info" href="#">?<span class="tooltip">Poslední screenshot mého desktopu.</span></a>
            Současný desktop
        </div>

        <div class="s_sekce" align="center">
            <#assign desktop_title=LAST_DESKTOP.title>
            <a href="${LAST_DESKTOP.url}" title="${desktop_title?html}" class="thumb">
                <img src="${LAST_DESKTOP.thumbnailListingUrl}" alt="${desktop_title?html}" border="0">
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


    <#if (USER?? && USER.id==BLOG.owner) || (! USER??)>
        <div class="s_nadpis">
            <a class="info" href="#">?<span class="tooltip">Tato sekce sdružuje akce pro majitele blogu.</span></a>
            Nastavení
        </div>
    </#if>

  <div class="s_sekce">
    <ul>
    <#if USER??>
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

    <div class="s_nadpis"><a href="/nej">Nej blogů na AbcLinuxu</a></div>
    <div class="s_sekce">
        <#if VARS.recentMostReadStories??>
            <b>Nejčtenější za poslední měsíc</b>
            <ul>
                <#list VARS.recentMostReadStories.entrySet() as rel>
                    <#if rel_index gt 2><#break></#if>
                    <li><a href="${rel.key.url}">${TOOL.childName(rel.key)}</a></li>
                </#list>
            </ul>
        </#if>

        <#if VARS.recentMostCommentedStories??>
            <b>Nejkomentovanější za poslední měsíc</b>
            <ul>
                <#list VARS.recentMostCommentedStories.entrySet() as rel>
                    <#if rel_index gt 2><#break></#if>
                    <li><a href="${rel.key.url}">${TOOL.childName(rel.key)}</a></li>
                </#list>
            </ul>
        </#if>
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
