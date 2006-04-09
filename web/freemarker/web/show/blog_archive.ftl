<#import "../macros.ftl" as lib>
<#assign intro=TOOL.xpath(BLOG,"//custom/intro")?default("UNDEF")>
<#assign title=TOOL.xpath(BLOG,"//custom/title")?default("UNDEF")>
<#assign owner=TOOL.createUser(BLOG.owner)>

<#assign plovouci_sloupec>

    <div class="s_nad_h1"><div class="s_nad_pod_h1">
        <h1><a href="/Profile/${owner.id}">${owner.nick?default(owner.name)}</a>
	<#if title!="UNDEF"> - <a href="/blog/${BLOG.subType}">${title}</a></#if>
	</h1>
    </div></div>

    <div class="s_sekce">
        <#if intro!="UNDEF">${intro}</#if>
    </div>

    <#if UNPUBLISHED_STORIES?exists>
        <div class="s_nad_h1"><div class="s_nad_pod_h1">
            <h1>Rozepsané zápisy</h1>
        </div></div>

        <div class="s_sekce">
            <ul>
            <#list UNPUBLISHED_STORIES as relation>
                <#assign story=relation.child, url=TOOL.getUrlForBlogStory(BLOG.subType, story.created, relation.id)>
                <li>
                    <a href="${url}">${TOOL.xpath(story, "/data/name")}</a>
                </li>
            </#list>
            </ul>
        </div>
    </#if>

    <div class="s_nad_h1"><div class="s_nad_pod_h1">
        <h1>Aktuální zápisy</h1>
    </div></div>

    <div class="s_sekce">
        <ul>
        <#list CURRENT_STORIES as relation>
            <#assign story=relation.child, url=TOOL.getUrlForBlogStory(BLOG.subType, story.created, relation.id)>
            <li>
                <a href="${url}">${TOOL.xpath(story, "/data/name")}</a>
            </li>
        </#list>
        </ul>
    </div>

    <#assign links = BLOG_XML.data.custom.links.link>
    <#if links?size!=0>
        <div class="s_nad_h1"><div class="s_nad_pod_h1">
            <a class="info" href="#">?<span class="tooltip">Seznam mých oblíbených stránek, které pravidelnì nav¹tìvuji.</span></a>
            <h1>Oblíbené stránky</h1>
        </div></div>

        <div class="s_sekce">
            <ul>
            <#list links as link>
                <li><a href="${link}" rel="nofollow">${link.@caption}</a></li>
            </#list>
            </ul>
        </div>
    </#if>

    <div class="s_nad_h1"><div class="s_nad_pod_h1">
        <a class="info" href="#">?<span class="tooltip">Pøístup na osobní hlavní stranu a na hlavní stranu v¹ech blogù.</span></a>
        <h1>Navigace</h1>
    </div></div>

    <div class="s_sekce">
        <ul>
            <#if title!="UNDEF">
                <li><a href="/blog/${BLOG.subType}">${title}, hlavní strana</a></li>
            </#if>
            <li><a href="/blog/${BLOG.subType}/souhrn"><#if title!="UNDEF">${title}, </#if>struèný souhrn</a></li>
            <li><a href="/auto/blog/${BLOG.subType}.rss">RSS kanál</a></li>
            <li><a href="/blog">V¹echny blogy</a></li>
	    <li><a href="/blog/souhrn">V¹echny blogy, struèný souhrn</a></li>
        </ul>
    </div>


    <#if (USER?exists && USER.id==BLOG.owner) || (! USER?exists)>
        <div class="s_nad_h1"><div class="s_nad_pod_h1">
            <a class="info" href="#">?<span class="tooltip">Tato sekce sdru¾uje akce pro majitele blogu.</span></a>
            <h1>Nastavení</h1>
        </div></div>
    </#if>

  <div class="s_sekce">
    <ul>
    <#if USER?exists>
        <#if USER.id==BLOG.owner>
            <li><a href="${URL.noPrefix("/blog/edit/"+REL_BLOG.id+"?action=add")}">Vlo¾ nový zápis</a></li>
            <li><a href="${URL.noPrefix("/blog/edit/"+REL_BLOG.id+"?action=custom")}">Nastavit blog</a></li>
            <li><a href="${URL.noPrefix("/blog/edit/"+REL_BLOG.id+"?action=rename")}">Pøejmenovat blog</a></li>
            <li><a href="${URL.noPrefix("/blog/edit/"+REL_BLOG.id+"?action=categories")}">Upravit kategorie</a></li>
            <li><a href="${URL.noPrefix("/blog/edit/"+REL_BLOG.id+"?action=links")}">Upravit oblíbené stránky</a></li>
        </#if>
    <#else>
        <li><a href="${URL.noPrefix("/Profile?action=login&amp;url="+REQUEST_URI)}">Pøihlásit se</a></li>
    </#if>
    </ul>
  </div>
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
