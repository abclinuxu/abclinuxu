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
    <br>Autorem blogu je <a href="/Profile/${owner.id}">${owner.name}</a>
  </div>
  
    <div class="s_nad_h1"><div class="s_nad_pod_h1">
            <a class="info" href="#">?<span class="tooltip">P��stup k archivovan�m z�pis�m za jednotliv� m�s�ce.</span></a>
            <h1>Arch�v</h1>
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
        <a class="info" href="#">?<span class="tooltip">P��stup na osobn� hlavn� stranu a na hlavn� stranu v�ech blog�.</span></a>
        <h1>Navigace</h1>
  </div></div>
  
  <div class="s_sekce">
    <ul>
        <#if title!="UNDEF">
	    <li><a href="/blog/${BLOG.subType}">${title}, hlavn� strana</a></li>
        </#if>
        <li><a href="/blog">Blogy na AbcLinuxu</a></li>	
    </ul>
  </div>
  
    <#if (USER?exists && USER.id==BLOG.owner) || (! USER?exists)>
        <div class="s_nad_h1"><div class="s_nad_pod_h1">
            <a class="info" href="#">?<span class="tooltip">Tato sekce sdru�uje akce pro majitele blogu.</span></a>
            <h1>Nastaven�</h1>
        </div></div>
    </#if>
    
  <div class="s_sekce">
    <#if USER?exists>
        <#if USER.id==BLOG.owner>
            <ul>
                <li><a href="${URL.noPrefix("/blog/edit/"+REL_BLOG.id+"?action=add")}">Vlo� nov� z�pis</a></li>
                <li><a href="${URL.noPrefix("/blog/edit/"+STORY.id+"?action=edit")}">Uprav z�pis</a></li>
                <li><a href="${URL.noPrefix("/blog/edit/"+STORY.id+"?action=remove")}">Sma� z�pis</a></li>
                <li><a href="${URL.noPrefix("/blog/edit/"+REL_BLOG.id+"?action=custom")}">Uprav vzhled</a></li>
                <li><a href="${URL.noPrefix("/blog/edit/"+REL_BLOG.id+"?action=rename")}">P�ejmenovat blog</a></li>
            </ul>
        </#if>
    <#else>
        <a href="${URL.noPrefix("/Profile?action=login&amp;url="+REQUEST_URI)}">P�ihl�sit se</a>
    </#if>
  </div>
</#assign>

<#include "../header.ftl">

<#assign CHILDREN=TOOL.groupByType(STORY.child.children), category = STORY.child.subType?default("UNDEF")>
<#assign url = TOOL.getUrlForBlogStory(BLOG.subType, STORY.child.created, STORY.id)>
<#if category!="UNDEF"><#assign category=TOOL.xpath(BLOG, "//category[@id='"+category+"']/@name")?default("UNDEF")></#if>

<h2>${TOOL.xpath(STORY.child, "/data/name")}</h2>
<p class="cl_inforadek">
    ${DATE.show(STORY.child.created, "CZ_SHORT")} |
    P�e�teno: ${TOOL.getCounterValue(STORY.child)}x
    <#if category!="UNDEF">| ${category}</#if>
</p>

<#assign text = TOOL.xpath(STORY.child, "/data/perex")?default("UNDEF")>
<#if text!="UNDEF">${text}</#if>
${TOOL.xpath(STORY.child, "/data/content")}

<p><b>N�stroje</b>: <a href="${url}?varianta=print">Tisk</a></p>

<h2>Koment��e</h2>
<#if CHILDREN.discussion?exists>
    <#assign DISCUSSION=CHILDREN.discussion[0].child>

    <p class="monitor"><b>AbcMonitor</b> v�m emailem za�le upozorn�n� p�i zm�n�.
        <#if USER?exists && TOOL.xpath(DISCUSSION,"//monitor/id[text()='"+USER.id+"'")?exists>
            <#assign monitorState="Vypni">
        <#else>
            <#assign monitorState="Zapni">
        </#if>
        <a href="${URL.make("/EditDiscussion?action=monitor&amp;rid="+CHILDREN.discussion[0].id+"&amp;url="+url)}">${monitorState}</a>
        (${TOOL.getMonitorCount(DISCUSSION.data)})
    </p>

    <p>
        <a href="${URL.make("/EditDiscussion?action=add&amp;dizId="+DISCUSSION.id+"&amp;threadId=0&amp;rid="+CHILDREN.discussion[0].id+"&amp;url="+url)}">
        Vlo�it dal�� koment��</a>
    </p>

    <#if USER?exists><#assign MAX_COMMENT=TOOL.getLastSeenComment(DISCUSSION,USER,true) in lib></#if>
    <#list TOOL.createDiscussionTree(DISCUSSION) as thread>
        <@lib.showThread thread, 0, DISCUSSION.id, CHILDREN.discussion[0].id, true, "&amp;url="+url/>
    </#list>
<#else>
    <a href="${URL.make("/EditDiscussion?action=addDiz&amp;rid="+STORY.id+"&amp;url="+url)}">Vlo�it prvn� koment��</a>
</#if>


<#include "../footer.ftl">
