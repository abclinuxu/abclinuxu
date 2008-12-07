<#macro advertisement (id)>${TOOL.getAdvertisement(id, .vars)}</#macro>

<#macro niceAdvertisementRegexp (regexp)>
    <#list TOOL.getStandardAdRegexps().entrySet() as entry><#if entry.key==regexp>${entry.value}<#local found=true><#break></#if></#list>
    <#if !found?exists>${regexp}</#if>
</#macro>

<#macro showTagAdvertisement url = "UNDEFINED", text = "UNDEFINED", image = "UNDEFINED", trackerImage = {"src":"UNDEFINED"}, cssClass = "stitek-ad">
    <div class="${cssClass}">
        <#if trackerImage.src != "UNDEFINED">
            <img src="${trackerImage.src}" border='0' alt='' />
        </#if>
        <#if url != "UNDEFINED">
            <#if image != "UNDEFINED">
                <a href="${url}"><img src="${image.src}" width="${image.width}" height="${image.height}" alt="${image.alt}"></a>
            </#if>
            <#if text != "UNDEFINED">
                <a href="${url}">${text.text}</a><br /> Vaše&nbsp;cena:&nbsp;<b>${text.price}&nbsp;Kč</b>.
            </#if>
        </#if>
    </div>
</#macro>
