<#macro showSoftware(software, showGallery)>
    <h1>${TOOL.xpath(software,"/data/name")?if_exists}</h1>

    <div>
        ${TOOL.render(TOOL.element(software.data,"data/description"),USER?if_exists)}
    </div>

    <table class="swdetail">
        <tr>
            <td>Prostředí:</td>
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

        <#local url = TOOL.xpath(software, "/data/url[@useType='homepage']")?default("UNDFEFINED")>
    	<#if url != "UNDFEFINED">
            <tr>
	        	<td>Domovská stránka:</td>
                <td>
                    <a href="${"/presmeruj?class=P&amp;id="+software.id+"&amp;url="+url?url}" rel="nofollow">${TOOL.limit(url,45,"..")}</a>
                    <#local visits = TOOL.getCounterValue(software,"visit")>
                    <#if (visits > 0)>
                      <span title="Počet návštěv domovské stránky">(${visits}&times;)</span>
                    </#if>
                </td>
            </tr>
        </#if>
        <#local url = TOOL.xpath(software, "/data/url[@useType='download']")?default("UNDFEFINED")>
        <#if url != "UNDFEFINED">
            <tr>
	        	<td>Adresa ke stažení:</td>
                <td>
		    <a href="${"/presmeruj?class=P&amp;id="+software.id+"&amp;url="+url?url}" rel="nofollow">${TOOL.limit(url,45,"..")}</a>
                </td>
            </tr>
        </#if>

        <#local alternatives = software.getProperty("alternative")>
        <#if alternatives?size gt 0>
            <tr>
                <td>Je alternativou k:</td>
                <td>
                    <#list alternatives as alternative>
                        <a href="/software/alternativy/${alternative?url}">${alternative}</a><#if alternative_has_next>, </#if>
                    </#list>
                </td>
            </tr>
        </#if>

        <#if software.id != 0>
            <#local usedBy = software.getProperty("used_by")>
            <tr>
                <td>
                    Počet uživatelů:
                </td>
                <td>
                    <a href="?action=users" title="Seznam uživatelů abclinuxu, kteří se označili za uživatele">${usedBy?size}</a>
                    <div class="zh-box">
                        <form action="${"/software/edit/"+RELATION.id}">
                            <#if USER?exists && usedBy.contains(""+USER.id)>
                                <input type="submit" value="Odebrat se">
                            <#else>
                                <input type="submit" value="Přidat se">
                            </#if>
                            <input type="hidden" name="action" value="user_of">
                            <input type="hidden" name="ticket" value="${TOOL.ticketValue(USER?if_exists)}">
                        </form>
                    </div>
                </td>
            </tr>
        </#if>

        <#if software.id != 0>
            <tr>
                <td colspan="2">
                    <@lib.showRating RELATION, true, "rating"/>
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
