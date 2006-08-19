<#include "../header.ftl">

<@lib.showMessages/>

<form action="${URL.make("/blog/edit/"+STORY.id)}" method="POST">

<p>Opravdu chcete smazat tento z�pis ve va�em blogu?
Tato operace je nevratn� a z�rove� budou odstran�ny
v�echny p��padn� koment��e z�pisu.
<input type="submit" name="finish" value="Sma�">
</p>

<input type="hidden" name="action" value="remove2">
</form>

 <h1 class="st_nadpis">N�hled va�eho z�pisu</h1>

<div style="padding-left: 30pt">
    <h2>${TOOL.xpath(STORY.child, "/data/name")}</h2>
    <p class="cl_inforadek">${DATE.show(STORY.child.created, "CZ_SHORT")} |
        P�e�teno: ${TOOL.getCounterValue(STORY.child,"read")}x
        <#assign category = STORY.child.subType?default("UNDEF")>
        <#if category!="UNDEF"><#assign category=TOOL.xpath(BLOG, "//category[@id='"+category+"']/@name")?default("UNDEF")></#if>
        <#if category!="UNDEF">| ${category}</#if>
    </p>
    ${TOOL.xpath(STORY.child, "/data/content")}
</div>


<#include "../footer.ftl">
