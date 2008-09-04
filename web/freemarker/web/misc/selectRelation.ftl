<#include "../header.ftl">

<@lib.showMessages/>

<#if PARAMS.rid?exists><h2>Výběr relace pro objekt: <i>${TOOL.childName(PARAMS.rid)}</i></h2></#if>

<p>Zvolte si relaci. Pokud chcete zobrazit její obsah, klikněte na tlačítko
<b>Načti relaci</b>. V opačném případě zvolte tlačítko <b>Pokračuj</b>.</p>

<form action="${URL.noPrefix("/SelectRelation")}" method="POST">

 <#if CURRENT?exists>

  <p>
   <input type="submit" name="continue" value="Načti relaci"> &nbsp;
   <input type="submit" name="confirm" value="Pokračuj">
  </p>

  <table border="0" cellpadding="5">
  <tr>
   <td colspan="5">
    <input type="radio" name="currentId" value="${CURRENT.id}" checked>
    <b>${TOOL.childName(CURRENT)}</b> (${CURRENT.id})
   </td>
  </tr>

  <#assign map=TOOL.groupByType(CURRENT.child.children)>

  <#list map.keySet() as key>
   <tr><td coslpan="5"><b>${key}</b></td></tr>
   <#list map(key) as i>
    <#if i_index%5==0><tr></#if>
    <td>
     <@lib.showOption "currentId", i.id?string, TOOL.childName(i), "radio"/><br>
    </td>
    <#if i_index%5==4></tr></#if>
   </#list>
  </#list>


 <#else>

  <p>Pokud znáte číslo relace, vložte jej zde:
   <input type="text" name="enteredId" size="6">
   <span class="error">${ERRORS?if_exists.enteredId?if_exists}</span>
   <input type="submit" name="continue" value="Načti relaci">
   <input type="submit" name="confirm" value="Pokračuj">
  </p>

  <table border="0" cellpadding="5">

   <tr><th colspan="5">Diskuse</th></tr>
   <#list SORT.byName(FORUM) as i>
    <#if i_index%5==0><tr></#if>
    <td>
     <@lib.showOption "currentId", i.id?string, TOOL.childName(i), "radio"/><br>
    </td>
    <#if i_index%5==4></tr></#if>
   </#list>

   <tr><th colspan="5">Hardware / 386</th></tr>
   <#list SORT.byName(H386) as i>
    <#if i_index%5==0><tr></#if>
    <td>
     <@lib.showOption "currentId", i.id?string, TOOL.childName(i), "radio"/><br>
    </td>
    <#if i_index%5==4></tr></#if>
   </#list>

   <tr><th colspan="5">Software</th></tr>
   <#list SORT.byName(SOFTWARE) as i>
    <#if i_index%5==0><tr></#if>
    <td>
     <@lib.showOption "currentId", i.id?string, TOOL.childName(i), "radio"/><br>
    </td>
    <#if i_index%5==4></tr></#if>
   </#list>

   <tr><th colspan="5">Články</th></tr>
   <#list SORT.byName(CLANKY) as i>
    <#if i_index%5==0><tr></#if>
    <td>
     <@lib.showOption "currentId", i.id?string, TOOL.childName(i), "radio"/><br>
    </td>
    <#if i_index%5==4></tr></#if>
   </#list>

 </#if>

 </table>

 ${TOOL.saveParams(PARAMS, ["currentId","enteredId","continue"])}
</form>

<#include "../footer.ftl">
