<#import "../macros.ftl" as lib>
<#assign intro=TOOL.xpath(BLOG,"//custom/intro")?default("UNDEF")>
<#assign title=TOOL.xpath(BLOG,"//custom/title")?default("UNDEF")>
<#assign owner=TOOL.createUser(BLOG.owner)>

<#assign plovouci_sloupec>

    <#if title!="UNDEF">
	<div class="s_nad_h1"><div class="s_nad_pod_h1">
            <h1><a href="/blog/${BLOG.subType}">${title}</a></h1>
	</div></div>
    </#if>

  <div class="s_sekce">
    <#if intro!="UNDEF">${intro}</#if>
  </div>

  <div class="s_nad_h1"><div class="s_nad_pod_h1">
        <a class="info" href="#">?<span class="tooltip">Pøístup k archivovaným zápisùm za jednotlivé mìsíce.</span></a>
        <h1>Archív</h1>
  </div></div>

  <div class="s_sekce">
    <#list BLOG_XML.data.archive.year as year>
        <ul>
        <#list year.month as month>
            <li>
                <a href="/blog/${BLOG.subType}/${year.@value}"><@lib.month month=month.@value/>${year.@value} (${month})</a>
            </li>
        </#list>
        </ul>
    </#list>
  </div>

  <div class="s_nad_h1"><div class="s_nad_pod_h1">
        <a class="info" href="#">?<span class="tooltip">Pøístup na osobní hlavní stranu a na hlavní stranu v¹ech blogù.</span></a>
        <h1>Navigace</h1>
  </div></div>

  <div class="s_sekce">
    <ul>
        <#if title!="UNDEF">
    	    <li><a href="/blog/${BLOG.subType}">${title}, hlavní strana</a></li>
        </#if>
        <li><a href="/Profile/${owner.id}">${owner.name}</a></li>
        <li><a href="/auto/blog/${BLOG.subType}.rss">RSS kanál</a></li>
        <li><a href="/blog">V¹echny blogy</a></li>
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
            <li><a href="${URL.noPrefix("/blog/edit/"+REL_BLOG.id+"?action=custom")}">Uprav vzhled</a></li>
            <li><a href="${URL.noPrefix("/blog/edit/"+REL_BLOG.id+"?action=rename")}">Pøejmenovat blog</a></li>
        </#if>
    <#else>
        <li><a href="${URL.noPrefix("/Profile?action=login&amp;url="+REQUEST_URI)}">Pøihlásit se</a></li>
    </#if>
    </ul>
  </div>
</#assign>

<#include "../header.ftl">

<#if STORIES.total==0>
    <p>Va¹emu výbìru neodpovídá ¾ádný zápis.</p>
</#if>

<#list STORIES.data as relation>
    <#assign story=relation.child, url=TOOL.getUrlForBlogStory(BLOG.subType, story.created, relation.id)>
    <#assign category = story.subType?default("UNDEF")>
    <#if category!="UNDEF"><#assign category=TOOL.xpath(BLOG, "//category[@id='"+category+"']/@name")?default("UNDEF")></#if>
    <div class="cl">
	<h1 class="st_nadpis"><a href="${url}">${TOOL.xpath(story, "/data/name")}</a></h1>
        <p class="cl_inforadek">
    	    ${DATE.show(story.created, "CZ_SHORT")} |
	    Pøeèteno: ${TOOL.getCounterValue(story)}x |
            <#if category!="UNDEF">${category} |</#if>
    	    <@showDiscussions story, url/>
	</p>
        <#assign text = TOOL.xpath(story, "/data/perex")?default("UNDEF")>
        <#if text!="UNDEF">
            ${text}
            <div class="signature"><a href="${url}">více...</a></div>
        <#else>
            ${TOOL.xpath(story, "/data/content")}
        </#if>
    </div>
    <hr>
</#list>

<p>
    <#assign url="/blog/"+BLOG.subType><#if YEAR?exists><#assign url=url+YEAR+"/"></#if>
    <#if MONTH?exists><#assign url=url+MONTH+"/"></#if><#if DAY?exists><#assign url=url+DAY+"/"></#if>
    <#if (STORIES.currentPage.row > 0) >
        <#assign start=STORIES.currentPage.row-STORIES.pageSize><#if (start<0)><#assign start=0></#if>
        <a href="${url}?from=${start}">Novìj¹í zápisy</a>
    </#if>
    <#assign start=STORIES.currentPage.row + STORIES.pageSize>
    <#if (start < STORIES.total) >
        <a href="${url}?from=${start}">Star¹í zápisy</a>
    </#if>
</p>

<#macro showDiscussions (story url)>
    <#local CHILDREN=TOOL.groupByType(story.children)>
    <#if CHILDREN.discussion?exists>
        <#local diz=TOOL.analyzeDiscussion(CHILDREN.discussion[0])>
    <#else>
        <#local diz=TOOL.analyzeDiscussion("UNDEF")>
    </#if>
    <a href="${url}">Komentáøù:</a> ${diz.responseCount}<#if diz.responseCount gt 0>, poslední ${DATE.show(diz.updated, "CZ_SHORT")}</#if>
</#macro>

<#include "../footer.ftl">
