<#include "/offline/macros.ftl">
<#call showParents>

<#if TOOL.xpath(CATEGORY,"data/note")?exists>
 <p class="note">${TOOL.render(TOOL.xpath(CATEGORY,"data/note"))}</p>
</#if>

<#global map=TOOL.groupByType(CATEGORY.content)>

<#if map.article?exists>
 <#list SORT.byDate(map.article, "DESCENDING") as clanek>
  <#call showArticle(clanek)>
  <#if clanek_has_next><#call separator><#else><#call doubleSeparator></#if>
 </#list>
 <br>
</#if>

<#if map.category?exists>
 <table width="100%" border="0" cellpadding="2">
 <tr><td colspan="3" class="cerna3"><strong>Sekce</strong></td></tr>
 <#list SORT.byName(map.category) as sekce>
  <#if sekce_index%3==0><tr></#if>
  <td width="33%">
  <a href="../${DUMP.getFile(sekce.id)}">${TOOL.childName(sekce)}</a>
  </td>
  <#if sekce_index%3==2></tr></#if>
 </#list>
 </table>
 <br>
</#if>

<#if map.make?exists>
 <table width="100%" border="0" cellpadding="2">
 <tr><td colspan="3" class="cerna3"><strong>Polo¾ky</strong></td></tr>
 <#list SORT.byName(map.make) as polozka>
  <#if polozka_index%3==0><tr></#if>
  <td width="33%">
  <a href="../${DUMP.getFile(polozka.id)}">${TOOL.childName(polozka)}</a>
  </td>
  <#if polozka_index%3==2></tr></#if>
 </#list>
 </table>
 <br>
</#if>

<#if map.driver?exists>
 <ul>
 <#list SORT.byDate(map.driver,"DESCENDING") as driver>
  <li><a href="../${DUMP.getFile(driver.id)}">
   ${TOOL.childName(driver)}, verze ${TOOL.xpath(driver.child,"data/version")}
  </a></li>
 </#list>
 </ul>
</#if>


<#if map.discussion?exists>
 <#call showDiscussions(map.discussion)>
</#if>
