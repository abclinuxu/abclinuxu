<#import "../macros.ftl" as lib>
<#include "../header.ftl">

<@lib.showParents />

<h1>${TOOL.xpath(ITEM,"/data/name")}</h1>

<#assign RECORDS = CHILDREN.record>
<#list RECORDS as REL_RECORD>
    <div class="dict-item">
        <#assign RECORD = REL_RECORD.child, who=TOOL.createUser(RECORD.owner)>
        ${TOOL.render(TOOL.element(RECORD.data,"/data/description"),USER?if_exists)}
    </div>
</#list>

<#include "../footer.ftl">
