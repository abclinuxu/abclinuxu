<#assign plovouci_sloupec>
    <p>
        P�ehled blog� za dan� obdob� pro v�echny u�ivatele.
        Chcete-li tak� ps�t sv�j blog, p�ihla�te se a v nastaven�
        si m��ete zalo�it sv�j blog.
    </p>
</#assign>

<#include "../header.ftl">

<#if STORIES.total==0>
    <p>Va�emu v�b�ru neodpov�d� ��dn� z�pis.</p>
</#if>

<#list STORIES.data as relation>
    <#assign story=relation.child, blog=relation.parent, author=TOOL.createUser(blog.owner)>
    <#assign url=TOOL.getUrlForBlogStory(blog.subType, story.created, relation.id)>
    <#assign category = story.subType?default("UNDEF")>
    <#if category!="UNDEF"><#assign category=TOOL.xpath(blog, "//category[@id='"+category+"']/@name")?default("UNDEF")></#if>
    <h1 class="st_nadpis"><a href="${url}">${TOOL.xpath(story, "/data/name")}</a></h1>
    <p class="cl_inforadek">
        ${DATE.show(story.created, "CZ_SHORT")} |
        <a href="/blog/${blog.subType}">${author.name}</a> |
        P�e�teno: ${TOOL.getCounterValue(story)}x |
        <#if category!="UNDEF">${category} |</#if>
        <@showDiscussions story, url/>
    </p>
    ${TOOL.xpath(story, "/data/content")}
</#list>

<p>
    <#assign url="/blog/"><#if YEAR?exists><#assign url=url+YEAR+"/"></#if>
    <#if MONTH?exists><#assign url=url+MONTH+"/"></#if><#if DAY?exists><#assign url=url+DAY+"/"></#if>
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
