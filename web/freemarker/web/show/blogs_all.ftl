<#assign plovouci_sloupec>
  <div class="s_nad_h1"><div class="s_nad_pod_h1">
    <a class="info" href="#">?<span class="tooltip">Vlastní blog si po pøihlá¹ení mù¾ete zalo¾it v nastavení svého profilu.</span></a>
    <h1><a href="/blog">Blogy na AbcLinuxu</a></h1>
  </div></div>

    <div class="s_sekce">
        <#if DIGEST?exists>
            Výbìr zápiskù, které se týkají Linuxu, Open Source èi IT. ®ádná politika.
        <#else>
            Pøehled zápisù ze v¹ech blogù na¹ich u¾ivatelù. Blog si mù¾e zalo¾it registrovaný u¾ivatel
            ve svém profilu.
        </#if>
        <ul>
            <#if DIGEST?exists>
                <li>
                    <a href="/blog">V¹echny zápisky</a>
                </li>
            <#else>
                <li>
                    <#if SUMMARY?exists>
                        <a href="/blog">Výpis s perexy</a>
                    <#else>
                        <a href="/blog/souhrn">Struènìj¹í souhrn</a>
                    </#if>
                </li>
                <li>
                    <a href="/blog/vyber">Výbìr z blogù</a>
                </li>
            </#if>
            <li><a href="/blogy">Seznam blogù</a></li>
            <li>
                <#if DIGEST?exists>
                    <a href="/auto/blogDigest.rss">RSS kanál</a>
                <#else>
                    <a href="/auto/blog.rss">RSS kanál</a>
                </#if>
            </li>
        </ul>
    </div>
</#assign>

<#include "../header.ftl">

<#if STORIES.total==0>
    <p>Va¹emu výbìru neodpovídá ¾ádný zápis.</p>
</#if>

<#list STORIES.data as relation>
    <#assign story=relation.child, blog=relation.parent, author=TOOL.createUser(blog.owner),
             url=TOOL.getUrlForBlogStory(blog.subType, story.created, relation.id),
             title=TOOL.xpath(blog,"//custom/title")?default("blog"),
             category = story.subType?default("UNDEF"), rating=TOOL.ratingFor(story.data,"story")?default("UNDEF"),
             tmp=TOOL.groupByType(story.children)>
    <#if category!="UNDEF"><#assign category=TOOL.xpath(blog, "//category[@id='"+category+"']/@name")?default("UNDEF")></#if>
    <div class="cl">
        <#if SUMMARY?exists>
            <h3 class="st_nadpis">
                <a href="${url}">${TOOL.xpath(story, "/data/name")}</a>
            </h3>
        <#else>
            <h1 class="st_nadpis">
                <a href="${url}">${TOOL.xpath(story, "/data/name")}</a>
            </h1>
        </#if>
        <p class="cl_inforadek">
            ${DATE.show(story.created, "CZ_SHORT")} |
    	    <a href="/blog/${blog.subType}">${title}</a> |
    	    <a href="/Profile/${author.id}">${author.name}</a>
            <#if (category!="UNDEF" && category?length > 1)>| ${category}</#if>
            <#if SUMMARY?exists><br /><#else> | </#if>
	        Pøeèteno: ${TOOL.getCounterValue(story)}x
            <#if tmp.discussion?exists>| <@lib.showCommentsInListing TOOL.analyzeDiscussion(tmp.discussion[0]), "CZ_SHORT", "/blog" /></#if>
            <#if rating!="UNDEF">| Hodnocení:&nbsp;<span title="Hlasù: ${rating.count}">${rating.result?string["#0.00"]}</span></#if>
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
    <hr />
</#list>

<p>
    <#if SUMMARY?exists>
        <#assign url="/blog/souhrn">
    <#elseif DIGEST?exists>
        <#assign url="/blog/vyber">
    <#else>
        <#assign url="/blog/">
        <#if YEAR?exists><#assign url=url+YEAR+"/"></#if>
        <#if MONTH?exists><#assign url=url+MONTH+"/"></#if>
        <#if DAY?exists><#assign url=url+DAY+"/"></#if>
    </#if>
    <#if (STORIES.currentPage.row > 0) >
        <#assign start=STORIES.currentPage.row-STORIES.pageSize><#if (start<0)><#assign start=0></#if>
        <a href="${url}?from=${start}">Novìj¹í zápisy</a>
    </#if>
    <#assign start=STORIES.currentPage.row + STORIES.pageSize>
    <#if (start < STORIES.total) >
        <a href="${url}?from=${start}">Star¹í zápisy</a>
    </#if>
</p>

<#include "../footer.ftl">
