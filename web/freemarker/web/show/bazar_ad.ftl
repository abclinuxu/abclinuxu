<#assign plovouci_sloupec>
  <div class="s_sekce">
    <ul>
    <#if USER?exists>
        <#if USER.id==ITEM.owner || USER.hasRole("bazaar admin")>
            <li><a href="${URL.make("/edit/"+RELATION.id+"?action=edit")}">Uprav inzer�t</a></li>
            <li><a href="${URL.make("/edit/"+RELATION.id+"?action=remove")}">Sma� inzer�t</a></li>
        </#if>
    <#else>
        <li><a href="${URL.noPrefix("/Profile?action=login&amp;url="+REQUEST_URI)}">P�ihl�sit se</a></li>
    </#if>
    </ul>
  </div>
</#assign>
<#include "../header.ftl">

<@lib.showMessages/>

<h1>
    ${TOOL.xpath(ITEM.data, "/data/title")}
    (<#if ITEM.subType=='buy'>prodej<#elseif ITEM.subType=='sell'>koup�<#else>darov�n�</#if>)
</h1>

<div>
    ${TOOL.render(TOOL.xpath(ITEM.data,"/data/text"), USER?if_exists)}
</div>

<h3>Koment��e</h3>
<#if CHILDREN.discussion?exists>
    <@lib.showDiscussion CHILDREN.discussion[0]/>
<#else>
   <a href="${URL.make("/EditDiscussion?action=addDiz&amp;rid="+RELATION.id)}">Vlo�it prvn� koment��</a>
</#if>


<#include "../footer.ftl">
