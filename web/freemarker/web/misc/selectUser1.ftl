<#include "../header.ftl">

<#if PARAMS.TITLE?exists><h1 class="st_nadpis">${PARAMS.TITLE}</h1></#if>

<p>Tento formuláø slou¾í pro hledání u¾ivatele portálu
www.abclinuxu.cz. Existuje nìkolik mo¾ností, jak mù¾ete
najít urèitého u¾ivatele. Mù¾ete zadat jeho</p>

<ul>
<li>èíslo
<li>pøihla¹ovací jméno (tøeba jen èást)
<li>jméno u¾ivatele (tøeba jen èást)
<li>email (tøeba jen èást)
</ul>

<p>V¾dy vyplòte alespoò jedno vstupní políèko, do nìho¾ napi¹te nejménì
tøi písmena a ode¹lete formuláø. Na dal¹í stránce si budete moci vybrat
z nalezených u¾ivatelù, se kterým budete moci pokraèovat v této akci.</p>

<@lib.showMessages/>

<form action="${URL.noPrefix("/SelectUser")}" method="POST">

  <table border="0" cellpadding="5">
   <tr>
    <td width="100">Èíslo u¾ivatele</td>
    <td>
     <input type="text" name="uid" size="5" value="${PARAMS.uid?if_exists}">
     <div class="error">${ERRORS.uid?if_exists}</div>
    </td>
   </tr>
   <tr>
    <td width="100">Pøihla¹ovací jméno</td>
    <td>
     <input type="text" name="login" size="25" value="${PARAMS.login?if_exists}">
     <div class="error">${ERRORS.login?if_exists}</div>
    </td>
   </tr>
   <tr>
    <td width="100">Jméno u¾ivatele</td>
    <td>
     <input type="text" name="name" size="25" value="${PARAMS.name?if_exists}">
     <div class="error">${ERRORS.name?if_exists}</div>
    </td>
   </tr>
   <tr>
    <td width="100">Email u¾ivatele</td>
    <td>
     <input type="text" name="email" size="25" value="${PARAMS.email?if_exists}">
     <div class="error">${ERRORS.email?if_exists}</div>
    </td>
   </tr>
   <tr>
    <td width="100">&nbsp;</td>
    <td>
     <input type="submit" value="Pokraèuj">
    </td>
   </tr>
  </table>
  ${SAVED_PARAMS?if_exists}
  <input type="hidden" name="sAction" value="search">
</form>

<#include "../footer.ftl">
