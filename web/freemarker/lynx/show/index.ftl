<#include "../header.ftl">

<#include "/include/zprava.txt">
<@lib.showMessages/>

Zkratka na <a href="#zpravicky">zprávièky</a>, <a href="#diskuse">diskusní fórum</a>

<#assign ARTICLES=VARS.getFreshArticles(USER?if_exists)>
<#list ARTICLES as rel>
 <@lib.showArticle rel, "CZ_SHORT" />
 <@lib.separator double=!rel_has_next />
</#list>

<p>
 <a href="/History?type=articles&from=${ARTICLES?size}&count=10" title="Dal¹í">Star¹í èlánky</a>
</p>

<#flush>

<h2><a href="/blog">Blogy na AbcLinuxu</a></h2>
  <ul>
  <#assign STORIES=VARS.getFreshStories(USER?if_exists)>
  <#list STORIES as relation>
     <li>
     <#assign story=relation.child, blog=relation.parent>
     <#assign url=TOOL.getUrlForBlogStory(blog.subType, story.created, relation.id)>
     <#assign title=TOOL.xpath(blog,"//custom/title")?default("UNDEF")>
     <#assign CHILDREN=TOOL.groupByType(story.children)>
     <#if CHILDREN.discussion?exists>
       <#assign diz=TOOL.analyzeDiscussion(CHILDREN.discussion[0])>
     <#else>
       <#assign diz=TOOL.analyzeDiscussion("UNDEF")>
     </#if>
     <a href="${url}">${TOOL.xpath(story, "/data/name")}<#if title!="UNDEF"> | ${title}</#if> | ${DATE.show(story.created, "CZ_DM")} | Komentáøù:&nbsp;${diz.responseCount}<#if diz.responseCount gt 0>, poslední&nbsp;${DATE.show(diz.updated, "CZ_SHORT")}</#if></a>
     </li>
  </#list>
  </ul>

<h2>Základy Linuxu</h2>
<ul>
 <li><a href="/clanky/show/26394">Co je to Linux?</a></li>
 <li><a href="/clanky/show/12707">Je opravdu zdarma?</a></li>
 <li><a href="/clanky/show/9503">Co jsou to distribuce?</a></li>
 <li><a href="/clanky/show/14665">Èím nahradím aplikaci X?</a></li>
 <li><a href="/clanky/show/20310">Rozcestník na¹ich seriálù</a></li>
</ul>

<h2><a href="/ovladace">Ovladaèe</a></h2>
 <#assign DRIVERS = VARS.getFreshDrivers(USER?if_exists)>
<ul>
 <#list DRIVERS as rel>
  <li><a href="${rel.url?default("/ovladace/show/"+rel.id)}">
  ${TOOL.xpath(rel.child,"data/name")}</a></li>
 </#list>
 <li><a href="/ovladace">&gt;&gt;</a></li>
</ul>

<h2><a href="/hardware">Hardware</a></h2>
 <#assign HARDWARE = VARS.getFreshHardware(USER?if_exists)>
<ul>
 <#list HARDWARE as rel>
  <li><a href="/hardware/show/${rel.id}">
  ${TOOL.xpath(rel.parent,"data/name")}</a></li>
 </#list>
 <li><a href="/History?type=hardware&from=0&count=25">&gt;&gt;</a></li>
 <#list SORT.byName(HARDWARE) as rel>
  <li><a href="/hardware/dir/${rel.id}">
  ${TOOL.childName(rel)}</a></li>
 </#list>
</ul>

<h2>Aktuální jádra</h2>
 <#include "/include/kernel.txt">

<#assign NEWS=VARS.getFreshNews(USER?if_exists)>
<a name="zpravicky"><h2>Zprávièky</h2></a>
<#list NEWS as rel>
 <@lib.showNews rel/>
 <#if rel_has_next><@lib.separator /></#if>
</#list>
<p>
 <a href="/History?type=news&from=${NEWS?size}&count=15" title="Dal¹í">Star¹í zprávièky</a>, 
 <a href="${URL.make("/news/EditItem?action=add")}">Vytvoøit zprávièku</a>
</p>

<#flush>

<#assign FORUM = VARS.getFreshQuestions(USER?if_exists)>
<#if (FORUM?size > 0)>
    <#assign FORUM=TOOL.analyzeDiscussions(FORUM)>
    <a name="diskuse"><h1>Diskusní fórum</h1></a>
    <p>
        <#list FORUM as diz>
            ${DATE.show(diz.updated,"CZ_SHORT")}, ${diz.responseCount} odp.,
            <#if TOOL.xpath(diz.discussion,"/data/frozen")?exists>
                <b>Z</b>,
            </#if>
            <#if TOOL.isQuestionSolved(diz.discussion.data)>
                <b>V</b>,
            </#if>
            <#if USER?exists && TOOL.xpath(diz.discussion,"//monitor/id[text()='"+USER.id+"']")?exists>
                <b>S</b>,
            </#if>
            <a href="/forum/show/${diz.relationId}">
                ${TOOL.limit(TOOL.xpath(diz.discussion,"data/title"),50," ..")}
            </a>
            <br>
        </#list>
    </p>

    <ul>
        <li><a href="/diskuse.jsp">Zobrazit diskusní fórum (polo¾it dotaz)</a></li>
        <li><a href="/History?type=discussions&from=${FORUM?size}&count=20">Zobrazit star¹í dotazy</a></li>
    </ul>
</#if>

<p>
<b>Z</b> - diskuse byla zmra¾ena,
<b>V</b> - diskuse byla vyøe¹ena,
<b>S</b> - diskuse sledujete monitorem.
</p>

<#include "../footer.ftl">
