<#include "../header.ftl">

<@lib.showMessages/>

<#if USER??>
 <p>
 <#if TOOL.permissionsFor(USER, RELATION).canCreate()>
     <a href="${URL.make("/EditCategory?action=add&rid="+RELATION.id)}"
     title="Vytvoř podkategorii"><img src="/images/actions/attach.png" ALT="Přidej článek" class="ikona22"></a>
 </#if>
 <#if TOOL.permissionsFor(USER, RELATION).canModify()>
     <a href="${URL.make("/EditCategory?action=edit&rid="+RELATION.id+"&categoryId="+CATEGORY.id)}"
     title="Uprav kategorii"><img src="/images/actions/pencil.png" class="ikona22" ALT="Uprav sekci"></a>
 </#if>
 <#if TOOL.permissionsFor(USER, RELATION.upper).canModify()>
 <a href="${URL.noPrefix("/EditRelation?action=remove&rid="+RELATION.id+"&prefix="+URL.prefix)}"
 title="Smaž kategorii"><img src="/images/actions/delete.png" ALT="Smaž sekci" class="ikona"></a>
 </p>
 </#if>
</#if>

<#if TOOL.xpath(CATEGORY,"data/note")??>
 <p class="note">${TOOL.render(TOOL.element(CATEGORY.data,"data/note"),USER!)}</p>
</#if>

<@lib.advertisement id="square" />

<h1>Seznam rubrik</h1>

<ul>
    <#list SORT.byName(CHILDREN) as relation>
        <li>
            <a href="${relation.url?default("/clanky/dir/"+relation.id)}">${TOOL.childName(relation)}</a>
            <#assign articles = (VARS.articleTree.getByRelation(relation.id).size)?default(0)>
            <#if (articles > 0)>(${articles} článků)</#if>
        </li>
    </#list>
</ul>

<#include "../footer.ftl">
