<#import "../macros.ftl" as lib>
<#assign intro=TOOL.xpath(BLOG,"//custom/intro")?default("UNDEF")>
<#assign title=TOOL.xpath(BLOG,"//custom/title")?default("UNDEF")>
<#assign owner=TOOL.createUser(BLOG.owner)>
<#if USER?exists && TOOL.xpath(BLOG,"//monitor/id[text()='"+USER.id+"']")?exists>
    <#assign monitorState="Přestaň sledovat"><#else><#assign monitorState="Sleduj blog">
</#if>

<#assign plovouci_sloupec>

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

    <#if (USER?exists && USER.id==BLOG.owner)>
        <div class="s_nadpis">
            Správa blogu
        </div>

        <div class="s_sekce">
            <ul>
                <li><a href="${URL.noPrefix("/blog/edit/"+REL_BLOG.id+"?action=add")}">Vlož nový zápis</a></li>
                <li><a href="${URL.noPrefix("/blog/edit/"+REL_BLOG.id+"?action=custom")}">Nastavit blog</a></li>
                <li><a href="${URL.noPrefix("/blog/edit/"+REL_BLOG.id+"?action=rename")}">Přejmenovat blog</a></li>
                <li><a href="${URL.noPrefix("/blog/edit/"+REL_BLOG.id+"?action=links")}">Upravit oblíbené stránky</a></li>
                <li><a href="${URL.noPrefix("/blog/"+BLOG.subType+"/export")}">Export do Movable Type</a></li>
            </ul>
        </div>
    </#if>

    <div class="s_nadpis">
        <@lib.showUser owner/>
    	<#if title!="UNDEF"> - <a href="/blog/${BLOG.subType}">${title}</a></#if>
    </div>

    <div class="s_sekce">
        <#if intro!="UNDEF">${intro}</#if>
    </div>

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

    <#if LAST_DESKTOP?exists>
        <div class="s_nadpis">
            <a class="info" href="#">?<span class="tooltip">Poslední screenshot mého desktopu.</span></a>
            Současný desktop
        </div>

        <div class="s_sekce" align="center">
            <#assign desktop_title=TOOL.xpath(LAST_DESKTOP.child,"/data/title")>
            <a href="${LAST_DESKTOP.url}" title="${desktop_title?html}" class="thumb">
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
	        <li><a href="/blog/souhrn">Stručný souhrn blogů</a></li>
	        <li><a href="/blog/vyber">Výběr z blogů</a></li>
	    <li>
                <a href="${URL.make("/EditMonitor/"+RELATION.id+"?action=toggle"+TOOL.ticket(USER?if_exists, false))}">${monitorState}</a>
                <span title="Počet lidí, kteří sledují tento blog">(${TOOL.getMonitorCount(BLOG.data)})</span>
                <a class="info" href="#">?<span class="tooltip">Zašle upozornění na váš email při vytvoření nového zápisku v tomto blogu.</span></a>
            </li>
        </ul>
    </div>

  <#--<@lib.advertisement id="arbo-sq" />-->
  <@lib.advertisement id="gg-sq-blog" />

</#assign>

<#include "../header.ftl">
<@lib.showMessages/>

<#if CATEGORY?exists>
    <h2>Zápisy v kategorii ${CATEGORY.name}</h2>
</#if>

<#if STORIES.total==0>
    <p>Vašemu výběru neodpovídá žádný zápis.</p>
</#if>

<#list STORIES.data as relation>
    <#assign story=relation.child, url=TOOL.getUrlForBlogStory(relation),
             category = story.subType?default("UNDEF"), tmp=TOOL.groupByType(story.children)>
    <#if category!="UNDEF">
        <#assign category_url=TOOL.xpath(BLOG, "//category[@id='"+category+"']/@url")?default("UNDEF"),
            category=TOOL.xpath(BLOG, "//category[@id='"+category+"']/@name")?default("UNDEF")>
    </#if>
    <div class="cl">
        <#if SUMMARY?exists>
            <h3 class="st_nadpis">
                <a href="${url}">${TOOL.xpath(story, "/data/name")}</a>
            </h3>
        <#else>
            <h2 class="st_nadpis">
                <a href="${url}">${TOOL.xpath(story, "/data/name")}</a>
            </h2>
        </#if>
        <p class="meta-vypis">
            ${DATE.show(story.created, "SMART")} |
            <#if (category!="UNDEF" && category?length > 1)>
                <#if category_url!="UNDEF">
                    <a href="/blog/${BLOG.subType+"/"+category_url}" title="Kategorie zápisu">${category}</a>
                <#else>
                    ${category}
                </#if>
            |</#if>
               Přečteno: ${TOOL.getCounterValue(story,"read")}&times;
            <#if tmp.discussion?exists>| <@lib.showCommentsInListing TOOL.analyzeDiscussion(tmp.discussion[0]), "SMART_DMY", "/blog" /></#if>
            <@lib.showShortRating relation, "| " />
        </p>
        <#if ! SUMMARY?exists>
            <#assign text = TOOL.xpath(story, "/data/perex")?default("UNDEF")>
            <#if text!="UNDEF">
                ${text}
                <div class="signature"><a href="${url}">více...</a></div>
            <#else>
                ${TOOL.xpath(story, "/data/content")}
            </#if>
        </#if>
    </div>
    <hr>
</#list>

<p>
    <#if SUMMARY?exists>
        <#assign url="/blog/"+BLOG.subType+"/souhrn">
    <#elseif CATEGORY?exists>
        <#assign url="/blog/"+BLOG.subType+"/"+CATEGORY>
    <#else>
        <#assign url="/blog/"+BLOG.subType+"/">
        <#if YEAR?exists><#assign url=url+YEAR+"/"></#if>
        <#if MONTH?exists><#assign url=url+MONTH+"/"></#if>
        <#if DAY?exists><#assign url=url+DAY+"/"></#if>
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
