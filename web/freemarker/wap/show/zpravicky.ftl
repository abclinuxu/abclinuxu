<#include "../header.ftl">
<card title="Zpr�vi�ky">
<#list FOUND.data as relation>
 <#global ITEM=TOOL.sync(relation.child)>
  <p>${DATE.show(ITEM.created,"CZ_SHORT")}<br/>
  ${TOOL.removeTags(TOOL.xpath(ITEM,"data/content"))?xml}
  <br/><br/></p>
</#list>
<#if FOUND.nextPage?exists>
<p>
 <anchor>Dal��<go href="/zpravicky" method="post"><postfield name="from" value="${FOUND.nextPage.row}"/></go></anchor>
</p>
</#if>
</card>
<#include "../footer.ftl">