<#include "../header.ftl">

<#assign autors=TOOL.createAuthorsForArticle(RELATION.getChild())>
<#assign forbidDiscussion=TOOL.xpath(ITEM, "//forbid_discussions")!"UNDEF">

<h1>${ITEM.title}</h1>

<div class="barva">
    ${DATE.show(ITEM.created,"SMART_DMY")} |
    <#list autors as autor>
        <a href="${autor.url}">${TOOL.childName(autor)}</a><#if autor_has_next>, </#if>
    </#list>
</div>

<#if PARENTS??>
    <#list TOOL.getParents(PARENTS,USER!,URL) as link>
        <a href="${link.url}">${link.title}</a>
        <#if link_has_next> - </#if>
    </#list>
</#if>&nbsp;

<#if USER?? && TOOL.permissionsFor(USER, RELATION).canModify()>
 <p>
  <a href="${URL.make("/edit?action=edit&rid="+RELATION.id)}">Upravit</a>
  <a href="${URL.noPrefix("/SelectRelation?prefix=/clanky&url=/EditRelation&action=move&rid="+RELATION.id)}">Přesunout</a>
  <#if TOOL.permissionsFor(USER, RELATION).canDelete()>
  	<a href="${URL.noPrefix("/EditRelation?action=remove&prefix=/clanky&rid="+RELATION.id)}">Smazat</a>
  </#if>
  <a href="${URL.make("/honorare/"+RELATION.id+"?action=add")}">Vložit honorář</a>
  <#if CHILDREN.royalties??>
   <#list CHILDREN.royalties as honorar>
    <a href="${URL.make("/honorare/"+honorar.id+"?action=edit")}">Upravit honorář</a>
   </#list>
  </#if>
  <a href="${URL.make("/"+RELATION.id+".docb")}">Docbook</a>
 </p>
</#if>

<p class="perex">${TOOL.xpath(ITEM,"/data/perex")}</p>

${TOOL.render(TOOL.getCompleteArticleText(ITEM),USER!)}

<#if SERIES??>
    <div>
        <h3>Seriál <a href="${SERIES.series.url}">${TOOL.childName(SERIES.series)}</a> (dílů: ${SERIES.total})</h3>
        První díl: <a href="${SERIES.first.url}">${TOOL.childName(SERIES.first)}</a><#rt>
        <#lt><#if (SERIES.total > 1)>, poslední díl: <a href="${SERIES.last.url}">${TOOL.childName(SERIES.last)}</a></#if>.<br>
        <#if SERIES.previous??>Předchozí díl: <a href="${SERIES.previous.url}">${TOOL.childName(SERIES.previous)}</a><br></#if>
        <#if SERIES.next??>Následující díl: <a href="${SERIES.next.url}">${TOOL.childName(SERIES.next)}</a><br></#if>
    </div>
</#if>

<div class="cl_perex">
  <#if RELATED??>
   <h3>Související články</h3>
   <div class="s_sekce">
    <#list RELATED as link>
     <a href="${link.url}">${link.title}</a><br>
    </#list>
   </div>
  </#if>
  <#if RESOURCES??>
   <h3>Odkazy a zdroje</h3>
   <div class="s_sekce">
    <#list RESOURCES as link>
     <a href="${link.url}">${link.title}</a><br>
    </#list>
   </div>
  </#if>
  <#if SAME_SECTION_ARTICLES??>
   <h3>Další články z této rubriky</h3>
    <div class="s_sekce">
     <#list SAME_SECTION_ARTICLES as relation>
       <a href="${relation.url?default("/clanky/show/"+relation.id)}">${relation.child.title}</a><br>
     </#list>
    </div>
  </#if>
</div>

<#flush>

<#if ! PARAMS.noDiz??>
 <#if CHILDREN.discussion??>
  <h1>Diskuse k tomuto článku</h1>
   <#assign diz = TOOL.createDiscussionTree(CHILDREN.discussion[0].child,USER!,CHILDREN.discussion[0].id,true)>
    <#list diz.threads as thread>
       <@lib.showThread thread, 0, diz, !diz.frozen />
    </#list>
 </#if>
</#if>

<#include "../footer.ftl">
