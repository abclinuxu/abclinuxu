<#include "../header.ftl">

<@lib.showMessages/>

<form action="${URL.noPrefix("/SelectIcon")}" method="POST">
 <select name="dir" onchange="reload.value='yes'; submit();">
 <#list DIRS as d>
   <option value="${d}" <#if d==DIR>SELECTED</#if> >${d}</option>
 </#list>
 </select>

 <table width="100%" border="1" cellpadding="5">
 <#list ICONS as i>
  <#if i_index%4==0><tr></#if>
  <td>
   <input type="radio" name="icon" value="${i}" <#if i_index==0>checked</#if> >
   <img src="/ikony/${DIR}/${i}">
  </td>
  <#if i_index%4==3></tr></#if>
 </#list>
 </table>

 ${TOOL.saveParams(PARAMS, ["dir","reload"])}
 <input type="hidden" name="reload" value="no">
 <input type="submit" name="finish" value="OK">
</form>

<#include "../footer.ftl">
