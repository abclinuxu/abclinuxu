<#include "../header.ftl">

<#assign title=TOOL.xpath(ITEM, "/data/title")?default("Zpr�vi�ka")>
<h1 class="st_nadpis">${title}</h1>

<p>
 <#assign autor=TOOL.createUser(ITEM.owner)>
 <b>Autor:</b> <a href="/Profile/${autor.id}">${autor.name}</a><br>
 <#if CATEGORY?exists>
  <b>Kategorie:</b> ${CATEGORY.name}<br>
 </#if>
 <b>Datum:</b> ${DATE.show(ITEM.created,"CZ_FULL")}<br>
 <#if RELATION.upper=37672>
  <b>Stav:</b> �ek� na schv�len�
  <#if USER?exists && USER.id=RELATION.child.owner>
   - <a href="${URL.make("/edit?action=edit&amp;rid="+RELATION.id)}">Upravit</a>
  </#if>
  <br>
 </#if>
 <#if USER?exists && USER.hasRole("news admin")>
  <#if TOOL.xpath(ITEM, "//locked_by")?exists>
   <#assign admin=TOOL.createUser(TOOL.xpath(ITEM, "//locked_by"))>
   Uzamknul <a href="/Profile/${admin.id}">${admin.name}</a> -
   <a href="${URL.make("/edit?action=unlock&amp;rid="+RELATION.id)}">odemknout</a>
  <#else>
   <a href="${URL.make("/edit?action=edit&amp;rid="+RELATION.id)}">Upravit</a>
   <#if RELATION.upper=37672><a href="${URL.make("/edit?action=approve&amp;rid="+RELATION.id)}">Schv�lit</a></#if>
   <a href="${URL.make("/edit?action=remove&amp;rid="+RELATION.id)}">Smazat</a>
   <a href="${URL.make("/edit?action=mail&amp;rid="+RELATION.id)}">Poslat email autorovi</a>
   <a href="${URL.make("/edit?action=lock&amp;rid="+RELATION.id)}">Zamknout</a>
  </#if>
  <br>
 </#if>
</p>

<p class="zpravicka">${TOOL.xpath(ITEM,"data/content")}</p>

<p><b>N�stroje</b>: <a href="${RELATION.url?default("/zpravicky/show/"+RELATION.id)}?varianta=print">Tisk</a></p>

<h2>Koment��e</h2>
<#if CHILDREN.discussion?exists>
    <#assign DISCUSSION=CHILDREN.discussion[0].child>
    <#assign diz = TOOL.createDiscussionTree(DISCUSSION,USER?if_exists,true)>

    <#assign frozen=TOOL.xpath(DISCUSSION,"/data/frozen")?exists>
    <#if frozen>Diskuse byla administr�tory uzam�ena</#if>
    <#if USER?exists && USER.hasRole("discussion admin")>
        <a href="${URL.make("/EditDiscussion?action=freeze&amp;rid="+CHILDREN.discussion[0].id+"&amp;dizId="+DISCUSSION.id)}">
        <#if frozen>Rozmrazit<#else>Zmrazit</#if> diskusi</a>
    </#if>

    <p>
    <#if diz.hasUnreadComments>
        <a href="#${diz.firstUnread}" title="Sko�it na prvn� nep�e�ten� koment��">Prvn� nep�e�ten� koment��</a>
    </#if>

    <a href="${URL.make("/EditDiscussion?action=add&amp;dizId="+DISCUSSION.id+"&amp;threadId=0&amp;rid="+CHILDREN.discussion[0].id)}">
    Vlo�it dal�� koment��</a>

    <#if USER?exists && TOOL.xpath(DISCUSSION,"//monitor/id[text()='"+USER.id+"']")?exists>
        <#assign monitorState="P�esta� sledovat"><#else><#assign monitorState="Sleduj">
    </#if>
    <a href="${URL.make("/EditDiscussion?action=monitor&amp;rid="+CHILDREN.discussion[0].id)}"
    title="AbcMonitor za�le emailem zpr�vu, dojde-li v diskusi ke zm�n�">${monitorState}</a>
    <span title="Po�et lid�, kte�� sleduj� tuto diskusi">(${TOOL.getMonitorCount(DISCUSSION.data)})</span>
    </p>

    <#list diz.threads as thread>
        <@lib.showThread thread, 0, DISCUSSION.id, CHILDREN.discussion[0].id, !frozen />
    </#list>
<#else>
    <a href="${URL.make("/EditDiscussion?action=addDiz&amp;rid="+RELATION.id)}">Vlo�it prvn� koment��</a>
</#if>

<#include "../footer.ftl">
