<#include "../header.ftl">

<#assign autor=TOOL.createUser(TOOL.xpath(ITEM,"/data/author"))>
<#assign forbidRating=TOOL.xpath(ITEM, "//forbid_rating")?default("UNDEF")>
<#assign forbidDiscussion=TOOL.xpath(ITEM, "//forbid_discussions")?default("UNDEF")>

<h1 class="st_nadpis">${TOOL.xpath(ITEM,"/data/name")}</h1>

<p class="cl_inforadek">
${DATE.show(ITEM.created,"CZ_FULL")} | <a href="/Profile/${autor.id}">${autor.name}</a>
</p>

<#if USER?exists && USER.hasRole("article admin")>
 <p>
  <a href="${URL.make("/edit?action=edit&amp;rid="+RELATION.id)}">Upravit</a>
  <a href="${URL.noPrefix("/SelectRelation?prefix=/clanky&amp;url=/EditRelation&action=move&amp;rid="+RELATION.id)}">Pøesunout</a>
  <a href="${URL.noPrefix("/EditRelation?action=remove&amp;prefix=/clanky&amp;rid="+RELATION.id)}">Smazat</a>
  <a href="${URL.make("/honorare/"+RELATION.id+"?action=add")}">Vlo¾it honoráø</a>
  <#if CHILDREN.royalties?exists>
   <#list CHILDREN.royalties as honorar>
    <a href="${URL.make("/honorare/"+honorar.id+"?action=edit")}">Upravit honoráø</a>
   </#list>
  </#if>
  <a href="${URL.make("/"+RELATION.id+".docb")}">Docbook</a>
 </p>
</#if>

<#if ( PAGE?default(0) == 0) >
 <div class="cl_perex">${TOOL.xpath(ITEM,"/data/perex")}</div>
</#if>

${TOOL.render(TEXT,USER?if_exists)}

<#if PAGES?exists>
 <div class="cl_perex">
  <h1 class="st_nadpis">Jednotlivé podstránky èlánku</h1>
  <ol>
  <#list PAGES as page><li>
   <#if page_index==PAGE>
    ${page}
   <#else>
    <a href="/clanky/show/${RELATION.id}?page=${page_index}">${page}</a>
   </#if>
  </#list>
  </ol>
 </div>
</#if>

<#if RELATED?exists || RESOURCES?exists>
 <div class="cl_perex">
  <#if RELATED?exists>
   <h1 class="st_nadpis">Související èlánky</h1>
   <div class="st_linky">
    <#list RELATED as link>
     <a href="${link.url}">${link.title}</a> ${link.description?if_exists}<br>
    </#list>
   </div>
  </#if>
  <#if RESOURCES?exists>
  <h1 class="st_nadpis">Odkazy a zdroje</h1>
   <div class="st_linky">
    <#list RESOURCES as link>
     <a href="${link.url}">${link.title}</a> ${link.description?if_exists}<br>
    </#list>
   </div>
  </#if>
 </div>
</#if>

<p><b>Nástroje</b>: <a href="/clanky/show/${RELATION.id}?varianta=print">Tisk</a>,
<a href="/clanky/show/${RELATION.id}?varianta=print&amp;noDiz">Tisk bez diskuse</a>
</p>

<#if forbidRating!="yes">
    <#macro starLink rid rvalue rtype value>
    <a href="${URL.make("/rating/"+rid+"?action=rate&amp;rtype="+rtype+"&amp;rvalue="+rvalue)}" target="rating" title="Va¹e hodnocení: <#list 1..rvalue as x>*</#list>"><@lib.star value /></a>
    </#macro>
    <#assign rating=TOOL.ratingFor(ITEM.data,"article")?default(0)>

    <div class="cl_rating">
     <h1 class="st_nadpis">Hodnocení&nbsp;&nbsp;<iframe name="rating" width="300" frameborder="0" height="20" scrolling="no" class="rating"></iframe></h1>
     <div class="hdn">
     <div class="text">Stav: <#if rating!=0>${rating?string["#0.00"]} <#else>bez hodnocení</#if></div>
     <div class="tpm">
        <img src="/images/site2/teplomerrtut.gif" height="5" width="<#if rating!=0>${3+(rating/3)*191} <#else>3</#if>" title="${rating?string["#0.00"]}">
        <#if USER?exists>
            <div class="stup">
                <a class="s0" href="${URL.make("/rating/"+RELATION.id+"?action=rate&amp;rtype=article&amp;rvalue=0")}" target="rating" title="Va¹e hodnocení: 0">0</a>
                <a class="s1" href="${URL.make("/rating/"+RELATION.id+"?action=rate&amp;rtype=article&amp;rvalue=1")}" target="rating" title="Va¹e hodnocení: 1">1</a>
                <a class="s2" href="${URL.make("/rating/"+RELATION.id+"?action=rate&amp;rtype=article&amp;rvalue=2")}" target="rating" title="Va¹e hodnocení: 2">2</a>
                <a class="s3" href="${URL.make("/rating/"+RELATION.id+"?action=rate&amp;rtype=article&amp;rvalue=3")}" target="rating" title="Va¹e hodnocení: 3">3</a>
            </div>
        </#if>
     </div>
     <#if rating!=0>
        <div class="text">Poèet hlasù: ${TOOL.xpath(ITEM,"//rating[type/text()='article']/count")}</div>
     </#if>
     <br><br><div>&nbsp;</div>
     </div>

     </div>
</#if>
<#flush>

<#if CHILDREN.discussion?exists>
 <h1 class=st_nadpis">Diskuse k tomuto èlánku</h1>
 <#assign DISCUSSION=CHILDREN.discussion[0].child>

 <p class="monitor"><b>AbcMonitor</b> vám emailem za¹le upozornìní pøi zmìnì.
  <#if USER?exists && TOOL.xpath(DISCUSSION,"//monitor/id[text()='"+USER.id+"'")?exists>
   <#assign monitorState="Vypni">
  <#else>
   <#assign monitorState="Zapni">
  </#if>
  <a href="${URL.make("/EditDiscussion?action=monitor&amp;rid="+CHILDREN.discussion[0].id)}">${monitorState}</a>
  (${TOOL.getMonitorCount(DISCUSSION.data)})
 </p>

 <p>
  <a href="${URL.make("/EditDiscussion?action=add&amp;dizId="+DISCUSSION.id+"&amp;threadId=0&amp;rid="+CHILDREN.discussion[0].id)}">
  Vlo¾it dal¹í komentáø</a>
 </p>

 <#assign frozen=TOOL.xpath(DISCUSSION,"/data/frozen")?exists>
 <#if frozen>Diskuse byla administrátory uzamèena</#if>

 <#if USER?exists && USER.hasRole("discussion admin")>
  <a href="${URL.make("/EditDiscussion?action=freeze&amp;rid="+CHILDREN.discussion[0].id+"&amp;dizId="+DISCUSSION.id)}">
  <#if frozen>Rozmrazit<#else>Zmrazit</#if> diskusi</a>
 </#if>

 <#if USER?exists><#assign MAX_COMMENT=TOOL.getLastSeenComment(DISCUSSION,USER,true) in lib></#if>
 <#list TOOL.createDiscussionTree(DISCUSSION) as thread>
  <@lib.showThread thread, 0, DISCUSSION.id, CHILDREN.discussion[0].id, !frozen />
 </#list>
<#elseif forbidDiscussion!="yes">
 <h1 class="st_nadpis">Diskuse k tomuto èlánku</h1>
 <a href="${URL.make("/EditDiscussion?action=addDiz&amp;rid="+RELATION.id)}">Vlo¾it první komentáø</a>
</#if>

<#include "../footer.ftl">
