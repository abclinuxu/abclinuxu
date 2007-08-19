<#macro showPersonality(personality, showGallery)>
    <h1>${TOOL.xpath(personality,"/data/name")}</h1>

    <div class="dict-item">
        ${TOOL.render(TOOL.xpath(personality,"/data/description"),USER?if_exists)}
    </div>

    <table class="swdetail">
            <#local date = TOOL.xpath(personality, "/data/date[@type='birth']")?default("UNDFEFINED")>
            <#if date != "UNDFEFINED">
                <tr>
                    <td>Datum narození:</td>
                    <td>${date}</td>
                </tr>
            </#if>
            <#local date = TOOL.xpath(personality, "/data/date[@type='death']")?default("UNDFEFINED")>
            <#if date != "UNDFEFINED">
                <tr>
                    <td>Datum úmrtí:</td>
                    <td>${date}</td>
                </tr>
            </#if>
            <#local url = TOOL.xpath(personality, "/data/url[@useType='info']")?default("UNDFEFINED")>
            <#if url != "UNDFEFINED">
                <tr>
                    <td>Web:</td>
                    <td><a href="${url}" rel="nofollow">${TOOL.limit(url,45,"..")}</a></td>
                </tr>
            </#if>
    </table>

    <#if showGallery>
        <#local images = TOOL.screenshotsFor(personality)>
        <#if (images?size > 0)>
            <h3>Fotografie</h3>

            <p class="galerie">
                <#list images as image>
                    <#if image.thumbnailPath?exists>
                        <a href="${image.path}"><img src="${image.thumbnailPath}" alt="${TOOL.xpath(personality,"/data/name")?if_exists}" border="0"></a>
                    <#else>
                        <img src="${image.path}" alt="${TOOL.xpath(personality,"/data/name")?if_exists}">
                    </#if>
                </#list>
            </p>
        </#if>
    </#if>
</#macro>
