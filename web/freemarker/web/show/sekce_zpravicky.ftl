<#include "../header.ftl">

<#if USER?exists && USER.hasRole("category admin")>
 <p>
 <a href="${URL.make("/EditCategory?action=edit&rid="+RELATION.id+"&categoryId="+CATEGORY.id)}"
 title="Uprav kategorii"><img src="/images/actions/pencil.png" class="ikona22" ALT="Uprav sekci"></a>
 <a href="${URL.noPrefix("/EditRelation?action=remove&rid="+RELATION.id+"&prefix="+URL.prefix)}"
 title="Smaž kategorii"><img src="/images/actions/delete.png" ALT="Smaž sekci" class="ikona"></a>
</#if>
<#if USER?exists && USER.hasRole("move relation")>
 <a href="/SelectRelation?url=/EditRelation&action=move&rid=${RELATION.id}&prefix=${URL.prefix}"
 title="Přesunout"><img src="/images/actions/cut.png" ALT="Přesunout" class="ikona"></a>
 </p>
</#if>

<#macro showAdminNews(relation)>
 <#local
   ITEM=TOOL.sync(relation.child),
   autor=TOOL.createUser(ITEM.owner),
   title=TOOL.xpath(ITEM, "/data/title")?default("Zprávička"),
   locked = TOOL.xpath(ITEM, "//locked_by")?exists,
   approved = TOOL.xpath(ITEM, "//approved_by")?exists
 >
 <h3>${title}</h3>
 <p>
    ${TOOL.xpath(ITEM,"data/content")}
    <br>
    <#if approved><b></#if>${DATE.show(ITEM.created,"SMART")}<#if approved></b></#if> |
    ${NEWS_CATEGORIES[ITEM.subType].name} |
    <a href="/Profile/${autor.id}">${autor.name}</a>
    <br>
    <a href="${URL.make("/edit?action=mail&amp;rid="+relation.id)}">Poslat email autorovi</a>
    <#if locked>
        <#assign admin=TOOL.createUser(TOOL.xpath(ITEM, "//locked_by"))>
        Uzamknul <a href="/Profile/${admin.id}">${admin.name}</a> -
        <a href="${URL.make("/edit?action=unlock&amp;rid="+relation.id)}">odemknout</a>
    <#else>
        <a href="${URL.make("/show/"+relation.id)}">Zobrazit</a>
        <a href="${URL.make("/edit?action=edit&amp;rid="+relation.id)}">Upravit</a>
        <#if ! approved>
            <a href="${URL.make("/edit?action=approve&amp;rid="+relation.id)}">Schválit</a>
            <a href="${URL.make("/edit?action=lock&amp;rid="+relation.id)}">Zamknout</a>
        </#if>

        <a href="${URL.make("/edit?action=remove&amp;rid="+relation.id)}">Smazat</a>
    </#if>
 </p>
</#macro>

<#if USER?exists && USER.hasRole("news admin")>
 <#assign map=TOOL.groupByType(CHILDREN)>
 <#if map.news?exists>
  <#list SORT.byDate(map.news, "ASCENDING") as rel>
   <@showAdminNews rel />
   <hr>
  </#list>
  <br>
 </#if>
</#if>

<#include "../footer.ftl">
