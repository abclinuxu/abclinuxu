<#include "../header.ftl">

<@lib.showMessages/>

Zkratka na <a href="#zpravicky">zprávičky</a>, <a href="#diskuse">diskusní fórum</a>

<#assign ARTICLES=VARS.getFreshArticles(USER!)>
<#list ARTICLES as rel>
 <@lib.showArticle rel, "CZ_SHORT" />
 <@lib.separator double=!rel_has_next />
</#list>

<p>
 <a href="/History?type=articles&from=${ARTICLES?size}&count=10" title="Další">Starší články</a>
</p>

<#assign NEWS=VARS.getFreshNews(USER!)>
<a name="zpravicky"><h2>Zprávičky</h2></a>
<#list NEWS as rel>
 <@lib.showNews rel/>
 <#if rel_has_next><@lib.separator /></#if>
</#list>
<p>
 <a href="/History?type=news&from=${NEWS?size}&count=15" title="Další">Starší zprávičky</a>,
 <a href="${URL.make("/news/EditItem?action=add")}">Vytvořit zprávičku</a>
</p>

<h3><a href="/ovladace">Ovladače</a></h3>
 <#assign DRIVERS = VARS.getFreshDrivers(USER!)>
<ul>
    <#list DRIVERS as rel>
         <li>
            <a href="${rel.url}">${rel.child.title}</a>
         </li>
    </#list>
 <li><a href="/ovladace">&gt;&gt;</a></li>
</ul>

<h3><a href="/hardware">Hardware</a></h3>
 <#assign HARDWARE = VARS.getFreshHardware(USER!)>
<ul>
    <#list HARDWARE as rel>
         <li><a href="${rel.url!"/hardware/show/"+rel.id}">${rel.child.title}</a></li>
    </#list>
 <li><a href="/History?type=hardware&from=0&count=25">&gt;&gt;</a></li>
</ul>

<h3><a href="/software">Software</a></h3>
<#assign SOFTWARE = VARS.getFreshSoftware(USER!)>
<ul>
    <#list SOFTWARE as rel>
         <li><a href="${rel.url}">${rel.child.title}</a></li>
    </#list>
 <li><a href="/History?type=software&from=0&count=25">&gt;&gt;</a></li>
</ul>

<#assign FORUM = VARS.getFreshQuestions(USER!)>
<#if (FORUM?size > 0)>
    <#assign FORUM=TOOL.analyzeDiscussions(FORUM)>
    <a name="diskuse"><h1>Diskusní fórum</h1></a>
    <p>
        <#list FORUM as diz>
            <a href="/forum/show/${diz.relationId}">
                ${TOOL.limit(diz.discussion.title,50," ..")}
            </a>
            ${DATE.show(diz.updated,"CZ_SHORT")}, ${diz.responseCount}<@lib.markNewComments diz/> odp.,
            <#if TOOL.xpath(diz.discussion,"/data/frozen")??>
                <b>Z</b>,
            </#if>
            <#if TOOL.isQuestionSolved(diz.discussion)>
                <b>V</b>,
            </#if>
            <#if USER?? && TOOL.isMonitored(diz.discussion, USER!)>
                <b>S</b>
            </#if>
            <br>
        </#list>
    </p>

    <ul>
        <li><a href="/diskuse.jsp">Zobrazit diskusní fórum (položit dotaz)</a></li>
        <li><a href="/History?type=discussions&from=${FORUM?size}&count=20">Zobrazit starší dotazy</a></li>
    </ul>
</#if>

<p>
<b>Z</b> - diskuse byla zmražena,
<b>V</b> - diskuse byla vyřešena,
<b>S</b> - diskusi sledujete monitorem.
</p>

<h2><a href="/blog">Blogy na AbcLinuxu</a></h2>
  <ul>
  <#assign STORIES=VARS.getFreshStories(USER!)>
  <#list STORIES as relation>
     <li>
     <#assign story=relation.child, blog=relation.parent>
     <#assign url=TOOL.getUrlForBlogStory(relation)>
     <#assign title=blog.title!"UNDEF">
     <#assign CHILDREN=TOOL.groupByType(story.children)>
     <#if CHILDREN.discussion??>
       <#assign diz=TOOL.analyzeDiscussion(CHILDREN.discussion[0])>
     <#else>
       <#assign diz=TOOL.analyzeDiscussion("UNDEF")>
     </#if>
     <a href="${url}">${story.title}<#if title!="UNDEF"> | ${title}</#if></a> |
     Komentářů:&nbsp;${diz.responseCount}
     <#if diz.responseCount gt 0><@lib.markNewComments diz/>, poslední&nbsp;${DATE.show(diz.updated, "CZ_SHORT")}</#if>
     </li>
  </#list>
  </ul>

<#include "../footer.ftl">
