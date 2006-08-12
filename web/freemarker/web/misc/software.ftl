<#macro showSoftware(software, showGallery)>
    <h1>${TOOL.xpath(software,"/data/name")?if_exists}</h1>

    <div>
        ${TOOL.render(TOOL.element(software.data,"data/description"),USER?if_exists)}
    </div>

    <table class="swdetail">
        <tr>
            <td>Prostøedí:</td>
            <td>
                <#list software.getProperty("ui") as ui>
                    ${UI_PROPERTY[ui]}<#if ui_has_next>, </#if>
                </#list>
            </td>
        </tr>

        <#local licenses = software.getProperty("license")>
        <#if licenses?size gt 0>
            <tr>
                <td>Licence:</td>
                <td>
                    <#list licenses as license>
                        ${LICENSE_PROPERTY[license]}<#if license_has_next>, </#if>
                    </#list>
                </td>
            </tr>
        </#if>

        <#local url = TOOL.xpath(software, "/data/url[@useType='homepage']")?if_exists>
    	<#if url != "UNDFEFINED">
            <tr>
	        	<td>Domovská stránka:</td>
                <td><a href="${url}" rel="nofollow">${url}</a></td>
            </tr>
        </#if>
        <#local url = TOOL.xpath(software, "/data/url[@useType='download']")?default("UNDFEFINED")>
        <#if url != "UNDFEFINED">
            <tr>
	        	<td>Adresa ke sta¾ení:</td>
                <td><a href="${url}" rel="nofollow">${url}</a></td>
            </tr>
        </#if>

        <#local alternatives = software.getProperty("alternative")>
        <#if alternatives?size gt 0>
            <tr>
                <td>Je alternativou k:</td>
                <td>
                    <#list alternatives as alternative>
                        <a href="">${alternative}</a><#if alternative_has_next>, </#if>
                    </#list>
                </td>
            </tr>
        </#if>

    </table>

    <#if showGallery>
        <#local images = TOOL.screenshotsFor(software)>
        <#if (images?size > 0)>
            <h3>Galerie</h3>

            <p class="galerie">
                <#list images as image>
                    <#if image.thumbnailPath?exists>
                        <a href="${image.path}"><img src="${image.thumbnailPath}" alt="${TOOL.xpath(software,"/data/name")?if_exists}" border="0"></a>
                    <#else>
                        <img src="${image.path}" alt="${TOOL.xpath(software,"/data/name")?if_exists}">
                    </#if>
                </#list>
            </p>
        </#if>
    </#if>
</#macro>
