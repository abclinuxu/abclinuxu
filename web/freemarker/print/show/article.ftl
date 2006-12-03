<#include "../header.ftl">

<#assign autors=TOOL.createAuthorsForArticle(RELATION.getChild())>
<#assign forbidDiscussion=TOOL.xpath(ITEM, "//forbid_discussions")?default("UNDEF")>

<h1>${TOOL.xpath(ITEM,"/data/name")}</h1>

<div class="barva">
    ${DATE.show(ITEM.created,"CZ_FULL")} |
    <#list autors as autor>
        <a href="${autor.url}">${TOOL.childName(autor)}</a><#if autor_has_next>, </#if>
    </#list>
</div>

<#if PARENTS?exists>
    <#list TOOL.getParents(PARENTS,USER?if_exists,URL) as link>
        <a href="${link.url}">${link.text}</a>
        <#if link_has_next> - </#if>
    </#list>
</#if>&nbsp;

<#if USER?exists && USER.hasRole("article admin")>
 <p>
  <a href="${URL.make("/edit?action=edit&rid="+RELATION.id)}">Upravit</a>
  <a href="${URL.noPrefix("/SelectRelation?prefix=/clanky&url=/EditRelation&action=move&rid="+RELATION.id)}">P�esunout</a>
  <a href="${URL.noPrefix("/EditRelation?action=remove&prefix=/clanky&rid="+RELATION.id)}">Smazat</a>
  <a href="${URL.make("/honorare/"+RELATION.id+"?action=add")}">Vlo�it honor��</a>
  <#if CHILDREN.royalties?exists>
   <#list CHILDREN.royalties as honorar>
    <a href="${URL.make("/honorare/"+honorar.id+"?action=edit")}">Upravit honor��</a>
   </#list>
  </#if>
  <a href="${URL.make("/"+RELATION.id+".docb")}">Docbook</a>
 </p>
</#if>

<p class="perex">${TOOL.xpath(ITEM,"/data/perex")}</p>

${TOOL.render(TOOL.getCompleteArticleText(ITEM),USER?if_exists)}

<div class="cl_perex">
  <#if RELATED?exists>
   <h3>Souvisej�c� �l�nky</h3>
   <div class="s_sekce">
    <#list RELATED as link>
     <a href="${link.url}">${link.title}</a><br>
    </#list>
   </div>
  </#if>
  <#if RESOURCES?exists>
   <h3>Odkazy a zdroje</h3>
   <div class="s_sekce">
    <#list RESOURCES as link>
     <a href="${link.url}">${link.title}</a><br>
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

<#flush>

<#if ! PARAMS.noDiz?exists>
 <#if CHILDREN.discussion?exists>
  <h1>Diskuse k tomuto �l�nku</h1>
   <#assign diz = TOOL.createDiscussionTree(CHILDREN.discussion[0].child,USER?if_exists,CHILDREN.discussion[0].id,true)>
    <#list diz.threads as thread>
       <@lib.showThread thread, 0, diz, !diz.frozen />
    </#list>
 </#if>
</#if>

<#include "../footer.ftl">
