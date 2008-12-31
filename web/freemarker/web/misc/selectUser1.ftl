<#include "../header.ftl">

<#if PARAMS.TITLE??><h1 class="st_nadpis">${PARAMS.TITLE}</h1></#if>

<p>Tento formulář slouží pro hledání uživatele portálu
www.abclinuxu.cz. Existuje několik možností, jak můžete
najít určitého uživatele. Můžete zadat jeho</p>

<ul>
<li>číslo
<li>přihlašovací jméno (třeba jen část)
<li>jméno uživatele (třeba jen část)
<li>email (třeba jen část)
<li>bydliště (třeba jen část)
</ul>

<p>Vždy vyplňte alespoň jedno vstupní políčko, do něhož napište nejméně
tři písmena a odešlete formulář. Na další stránce si budete moci vybrat
z nalezených uživatelů, se kterým budete moci pokračovat v této akci.</p>

<@lib.showMessages/>

<form action="${URL.noPrefix("/SelectUser")}" method="POST">

  <table border="0" cellpadding="5">
   <tr>
    <td width="100">Číslo uživatele</td>
    <td>
     <input type="text" name="uid" size="5" value="${PARAMS.uid!}">
     <div class="error">${ERRORS.uid!}</div>
    </td>
   </tr>
   <tr>
    <td width="100">Přihlašovací jméno</td>
    <td>
     <input type="text" name="login" size="25" value="${PARAMS.login!}">
     <div class="error">${ERRORS.login!}</div>
    </td>
   </tr>
   <tr>
    <td width="100">Jméno uživatele</td>
    <td>
     <input type="text" name="name" size="25" value="${PARAMS.name!}">
     <div class="error">${ERRORS.name!}</div>
    </td>
   </tr>
   <tr>
    <td width="100">Email uživatele</td>
    <td>
     <input type="text" name="email" size="25" value="${PARAMS.email!}">
     <div class="error">${ERRORS.email!}</div>
    </td>
   </tr>
    <tr>                                                                                                            
    <td width="100">Bydliště uživatele</td>                                                                        
    <td>                                                                                                           
     <input type="text" name="city" size="25" value="${PARAMS.city!}">
     <div class="error">${ERRORS.city!}</div>
    </td>                                                                                                          
   </tr>
   <tr>
    <td width="100">&nbsp;</td>
    <td>
     <input type="submit" value="Pokračuj">
    </td>
   </tr>
  </table>
  ${SAVED_PARAMS!}
  <input type="hidden" name="sAction" value="search">
</form>

<#include "../footer.ftl">
