<#include "/include/macros.ftl">
<#include "../header.ftl">

<#call showMessages>

<p>Zvolte si relaci. Pokud chcete zobrazit její obsah, kliknìte na tlaèítko
<b>Naèti relaci</b>. V opaèném pøípadì zvolte tlaèítko <b>Pokraèuj</b>.
</p>

<form action="${URL.noPrefix("/SelectRelation")}" method="POST">

 <#if CURRENT?exists>

  <p>
   <input type="submit" name="continue" value="Naèti relaci"> &nbsp;
   <input type="submit" name="confirm" value="Pokraèuj">
  </p>

  <table border="0" cellpadding="5">
  <tr>
   <td colspan="5">
    <input type="radio" name="currentId" value="${CURRENT.id}" checked>
    <b>${TOOL.childName(CURRENT)}</b> (${CURRENT.id})
   </td>
  </tr>

  <#global map=TOOL.groupByType(CURRENT.child.children)>

  <#list map.keySet() as key>
   <tr><td coslpan="5"><b>${key}</b></td></tr>
   <#list map(key) as i>
    <#if i_index%5==0><tr></#if>
    <td>
     <input type="radio" name="currentId" value="${i.id}">${TOOL.childName(i)}
    </td>
    <#if i_index%5==4></tr></#if>
   </#list>
  </#list>


 <#else>

  <p>Pokud znáte èíslo relace, vlo¾te jej zde:
   <input type="text" name="enteredId" size="6">
   <span class="error">${ERRORS?if_exists.enteredId?if_exists}</span>
   <input type="submit" name="continue" value="Naèti relaci">
   <input type="submit" name="confirm" value="Pokraèuj">
  </p>

  <table border="0" cellpadding="5">
   <tr><th colspan="5">Hardware / 386</th></tr>
   <#list SORT.byName(H386) as i>
    <#if i_index%5==0><tr></#if>
    <td>
     <input type="radio" name="currentId" value="${i.id}" <#if i_index==0>checked</#if> >
     ${TOOL.childName(i)}
    </td>
    <#if i_index%5==4></tr></#if>
   </#list>

   <tr><th colspan="5">Diskuse</th></tr>
   <#list SORT.byName(FORUM) as i>
    <#if i_index%5==0><tr></#if>
    <td>
     <input type="radio" name="currentId" value="${i.id}">${TOOL.childName(i)}
    </td>
    <#if i_index%5==4></tr></#if>
   </#list>

   <tr><th colspan="5">Èlánky</th></tr>
   <#list SORT.byName(CLANKY) as i>
    <#if i_index%5==0><tr></#if>
    <td>
     <input type="radio" name="currentId" value="${i.id}">${TOOL.childName(i)}
    </td>
    <#if i_index%5==4></tr></#if>
   </#list>

 </#if>

 </table>

 ${TOOL.saveParams(PARAMS, ["currentId","enteredId","continue"])}
</form>

<#include "../footer.ftl">
