<#macro showPersonality(personality, showGallery)>
    <h1>
        ${TOOL.childName(personality)?html}
    </h1>

    <div class="dict-item">
        ${TOOL.render(TOOL.xpath(personality,"/data/description"),USER!)}
    </div>

    <table class="swdetail">
            <#local date = TOOL.xpath(personality, "/data/date[@type='birth']")!"UNDFEFINED">
            <#if date != "UNDFEFINED">
                <tr>
                    <td>Datum narození:</td>
                    <td>${date}</td>
                </tr>
            </#if>
            <#local date = TOOL.xpath(personality, "/data/date[@type='death']")!"UNDFEFINED">
            <#if date != "UNDFEFINED">
                <tr>
                    <td>Datum úmrtí:</td>
                    <td>${date}</td>
                </tr>
            </#if>
            <#local url = TOOL.xpath(personality, "/data/url[@useType='info']")!"UNDFEFINED">
            <#if url != "UNDFEFINED">
                <tr>
                    <td>Web:</td>
                    <td><a href="${url}" rel="nofollow">${TOOL.limit(url,45,"..")}</a></td>
                </tr>
            </#if>
    </table>

    <#if showGallery>
        <@lib.showGallery ITEM "Fotografie"/>
    </#if>
</#macro>
