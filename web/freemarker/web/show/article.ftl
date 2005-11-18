<#include "../header.ftl">

<#assign autor=TOOL.createUser(TOOL.xpath(ITEM,"/data/author"))>
<#assign forbidRating=TOOL.xpath(ITEM, "//forbid_rating")?default("UNDEF")>
<#assign forbidDiscussion=TOOL.xpath(ITEM, "//forbid_discussions")?default("UNDEF")>

<h1>${TOOL.xpath(ITEM,"/data/name")}</h1>

<p class="cl_inforadek">
${DATE.show(ITEM.created,"CZ_FULL")} | <a href="/Profile/${autor.id}">${autor.name}</a>
</p>

<#if RELATION.upper==8082>
    <h2>Rubrika:
        <#assign section=TOOL.xpath(ITEM, "/data/section_rid")?default("UNDEFINED")>
        <#if section=="UNDEFINED">
            nezadána
        <#else>
            ${TOOL.childName(section)}
        </#if>
        </b>
    </h2>
</#if>

<#if USER?exists && USER.hasRole("article admin")>
 <p>
  <a href="${URL.make("/edit?action=edit&amp;rid="+RELATION.id)}">Upravit</a>
  <a href="${URL.make("/honorare/"+RELATION.id+"?action=add")}">Vlo¾it honoráø</a>
  <#if CHILDREN.royalties?exists>
   <#list CHILDREN.royalties as honorar>
    <a href="${URL.make("/honorare/"+honorar.id+"?action=edit")}">Upravit honoráø</a>
   </#list>
  </#if>
  <#if !CHILDREN.poll?exists>
      <a href="${URL.noPrefix("/EditPoll?action=add&amp;rid="+RELATION.id)}">Vytvoø anketu</a>
  </#if>
  <a href="${URL.noPrefix("/SelectRelation?prefix=/clanky&amp;url=/EditRelation&action=move&amp;rid="+RELATION.id)}">Pøesunout</a>
  <a href="${URL.noPrefix("/EditRelation?action=remove&amp;prefix=/clanky&amp;rid="+RELATION.id)}">Smazat</a>
  <a href="${URL.make("/"+RELATION.id+".docb")}">Docbook</a>
  <a href="${URL.make("/edit/"+RELATION.id+"?action=showTalk")}">Rozhovor</a>
 </p>
</#if>

<#if ( PAGE?default(0) == 0) >
 <div class="cl_perex">${TOOL.xpath(ITEM,"/data/perex")}</div>
</#if>

<div class="cl_square">
 <a href="mailto:reklama@stickfish.cz">reklama</a><br>
 <#include "/include/impact-cl-sq.txt">
</div>

${TOOL.render(TEXT,USER?if_exists)}

<#if CHILDREN.poll?exists>
    <h3>Anketa</h3>
    <@lib.showPoll CHILDREN.poll[0], RELATION.url?default("/clanky/show/"+RELATION.id) />
</#if>

<#if PAGES?exists>
 <div class="cl_perex">
  <h3>Jednotlivé podstránky èlánku</h3>
  <div class="s_sekce">
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
 </div>
</#if>

<div class="cl_perex">
  <#if RELATED?exists>
   <h3>Související èlánky</h3>
   <div class="s_sekce">
    <#list RELATED as link>
     <a href="${link.url}">${link.title}</a> ${link.description?if_exists}<br>
    </#list>
   </div>
  </#if>
  <#if RESOURCES?exists>
   <h3>Odkazy a zdroje</h3>
   <div class="s_sekce">
    <#list RESOURCES as link>
     <a href="${link.url}" rel="nofollow">${link.title}</a> ${link.description?if_exists}<br>
    </#list>
   </div>
  </#if>
  <#if SAME_SECTION_ARTICLES?exists>
   <h3>Dal¹í èlánky z této rubriky</h3>
    <div class="s_sekce">
     <#list SAME_SECTION_ARTICLES as relation>
       <a href="${relation.url?default("/clanky/show/"+relation.id)}">${TOOL.xpath(relation.child,"data/name")}</a><br>
     </#list>
    </div>
  </#if>
</div>

<p><b>Nástroje</b>: <a href="/clanky/show/${RELATION.id}?varianta=print">Tisk</a>,
<a href="/clanky/show/${RELATION.id}?varianta=print&amp;noDiz">Tisk bez diskuse</a>
</p>

<#if forbidRating!="yes">
    <#macro starLink rid rvalue rtype value>
    <a href="${URL.make("/rating/"+rid+"?action=rate&amp;rtype="+rtype+"&amp;rvalue="+rvalue)}" target="rating" title="Va¹e hodnocení: <#list 1..rvalue as x>*</#list>"><@lib.star value /></a>
    </#macro>
    <#assign rating=TOOL.ratingFor(ITEM.data,"article")?default("UNDEF")>

    <div class="cl_rating">
     <h3>Hodnocení&nbsp;&nbsp;<iframe name="rating" width="300" frameborder="0" height="20" scrolling="no" class="rating"></iframe></h3>
     <div class="hdn">
     <div class="text">Stav: <#if rating!="UNDEF">${rating.result?string["#0.00"]} <#else>bez hodnocení</#if></div>
     <div class="tpm">
        <img src="/images/site2/teplomerrtut.gif" alt="hodnoceni" height="5" width="<#if rating!="UNDEF">${3+(rating.result/3)*191} <#else>3</#if>" title="<#if rating!="UNDEF">${rating.result?string["#0.00"]}</#if>">
        <#if USER?exists>
            <div class="stup">
		<img id="spatny" src="/images/site2/palec_spatny.gif" alt="¹patné">
                <a class="s0" href="${URL.make("/rating/"+RELATION.id+"?action=rate&amp;rtype=article&amp;rvalue=0")}" target="rating" title="Va¹e hodnocení: 0">0</a>
                <a class="s1" href="${URL.make("/rating/"+RELATION.id+"?action=rate&amp;rtype=article&amp;rvalue=1")}" target="rating" title="Va¹e hodnocení: 1">1</a>
                <a class="s2" href="${URL.make("/rating/"+RELATION.id+"?action=rate&amp;rtype=article&amp;rvalue=2")}" target="rating" title="Va¹e hodnocení: 2">2</a>
                <a class="s3" href="${URL.make("/rating/"+RELATION.id+"?action=rate&amp;rtype=article&amp;rvalue=3")}" target="rating" title="Va¹e hodnocení: 3">3</a>
		<img id="dobry" src="/images/site2/palec_dobry.gif" alt="dobré">
            </div>
        </#if>
     </div>
     <#if rating!="UNDEF">
        <div class="text">Poèet hlasù: ${rating.count}</div>
     </#if>
     <br><br><div>&nbsp;</div>
     </div>

     </div>
</#if>
<#flush>

<#if CHILDREN.discussion?exists>
 <h1>Diskuse k tomuto èlánku</h1>
 <#assign DISCUSSION=CHILDREN.discussion[0].child>

 <p><b>AbcMonitor</b> vám emailem za¹le upozornìní pøi zmìnì.
  <#if USER?exists && TOOL.xpath(DISCUSSION,"//monitor/id[text()='"+USER.id+"']")?exists>
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

 <#assign diz = TOOL.createDiscussionTree(DISCUSSION,USER?if_exists,true)>
 <#if diz.hasUnreadComments><a href="#${diz.firstUnread}" title="Skoèit na první nepøeètený komentáø">První nepøeètený komentáø</a></#if>
 <#list diz.threads as thread>
  <@lib.showThread thread, 0, DISCUSSION.id, CHILDREN.discussion[0].id, !frozen />
 </#list>
<#elseif forbidDiscussion!="yes">
 <h1>Diskuse k tomuto èlánku</h1>
 <a href="${URL.make("/EditDiscussion?action=addDiz&amp;rid="+RELATION.id)}">Vlo¾it první komentáø</a>
</#if>

<#include "../footer.ftl">
