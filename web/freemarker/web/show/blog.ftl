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

    <#if (CATEGORIES!?size > 0)>
        <div class="s_nadpis">Kategorie zápisků</div>
        <div class="s_sekce">
            <ul>
                <#list CATEGORIES as cat>
                    <#if cat.url??>
                        <li><a href="/blog/${BLOG.subType + "/" + cat.url}">${cat.name}</a></li>
                    </#if>
                </#list>
            </ul>
        </div>
    </#if>

    <#if (UNPUBLISHED_STORIES!?size > 0)>
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

    <#if (WAITING_STORIES!?size > 0)>
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

    <#if (USER?? && USER.id==BLOG.owner)>
        <div class="s_nadpis">
            Správa blogu
        </div>

        <div class="s_sekce">
            <ul>
                <li><a href="${URL.noPrefix("/blog/edit/"+REL_BLOG.id+"?action=add")}">Vlož nový zápis</a></li>
                <li><a href="${URL.noPrefix("/blog/edit/"+REL_BLOG.id+"?action=custom")}">Nastavit blog</a></li>
                <li><a href="${URL.noPrefix("/blog/edit/"+REL_BLOG.id+"?action=rename")}">Přejmenovat blog</a></li>
                <li><a href="${URL.noPrefix("/blog/edit/"+REL_BLOG.id+"?action=categories")}">Správa kategorií</a></li>
                <li><a href="${URL.noPrefix("/blog/edit/"+REL_BLOG.id+"?action=links")}">Upravit oblíbené stránky</a></li>
                <li><a href="${URL.noPrefix("/blog/"+BLOG.subType+"/export")}">Export do Movable Type</a></li>
            </ul>
        </div>
    </#if>

    <div class="s_nadpis">Aktuální zápisy</div>

    <div class="s_sekce">
        <ul>
        <#list CURRENT_STORIES as relation>
            <#assign story=relation.child, url=TOOL.getUrlForBlogStory(relation)>
            <li>
                <a href="${url}" title="${story.title}">${story.title}</a>
            </li>
        </#list>
        </ul>
    </div>

    <div class="s_nadpis">
        <a class="info" href="#">?<span class="tooltip">Přístup k archivovaným zápisům za jednotlivé měsíce.</span></a>
        <a href="/blog/${BLOG.subType}/archiv">Archív</a>
    </div>

    <div class="s_sekce">
        <ul>
        <#list ARCHIVE as item>
            <li>
                <a href="/blog/${BLOG.subType}/${item.year}/${item.month}/"><@lib.month month=""+item.month/>
                ${item.year} (${item.size})</a>
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
	        <li><a href="/blog/souhrn">Stručný souhrn blogů</a></li>
	        <li><a href="/blog/vyber">Výběr z blogů</a></li>
	        <li>
                <@lib.showMonitor RELATION "Zašle upozornění na váš email při vytvoření nového zápisku v tomto blogu."/>
            </li>
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

  <hr id="arbo-sq-cara" />
  <@lib.advertisement id="arbo-sq" />
  <@lib.advertisement id="gg-sq-blog" />

</#assign>

<#include "../header.ftl">
<@lib.showMessages/>

<#if CATEGORY??>
    <h2>Zápisy v kategorii ${CATEGORY.name}</h2>
</#if>

<#if STORIES.total==0>
    <p>Vašemu výběru neodpovídá žádný zápis.</p>
</#if>

<#list STORIES.data as blogStory>
    <@lib.showStoryInListing blogStory, true, SUMMARY!false />
    <hr>
</#list>

<p>
    <#if SUMMARY??>
        <#assign url="/blog/"+BLOG.subType+"/souhrn">
    <#elseif CATEGORY??>
        <#assign url="/blog/"+BLOG.subType+"/"+CATEGORY>
    <#else>
        <#assign url="/blog/"+BLOG.subType+"/">
        <#if YEAR??><#assign url=url+YEAR+"/"></#if>
        <#if MONTH??><#assign url=url+MONTH+"/"></#if>
        <#if DAY??><#assign url=url+DAY+"/"></#if>
    </#if>
    <#if (STORIES.currentPage.row > 0) >
        <#assign start=STORIES.currentPage.row-STORIES.pageSize><#if (start<0)><#assign start=0></#if>
        <a href="${url}?from=${start}">Novější zápisy</a> &#8226;
    </#if>
    <#assign start=STORIES.currentPage.row + STORIES.pageSize>
    <#if (start < STORIES.total) >
        <a href="${url}?from=${start}">Starší zápisy</a>
    </#if>
</p>

<#include "../footer.ftl">
