<#import "../macros.ftl" as lib>
<#include "../header.ftl">

<@lib.showParents />

<h1>${TOOL.xpath(ITEM,"/data/name")}</h1>

<div class="dict-item">
    ${TOOL.render(TOOL.xpath(ITEM,"/data/description"),USER!)}
</div>

<#include "../footer.ftl">
