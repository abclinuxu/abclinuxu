<#assign plovouci_sloupec>
  <div class="s_nad_h1"><div class="s_nad_pod_h1">
    <a class="info" href="#">?<span class="tooltip">Vlastn� blog si po p�ihl�en� m��ete zalo�it v nastaven� sv�ho profilu.</span></a>
    <h1><a href="/blog">Blogy na AbcLinuxu</a></h1>
  </div></div>

    <div class="s_sekce">
        P�ehled z�pis� ze v�ech blog� na�ich u�ivatel�. Blog si m��e zalo�it registrovan� u�ivatel
        ve sv�m profilu.
        <ul>
          <li>
	    <#if SUMMARY?exists>
	      <a href="/blog">V�pis s perexy</a>
	    <#else>
	      <a href="/blog/souhrn">Stru�n�j�� souhrn</a>
	    </#if>
	  </li>
	  <li><a href="/blogy">Seznam blog�</a></li>
          <li><a href="/auto/blog.rss">RSS kan�l</a></li>
        </ul>
    </div>
</#assign>

<#include "../header.ftl">

<#if STORIES.total==0>
    <p>Va�emu v�b�ru neodpov�d� ��dn� z�pis.</p>
</#if>

<#list STORIES.data as relation>
    <#assign story=relation.child, blog=relation.parent, author=TOOL.createUser(blog.owner)>
    <#assign url=TOOL.getUrlForBlogStory(blog.subType, story.created, relation.id)>
    <#assign title=TOOL.xpath(blog,"//custom/title")?default("blog")>
    <#assign category = story.subType?default("UNDEF"), rating=TOOL.ratingFor(story.data,"story")?default("UNDEF")>
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
	        P�e�teno: ${TOOL.getCounterValue(story)}x |
    	    <@showDiscussions story, url/>
            <#if rating!="UNDEF">| Hodnocen�:&nbsp;<span title="Hlas�: ${rating.count}">${rating.result?string["#0.00"]}</span></#if>
    	  </p>
	  <#if ! SUMMARY?exists>
            <#assign text = TOOL.xpath(story, "/data/perex")?default("UNDEF")>
            <#if text!="UNDEF">
                ${text}
                <div class="signature"><a href="${url}">v�ce...</a></div>
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
    <#else>
        <#assign url="/blog/">
        <#if YEAR?exists><#assign url=url+YEAR+"/"></#if>
        <#if MONTH?exists><#assign url=url+MONTH+"/"></#if>
        <#if DAY?exists><#assign url=url+DAY+"/"></#if>
    </#if>
    <#if (STORIES.currentPage.row > 0) >
        <#assign start=STORIES.currentPage.row-STORIES.pageSize><#if (start<0)><#assign start=0></#if>
        <a href="${url}?from=${start}">Nov�j�� z�pisy</a>
    </#if>
    <#assign start=STORIES.currentPage.row + STORIES.pageSize>
    <#if (start < STORIES.total) >
        <a href="${url}?from=${start}">Star�� z�pisy</a>
    </#if>
</p>

<#macro showDiscussions (story url)>
    <#local CHILDREN=TOOL.groupByType(story.children)>
    <#if CHILDREN.discussion?exists>
        <#local diz=TOOL.analyzeDiscussion(CHILDREN.discussion[0])>
    <#else>
        <#local diz=TOOL.analyzeDiscussion("UNDEF")>
    </#if>
    <a href="${url}">Koment���:</a> ${diz.responseCount}<#if diz.responseCount gt 0>, posledn� ${DATE.show(diz.updated, "CZ_SHORT")}</#if>
</#macro>

<#include "../footer.ftl">
