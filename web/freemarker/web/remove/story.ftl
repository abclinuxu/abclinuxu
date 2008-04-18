<#include "../header.ftl">

<@lib.showMessages/>

<form action="${URL.make("/blog/edit/"+STORY.id)}" method="POST">

<p>Opravdu chcete smazat tento zápis ve vašem blogu?
Tato operace je nevratná a zároveň budou odstraněny
všechny případné komentáře zápisu.
<input type="submit" name="finish" value="Smaž">
</p>

<input type="hidden" name="action" value="remove2">
</form>

 <h1 class="st_nadpis">Náhled vašeho zápisu</h1>

<div style="padding-left: 30pt">
    <h2>${STORY.child.title}</h2>
    <p class="cl_inforadek">${DATE.show(STORY.child.created, "CZ_SHORT")} |
        Přečteno: ${TOOL.getCounterValue(STORY.child,"read")}x
        <#assign category = STORY.child.subType?default("UNDEF")>
        <#if category!="UNDEF"><#assign category=TOOL.xpath(BLOG, "//category[@id='"+category+"']/@name")?default("UNDEF")></#if>
        <#if category!="UNDEF">| ${category}</#if>
    </p>
    ${TOOL.xpath(STORY.child, "/data/content")}
</div>


<#include "../footer.ftl">
