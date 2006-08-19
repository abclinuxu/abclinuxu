<#include "../header.ftl">

<@lib.showMessages/>

<form action="${URL.make("/blog/edit/"+STORY.id)}" method="POST">

<p>Opravdu chcete smazat tento zápis ve va¹em blogu?
Tato operace je nevratná a zároveò budou odstranìny
v¹echny pøípadné komentáøe zápisu.
<input type="submit" name="finish" value="Sma¾">
</p>

<input type="hidden" name="action" value="remove2">
</form>

 <h1 class="st_nadpis">Náhled va¹eho zápisu</h1>

<div style="padding-left: 30pt">
    <h2>${TOOL.xpath(STORY.child, "/data/name")}</h2>
    <p class="cl_inforadek">${DATE.show(STORY.child.created, "CZ_SHORT")} |
        Pøeèteno: ${TOOL.getCounterValue(STORY.child,"read")}x
        <#assign category = STORY.child.subType?default("UNDEF")>
        <#if category!="UNDEF"><#assign category=TOOL.xpath(BLOG, "//category[@id='"+category+"']/@name")?default("UNDEF")></#if>
        <#if category!="UNDEF">| ${category}</#if>
    </p>
    ${TOOL.xpath(STORY.child, "/data/content")}
</div>


<#include "../footer.ftl">
