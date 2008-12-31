<#import "../macros.ftl" as lib>
<#include "../header.ftl">

<@lib.showParents/>

<#if TOOL.xpath(CATEGORY,"data/note")??>
 <p class="note">${TOOL.render(TOOL.xpath(CATEGORY,"data/note"),USER!)}</p>
</#if>

<#if CHILDREN.article??>
 <#list SORT.byDate(CHILDREN.article, "DESCENDING") as clanek>
  <@lib.showArticle clanek/>
  <#if clanek_has_next><@lib.separator/><#else><@lib.doubleSeparator/></#if>
 </#list>
 <br>
</#if>

<#if CHILDREN.category??>
 <table width="100%" border="0" cellpadding="2">
 <tr><td colspan="3" class="cerna3"><strong>Sekce</strong></td></tr>
 <#list SORT.byName(CHILDREN.category) as sekce>
  <#if sekce_index%3==0><tr></#if>
  <td width="33%">
  <a href="../../${DUMP.getFile(sekce.id)}">${TOOL.childName(sekce)}</a>
  </td>
  <#if sekce_index%3==2></tr></#if>
 </#list>
 </table>
 <br>
</#if>

<#if CHILDREN.make??>
 <table width="100%" border="0" cellpadding="2">
 <tr><td colspan="3" class="cerna3"><strong>Položky</strong></td></tr>
 <#list SORT.byName(CHILDREN.make) as polozka>
  <#if polozka_index%3==0><tr></#if>
  <td width="33%">
  <a href="../../${DUMP.getFile(polozka.id)}">${TOOL.childName(polozka)}</a>
  </td>
  <#if polozka_index%3==2></tr></#if>
 </#list>
 </table>
 <br>
</#if>

<#if CHILDREN.driver??>
 <ul>
 <#list SORT.byDate(CHILDREN.driver,"DESCENDING") as driver>
  <li><a href="../../${DUMP.getFile(driver.id)}">
   ${TOOL.childName(driver)}, verze ${TOOL.xpath(driver.child,"data/version")}
  </a></li>
 </#list>
 </ul>
</#if>

<#include "../footer.ftl">
