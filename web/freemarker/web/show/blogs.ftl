<#assign intro=TOOL.xpath(BLOG,"//custom/intro")?default("UNDEF")>
<#assign title=TOOL.xpath(BLOG,"//custom/title")?default("UNDEF")>
<#assign owner=TOOL.createUser(BLOG.owner)>

<#assign plovouci_sloupec>
    <#if title!="UNDEF"><h2 style="text-align: center">${title}</h2></#if>
    <#if intro!="UNDEF">${intro}</#if>
    <br>Autorem blogu je <a href="/Profile/${owner.id}">${owner.name}</a>

    <#if (USER?exists && USER.id==BLOG.owner) || (! USER?exists)>
        <div class="s_nad_h1"><div class="s_nad_pod_h1">
            <a class="info" href="#">?<span class="tooltip">Tato sekce sdru�uje akce pro majitele blogu.</span></a>
            <h1>Nastaven�</h1>
        </div></div>
    </#if>
    <#if USER?exists>
        <#if USER.id==BLOG.owner>
            <ul>
                <li><a href="${URL.noPrefix("/blog/edit/"+REL_BLOG.id+"?action=add")}">Vlo� nov� z�pis</a></li>
                <li><a href="${URL.noPrefix("/blog/edit/"+REL_BLOG.id+"?action=custom")}">Uprav vzhled</a></li>
                <li><a href="${URL.noPrefix("/blog/edit/"+REL_BLOG.id+"?action=rename")}">P�ejmenovat blog</a></li>
            </ul>                      
        </#if>
    <#else>
        Pro pr�ci s blogem se mus�te <a href="${URL.noPrefix("/Profile?action=login&amp;url="+REQUEST_URI)}">p�ihl�sit</a>.
    </#if>
</#assign>

<#include "../header.ftl">

<#list STORIES as relation>
    <#assign story=relation.child, tmp=TOOL.groupByType(story.children)>
    <#if tmp.discussion?exists><#assign diz=TOOL.analyzeDiscussion(tmp.discussion[0])></#if>
    <h2>${TOOL.xpath(story, "/data/name")}</h2>
    <p class="cl_inforadek">${DATE.show(story.created, "CZ_SHORT")} |
        P�e�teno: ${TOOL.getCounterValue(story)}x |
        <a href="${TOOL.getUrlForBlogStory(BLOG.subType, story.created, relation.id)}">Link</a>
    </p>
    ${TOOL.xpath(story, "/data/content")}
    <#if diz?exists>
        <p class="cl_inforadek">
            <a href="/clanky/show/${diz.relationId}">
            Koment���: ${diz.responseCount}</a
            ><#if diz.responseCount gt 0>, posledn� ${DATE.show(diz.updated, "CZ_SHORT")}</#if>
        </p>
    </#if>
</#list>

<#include "../footer.ftl">
