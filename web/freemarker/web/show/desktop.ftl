<#if USER?exists>
    <#assign plovouci_sloupec>
        <div class="s_sekce">
            <ul>
                <li>
                    <a class="bez-slovniku" href="${URL.make("/edit/"+RELATION.id+"?action=edit")}" rel="nofollow">Upravit</a>
                </li>
                <#if USER.hasRole("attachment admin")>
                    <li>
                        <a href="${URL.make("/edit/"+RELATION.id+"?action=rm2"+TOOL.ticket(USER, false))}" title="Smaž"
                        onClick="javascript:return confirm('Opravdu chcete smazat tento desktop?');">Smazat</a>
                    </li>
                </#if>
            </ul>
        </div>
    </#assign>
</#if>

<#include "../header.ftl">

<h1>${TOOL.xpath(ITEM,"/data/title")}</h1>

<p>
    <a href="${TOOL.xpath(ITEM,"/data/image")}">
        <img src="${TOOL.xpath(ITEM,"/data/detailThumbnail")}" alt="${TOOL.xpath(ITEM,"/data/title")}" border="0">
    </a>
</p>

<table class="swdetail">
    <#assign usedBy = ITEM.getProperty("favourited_by"), autor=TOOL.createUser(ITEM.owner)>
    <#assign desc=TOOL.xpath(ITEM, "/data/description")?default("UNDEFINED")>
    <tr>
        <td>Autor</td>
        <td><@lib.showUser autor/></td>
    </tr>
    <tr>
        <td>Datum</td>
        <td>${DATE.show(ITEM.created,"SMART_DMY")}</td>
    </tr>
    <tr>
        <td>Oblíbenost</td>
        <td>
           <form action="${URL.make("/desktopy/edit/"+RELATION.id)}">
              <#if (usedBy?size > 0)>
                  <a href="?action=users" title="Seznam uživatelů abclinuxu, kterým se líbí tento desktop">${usedBy?size}</a> &nbsp;
              <#else>
              0
              </#if>
              <#if USER?exists && usedBy.contains(""+USER.id)>
                 <input type="submit" value="Odebrat se">
              <#else>
                 <input type="submit" value="Přidat se">
              </#if>
              <input type="hidden" name="action" value="favourite">
              <input type="hidden" name="ticket" value="${TOOL.ticketValue(USER?if_exists)}">
           </form>
        </td>
    </tr>
    <tr>
        <td>Shlédnuto</td>
        <td>
            <#assign reads = TOOL.getCounterValue(ITEM,"read")>
            ${reads}&times;
        </td>
    </tr>
    <#if desc != "UNDEFINED">
        <tr>
            <td colspan="2">${desc}</td>
        </tr>
    </#if>
</table>

<h3>Komentáře</h3>
<#if CHILDREN.discussion?exists>
    <@lib.showDiscussion CHILDREN.discussion[0]/>
<#else>
   <a href="${URL.make("/EditDiscussion?action=addDiz&amp;rid="+RELATION.id)}">Vložit první komentář</a>
</#if>

<#include "../footer.ftl">
