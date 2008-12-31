<#macro advertisement (id)>${TOOL.getAdvertisement(id, .vars)}</#macro>

<#macro niceAdvertisementRegexp (regexp)>
    <#list TOOL.getStandardAdRegexps().entrySet() as entry><#if entry.key==regexp>${entry.value}<#local found=true><#break></#if></#list>
    <#if !found??>${regexp}</#if>
</#macro>

<#macro showTagAdvertisement text, image, trackerImage = {"src":"UNDEFINED"}, cssClass = {"name":"stitek-ad"}>
    <div class="${cssClass.name}" style="min-height:${image.height}px">
        <#if trackerImage.src != "UNDEFINED">
            <img src="${trackerImage.src}" border='0' alt='' />
        </#if>
        <a href="${text.url}"><img src="${image.src}" width="${image.width}" height="${image.height}" alt="${image.alt}" style="vertical-align:middle"></a>
        <div style="vertical-align:middle"><a href="${text.url}">${text.text}</a><br /> Vaše&nbsp;cena:&nbsp;<b>${text.price}&nbsp;Kč</b>.</div>
    </div>
</#macro>

<#macro showPrice id>
    ${(VARS.getProduct("64bit", id).price)!!}
</#macro>