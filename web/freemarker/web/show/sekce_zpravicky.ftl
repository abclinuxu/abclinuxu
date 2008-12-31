<#include "../header.ftl">

<#if USER?? && USER.hasRole("category admin")>
 <p>
 <a href="${URL.make("/EditCategory?action=edit&rid="+RELATION.id+"&categoryId="+CATEGORY.id)}"
 title="Uprav kategorii"><img src="/images/actions/pencil.png" class="ikona22" ALT="Uprav sekci"></a>
 <a href="${URL.noPrefix("/EditRelation?action=remove&rid="+RELATION.id+"&prefix="+URL.prefix)}"
 title="Smaž kategorii"><img src="/images/actions/delete.png" ALT="Smaž sekci" class="ikona"></a>
</#if>
<#if USER?? && USER.hasRole("move relation")>
 <a href="/SelectRelation?url=/EditRelation&action=move&rid=${RELATION.id}&prefix=${URL.prefix}"
 title="Přesunout"><img src="/images/actions/cut.png" ALT="Přesunout" class="ikona"></a>
 </p>
</#if>

<#macro showAdminNews(relation)>
 <#local
   ITEM=TOOL.sync(relation.child),
   autor=TOOL.createUser(ITEM.owner),
   title=ITEM.title,
   locked = TOOL.xpath(ITEM, "//locked_by")??,
   approved = TOOL.xpath(ITEM, "//approved_by")??
 >
 <h3>${title}</h3>
 <p>
    ${TOOL.xpath(ITEM,"data/content")}
    <br>
    <#if approved><b></#if>${DATE.show(ITEM.created,"SMART")}<#if approved></b></#if> |
    ${NEWS_CATEGORIES[ITEM.subType].name} |
    <@lib.showUser autor/>
    <br>
    <a href="${URL.make("/edit?action=mail&amp;rid="+relation.id)}">Poslat email autorovi</a>
    <#if locked>
        <#assign admin=TOOL.createUser(TOOL.xpath(ITEM, "//locked_by"))>
        Uzamknul <@lib.showUser admin/> -
        <a href="${URL.make("/edit?action=unlock&amp;rid="+relation.id+TOOL.ticket(USER!, false))}">odemknout</a>
    <#else>
        <a href="${URL.make("/show/"+relation.id)}">Zobrazit</a>
        <a href="${URL.make("/edit?action=edit&amp;rid="+relation.id)}">Upravit</a>
        <#if ! approved>
            <a href="${URL.make("/edit?action=approve&amp;rid="+relation.id+TOOL.ticket(USER!, false))}">Schválit</a>
            <a href="${URL.make("/edit?action=lock&amp;rid="+relation.id+TOOL.ticket(USER!, false))}">Zamknout</a>
        </#if>

        <a href="${URL.make("/edit?action=remove&amp;rid="+relation.id)}">Smazat</a>
    </#if>
 </p>
</#macro>

<#if USER?? && USER.hasRole("news admin")>
    <form action="/zpravicky/hledani" method="POST">
        <input type="text" name="dotaz" size="30" tabindex="1">
        <input type="submit" class="button" value="Prohledej zprávičky" tabindex="2">
        <input type="hidden" name="parent" value="42932">
        <input type="hidden" name="type" value="zpravicka">
        <input type="hidden" name="orderBy" value="create">
        <input type="hidden" name="orderDir" value="desc">
    </form>

    <#assign map=TOOL.groupByType(CHILDREN)>
    <#if map.news??>
        <#list SORT.byDate(map.news, "ASCENDING") as rel>
            <@showAdminNews rel />
            <hr>
        </#list>
        <br>
    </#if>
</#if>

<#include "../footer.ftl">
