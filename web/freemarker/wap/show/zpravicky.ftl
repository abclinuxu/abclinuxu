<#include "../header.ftl">
<card title="Zprávièky">
<#list FOUND.data as relation>
 <#assign ITEM=TOOL.sync(relation.child)>
  <p>${DATE.show(ITEM.created,"CZ_SHORT")}<br/>
  ${TOOL.removeTags(TOOL.xpath(ITEM,"data/content"))}
  <br/><br/></p>
</#list>
<#if FOUND.nextPage?exists>
<p>
 <anchor>Dal¹í<go href="/zpravicky" method="post"><postfield name="from" value="${FOUND.nextPage.row}"/></go></anchor>
</p>
</#if>
</card>
<#include "../footer.ftl">