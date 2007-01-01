<#include "../header.ftl">

<#assign autors=TOOL.createAuthorsForArticle(RELATION.getChild())>
<#assign forbidRating=TOOL.xpath(ITEM, "//forbid_rating")?default("UNDEF")>
<#assign forbidDiscussion=TOOL.xpath(ITEM, "//forbid_discussions")?default("UNDEF")>

<h1>${TOOL.xpath(ITEM,"/data/name")}</h1>

<p class="cl_inforadek">
    ${DATE.show(ITEM.created,"CZ_FULL")} |
    <#list autors as autor>
        <a href="${autor.url}">${TOOL.childName(autor)}</a><#if autor_has_next>, </#if>
    </#list>
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
 <a href="mailto:info@stickfish.cz">reklama</a><br>
 <@lib.advertisement id="square" />
</div>

${TOOL.render(TEXT,USER?if_exists)}

<#if forbidRating!="yes">
    <@lib.showRating RELATION/>
</#if>

<#if CHILDREN.poll?exists>
    <h3>Anketa</h3>
    <div class="anketa">
      <@lib.showPoll CHILDREN.poll[0], RELATION.url?default("/clanky/show/"+RELATION.id) />
    </div>
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

<div class="cl_perex souvisejici">
  <#if RELATED?exists>
   <h3>Související èlánky</h3>
    <#list RELATED as link>
     <a href="${link.url}">${link.title}</a> ${link.description?if_exists}<br>
    </#list>
  </#if>
  <#if RESOURCES?exists>
   <h3>Odkazy a zdroje</h3>
    <#list RESOURCES as link>
     <a href="${link.url}" rel="nofollow">${link.title}</a> ${link.description?if_exists}<br>
    </#list>
  </#if>
  <#if SAME_SECTION_ARTICLES?exists>
   <h3>Dal¹í èlánky z této rubriky</h3>
     <#list SAME_SECTION_ARTICLES as relation>
       <a href="${relation.url?default("/clanky/show/"+relation.id)}">${TOOL.xpath(relation.child,"data/name")}</a><br>
     </#list>
  </#if>
</div>

<p><b>Nástroje</b>: <a rel="nofollow" href="/clanky/show/${RELATION.id}?varianta=print">Tisk</a>,
<a rel="nofollow" href="/clanky/show/${RELATION.id}?varianta=print&amp;noDiz">Tisk bez diskuse</a>
</p>

<#flush>

<#if CHILDREN.discussion?exists>
    <h3>Komentáøe</h3>
    <@lib.showDiscussion CHILDREN.discussion[0]/>
<#elseif forbidDiscussion!="yes">
    <h3>Diskuse k tomuto èlánku</h3>
    <a href="${URL.make("/EditDiscussion?action=addDiz&amp;rid="+RELATION.id)}">Vlo¾it první komentáø</a>
</#if>

<#include "../footer.ftl">
