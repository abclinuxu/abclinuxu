<#import "../ads-macro.ftl" as lib>

<#assign plovouci_sloupec>
  <div class="s_nadpis">
    <a class="info" href="#">?<span class="tooltip">Vlastní blog si po přihlášení můžete založit v nastavení svého profilu.</span></a>
    <a href="/blog">Blogy na AbcLinuxu</a>
  </div>

    <div class="s_sekce">
        <#if DIGEST??>
            Výběr zápisků, které se týkají Linuxu, Open Source či IT. Žádná politika.
        <#else>
            Přehled zápisů ze všech blogů našich uživatelů. Blog si může založit registrovaný uživatel
            ve svém profilu.
        </#if>
        <ul>
            <#if DIGEST??>
                <li>
                    <a href="/blog">Všechny zápisky</a>
                </li>
            <#else>
                <li>
                    <#if SUMMARY??>
                        <a href="/blog">Výpis s perexy</a>
                    <#else>
                        <a href="/blog/souhrn">Stručnější souhrn</a>
                    </#if>
                </li>
                <li>
                    <a href="/blog/vyber">Výběr z blogů</a>
                </li>
            </#if>
            <li><a href="/blogy">Seznam blogů</a></li>
            <li>
                <#if DIGEST??>
                    <a href="/auto/blogDigest.rss">RSS kanál</a>
                <#else>
                    <a href="/auto/blog.rss">RSS kanál</a>
                </#if>
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

    <#--<@lib.advertisement id="arbo-sq" />-->
    <@lib.advertisement id="gg-sq-blog" />

</#assign>

<#include "../header.ftl">

<#if STORIES.total==0>
    <p>Vašemu výběru neodpovídá žádný zápis.</p>
</#if>

<#list STORIES.data as relation>
    <#assign story=relation.child, blog=relation.parent, author=TOOL.createUser(blog.owner),
             url=TOOL.getUrlForBlogStory(relation),
             title=blog.title?default("blog"),
             category = story.subType?default("UNDEF"), tmp=TOOL.groupByType(story.children)>
    <#if category!="UNDEF"><#assign category=TOOL.xpath(blog, "//category[@id='"+category+"']/@name")?default("UNDEF")></#if>
    <div class="cl">
        <#if SUMMARY??>
            <h3 class="st_nadpis">
                <a href="${url}">${story.title}</a>
            </h3>
        <#else>
            <h2 class="st_nadpis">
                <a href="${url}">${story.title}</a>
            </h2>
        </#if>
        <p class="meta-vypis">
            ${DATE.show(story.created, "SMART")} |
            <a href="/blog/${blog.subType}">${title}</a> |
            <@lib.showUser author/>
            <#if (category!="UNDEF" && category?length > 1)>| ${category}</#if>
            <#if SUMMARY??><br /><#else> | </#if>
               Přečteno: ${TOOL.getCounterValue(story,"read")}&times;
            <#if tmp.discussion??>| <@lib.showCommentsInListing TOOL.analyzeDiscussion(tmp.discussion[0]), "SMART_DMY", "/blog" /></#if>
            <@lib.showShortRating relation, "| " />
        </p>
        <#if ! SUMMARY??>
            <#assign text = TOOL.xpath(story, "/data/perex")?default("UNDEF"), showmore=false>
            <#if text!="UNDEF">
                ${text}
                <#assign showmore=true>
            <#else>
                <#if TOOL.groupByType(story.children).poll?? || TOOL.screenshotsFor(story)?size gt 0><#assign showmore=true></#if>
                ${TOOL.xpath(story, "/data/content")}
            </#if>
            <#if showmore>
                <div class="signature"><a href="${url}">více...</a></div>
            </#if>
        </#if>
    </div>
    <hr />
</#list>

<p>
    <#if SUMMARY??>
        <#assign url="/blog/souhrn">
    <#elseif DIGEST??>
        <#assign url="/blog/vyber">
    <#else>
        <#assign url="/blog/">
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
