<#macro advertisement (id)>${TOOL.getAdvertisement(id, .vars)}</#macro>

<#macro niceAdvertisementRegexp (regexp)>
    <#list TOOL.getStandardAdRegexps().entrySet() as entry><#if entry.key==regexp>${entry.value}<#local found=true><#break></#if></#list>
    <#if !found?exists>${regexp}</#if>
</#macro>

<#macro showTagAdvertisement text, image, trackerImage = {"src":"UNDEFINED"}>
    <style type="text/css">
    .stitek-ad { background:#649bcc; color:white; padding:0.3em 0.5em 0.5em 0.5em; margin:0.5em; border: 1px solid silver; }
    .stitek-ad a { color:white; }
    .stitek-ad a:hover { color:#649bcc; background:white; }
    .stitek-ad img { margin:0 0.5em 0 -0.3em; float:left; }
    </style>
    <div class="stitek-ad">
        <#if trackerImage.src != "UNDEFINED"><img src="${trackerImage.src}" border='0' alt='' /></#if>
        <a href="${text.url}"><img src="${image.src}" width="${image.width}" height="${image.height}" alt="${image.alt}"></a>
        <a href="${text.url}">${text.text}</a><br /> Vaše&nbsp;cena:&nbsp;<b>${text.price}&nbsp;Kč</b>.
    </div>
</#macro>
