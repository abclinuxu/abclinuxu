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
            nezad�na
        <#else>
            ${TOOL.childName(section)}
        </#if>
        </b>
    </h2>
</#if>

<#if USER?exists && USER.hasRole("article admin")>
 <p>
  <a href="${URL.make("/edit?action=edit&amp;rid="+RELATION.id)}">Upravit</a>
  <a href="${URL.make("/honorare/"+RELATION.id+"?action=add")}">Vlo�it honor��</a>
  <#if CHILDREN.royalties?exists>
   <#list CHILDREN.royalties as honorar>
    <a href="${URL.make("/honorare/"+honorar.id+"?action=edit")}">Upravit honor��</a>
   </#list>
  </#if>
  <#if !CHILDREN.poll?exists>
      <a href="${URL.noPrefix("/EditPoll?action=add&amp;rid="+RELATION.id)}">Vytvo� anketu</a>
  </#if>
  <a href="${URL.noPrefix("/SelectRelation?prefix=/clanky&amp;url=/EditRelation&action=move&amp;rid="+RELATION.id)}">P�esunout</a>
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
 <#include "/include/impact-cl-sq.txt">
</div>

${TOOL.render(TEXT,USER?if_exists)}

<#if CHILDREN.poll?exists>
    <h3>Anketa</h3>
    <@lib.showPoll CHILDREN.poll[0], RELATION.url?default("/clanky/show/"+RELATION.id) />
</#if>

<#if PAGES?exists>
 <div class="cl_perex">
  <h3>Jednotliv� podstr�nky �l�nku</h3>
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

<#if forbidRating!="yes">
    <@lib.showRating RELATION/>
</#if>

<div class="cl_perex">
  <#if RELATED?exists>
   <h3>Souvisej�c� �l�nky</h3>
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
   <h3>Dal�� �l�nky z t�to rubriky</h3>
    <div class="s_sekce">
     <#list SAME_SECTION_ARTICLES as relation>
       <a href="${relation.url?default("/clanky/show/"+relation.id)}">${TOOL.xpath(relation.child,"data/name")}</a><br>
     </#list>
    </div>
  </#if>
</div>

<p><b>N�stroje</b>: <a href="/clanky/show/${RELATION.id}?varianta=print">Tisk</a>,
<a href="/clanky/show/${RELATION.id}?varianta=print&amp;noDiz">Tisk bez diskuse</a>
</p>

<#flush>

<#include "/include/sun-clanky.txt">

<#if CHILDREN.discussion?exists>
    <h3>Koment��e</h3>
    <@lib.showDiscussion CHILDREN.discussion[0]/>
<#elseif forbidDiscussion!="yes">
    <h1>Diskuse k tomuto �l�nku</h1>
    <a href="${URL.make("/EditDiscussion?action=addDiz&amp;rid="+RELATION.id)}">Vlo�it prvn� koment��</a>
</#if>

<#include "../footer.ftl">
