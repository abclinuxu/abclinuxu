<#include "../header.ftl">

<@lib.showMessages/>

<p>Na t�to str�nce si m��ete zm�nit sv� z�kladn� �daje.
Pro va�i ochranu nejd��ve zadejte sou�asn� heslo.</p>

<p>
P�ihla�ovac� jm�no (login) mus� m�t nejm�n� t�i znaky,
maxim�ln� 16 znak� a to pouze p�smena A a� Z, ��slice,
poml�ku, te�ku nebo podtr��tko.
Login v�s jednozna�n� identifikuje v syst�mu,
proto nen� mo�n� pou��vat hodnotu, kter� ji� pou�il
n�kdo p�ed v�mi.
</p>

<p>Va�e jm�no mus� b�t nejm�n� p�t znak� dlouh�. Email mus� b�t platn�.
Nebojte se, budeme jej chr�nit p�ed spammery a my v�m budeme
zas�lat jen ty informace, kter� si sami objedn�te.
</p>

<form action="${URL.noPrefix("/EditUser")}" method="POST">
 <table width="100%" border=0 cellpadding=5>
  <tr>
   <td class="required" width="60">Heslo</td>
   <td>
    <input type="password" name="PASSWORD" size="16" tabindex="1">
    <div class="error">${ERRORS.PASSWORD?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td class="required" width="60">Jm�no</td>
   <td>
    <input type="text" name="name" value="${PARAMS.name?if_exists}" size="24" tabindex="2">
    <div class="error">${ERRORS.name?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td class="required" width="60">Login</td>
   <td>
    <input type="text" name="login" value="${PARAMS.login?if_exists}" size="24" tabindex="3">
    <div class="error">${ERRORS.login?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td class="required" width="60">Email</td>
   <td>
    <input type="text" name="email" value="${PARAMS.email?if_exists}" size="24" tabindex="4">
    <div class="error">${ERRORS.email?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td class="required" width="60">P�ezd�vka</td>
   <td>
    <input type="text" name="nick" value="${PARAMS.nick?if_exists}" size="24" tabindex="5">
    <div class="error">${ERRORS.nick?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="60">&nbsp;</td>
   <td><input type="submit" value="Dokon�i" tabindex="6"></td>
  </tr>
 </table>
 <input type="hidden" name="action" value="editBasic2">
 <input type="hidden" name="uid" value="${MANAGED.id}">
</form>


<#include "../footer.ftl">
