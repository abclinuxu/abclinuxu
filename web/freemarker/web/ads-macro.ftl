<#macro advertisement (id)>${TOOL.getAdvertisement(id, .vars)}</#macro>

<#macro niceAdvertisementRegexp (regexp)>
    <#list TOOL.getStandardAdRegexps().entrySet() as entry><#if entry.key==regexp>${entry.value}<#local found=true><#break></#if></#list>
    <#if !found?exists>${regexp}</#if>
</#macro>
