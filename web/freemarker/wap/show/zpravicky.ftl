<#include "../header.ftl">
<card title="Zprávièky">
<#list FOUND.data as relation>
 <#global ITEM=TOOL.sync(relation.child)>
  <p>${DATE.show(ITEM.created,"CZ_SHORT")}<br/>
  ${TOOL.removeTags(TOOL.xpath(ITEM,"data/content"))?xml}
  <br/></p>
</#list>
<p>
<#if FOUND.nextPage?exists>
<anchor>Dal¹í<go href="/zpravicky" method="post"><postfield name="from" value="${FOUND.nextPage.row}"/></go></anchor>
</#if>
<#if FOUND.prevPage?exists><anchor>Zpìt<prev/></anchor></#if>
</p>
</card>
<#include "../footer.ftl">