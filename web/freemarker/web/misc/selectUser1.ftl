<#include "../header.ftl">

<#if PARAMS.TITLE?exists><h1 class="st_nadpis">${PARAMS.TITLE}</h1></#if>

<p>Tento formul�� slou�� pro hled�n� u�ivatele port�lu
www.abclinuxu.cz. Existuje n�kolik mo�nost�, jak m��ete
naj�t ur�it�ho u�ivatele. M��ete zadat jeho</p>

<ul>
<li>��slo
<li>p�ihla�ovac� jm�no (t�eba jen ��st)
<li>jm�no u�ivatele (t�eba jen ��st)
<li>email (t�eba jen ��st)
</ul>

<p>V�dy vypl�te alespo� jedno vstupn� pol��ko, do n�ho� napi�te nejm�n�
t�i p�smena a ode�lete formul��. Na dal�� str�nce si budete moci vybrat
z nalezen�ch u�ivatel�, se kter�m budete moci pokra�ovat v t�to akci.</p>

<@lib.showMessages/>

<form action="${URL.noPrefix("/SelectUser")}" method="POST">

  <table border="0" cellpadding="5">
   <tr>
    <td width="100">��slo u�ivatele</td>
    <td>
     <input type="text" name="uid" size="5" value="${PARAMS.uid?if_exists}">
     <div class="error">${ERRORS.uid?if_exists}</div>
    </td>
   </tr>
   <tr>
    <td width="100">P�ihla�ovac� jm�no</td>
    <td>
     <input type="text" name="login" size="25" value="${PARAMS.login?if_exists}">
     <div class="error">${ERRORS.login?if_exists}</div>
    </td>
   </tr>
   <tr>
    <td width="100">Jm�no u�ivatele</td>
    <td>
     <input type="text" name="name" size="25" value="${PARAMS.name?if_exists}">
     <div class="error">${ERRORS.name?if_exists}</div>
    </td>
   </tr>
   <tr>
    <td width="100">Email u�ivatele</td>
    <td>
     <input type="text" name="email" size="25" value="${PARAMS.email?if_exists}">
     <div class="error">${ERRORS.email?if_exists}</div>
    </td>
   </tr>
   <tr>
    <td width="100">&nbsp;</td>
    <td>
     <input type="submit" value="Pokra�uj">
    </td>
   </tr>
  </table>
  ${SAVED_PARAMS?if_exists}
  <input type="hidden" name="sAction" value="search">
</form>

<#include "../footer.ftl">
