<#include "../header.ftl">
<#if USER?exists && USER.hasRole("content admin")>
 <p>
  <a href="${URL.make("/editContent/"+RELATION.id+"?action=edit")}">Upravit</a>
 </p>
</#if>

${TOOL.xpath(ITEM,"/data/content")}

<#include "../footer.ftl">
