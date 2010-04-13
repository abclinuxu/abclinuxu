<#include "../header.ftl">

<h1>${PAGE_TITLE}</h1>

<#if DESC??>
    <div>
        ${DESC}
    </div>
</#if>

<table class="siroka" cellspacing="5px">
    <#list USERS as user>
        <#if user_index % 2 = 0><tr></#if>
        <td>
            <@lib.showUser user/>
            <#assign score=user.getIntProperty("score")!(-1)>
            <#if score != -1> | sk¨®re: ${score}</#if>
            <#assign city=TOOL.xpath(user,"//personal/city")!"UNDEF">
            <#if city!="UNDEF"> | ${city}</#if>
        </td>
        <#if user_index % 2 = 1 || ! user_has_next></tr><#assign open=false></#if>
    </#list>
</table>

<div class="no-col-ad">
    <@lib.advertisement id="hypertext2nahore" />
    <@lib.advertisement id="square" />
    <@lib.advertisement id="hypertext2dole" />
</div>

<#include "../footer.ftl">
