<#include "../header.ftl">

<@lib.showMessages/>

<p>
D�kujeme v�m, �e jste se rozhodli zaregistrovat
se na na�em port�lu. V���me, �e nab�zen� v�hody
vyv�� p�r minut str�ven�ch registrac�. Jako
mal� bonus z�sk�te vlastn� domovskou str�nku,
na kter� m��ete prezentovat informace o sv� osob�.
Z�rove� v n� m��ete naj�t sv� diskuse, �l�nky, zpr�vi�ky
�i z�znamy.
</p>

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
Po registraci v�m za�leme email s informacemi
o va�em ��t�, kter� si pe�liv� uschovejte.
</p>

<p>Na t�to str�nce si m��ete tak� objednat dv� atraktivn�
slu�by. Prvn� z nich je t�denn� souhrn �l�nk� a zpr�vi�ek a je
ur�en t�m, kte�� nemaj� �as denn� n�s nav�t�vovat. Pokud si jej
p�ihl�s�te, ka�d� v�kend v�m za�leme emailem seznam
nov�ch �l�nk� a zpr�vi�ek. Druhou slu�bou je Zpravodaj, kter� vych�z�
za��tkem ka�d�ho m�s�ce. Tento email obsahuje spoustu
zaj�mavost� ze sv�ta Linuxu i z na�eho port�lu.
</p>

<form action="${URL.noPrefix("/EditUser")}" method="POST">
 <table width="100%" border=0 cellpadding=5>
  <tr>
   <td class="required" width="60">Jm�no</td>
   <td>
    <input type="text" name="name" value="${PARAMS.name?if_exists}" size="16" tabindex="1">
    <div class="error">${ERRORS.name?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td class="required" width="60">Login</td>
   <td>
    <input type="text" name="login" value="${PARAMS.login?if_exists}" size="24" tabindex="2">
    <div class="error">${ERRORS.login?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td class="required" width="60">P�ezd�vka</td>
   <td>
    <input type="text" name="nick" value="${PARAMS.nick?if_exists}" size="24" tabindex="3">
    <div class="error">${ERRORS.nick?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td class="required" width="160">Heslo</td>
   <td>
    <input type="password" name="password" size="16" maxlength="12" tabindex="4">
    <div class="error">${ERRORS.password?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td class="required" width="160">Zopakujte heslo</td>
   <td>
    <input type="password" name="password2" size="16" tabindex="5">
   </td>
  </tr>
  <tr>
   <td class="required" width="60">Email</td>
   <td>
    <input type="text" name="email" value="${PARAMS.email?if_exists}" size="16" tabindex="6">
    <div class="error">${ERRORS.email?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td class="required" width="60">Va�e pohlav�</td>
   <td>
    <select name="sex" tabindex="7">
     <#assign sex=PARAMS.sex?default("man")>
     <option value="man" <#if sex=="man">SELECTED</#if>>mu�</option>
     <option value="woman"<#if sex=="woman">SELECTED</#if>>�ena</option>
    </select>
    <div class="error">${ERRORS.sex?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="60">T�denn� souhrn</td>
   <td>
    <select name="weekly" tabindex="8">
     <#assign weekly=PARAMS.weekly?default("no")>
     <option value="yes" <#if weekly=="yes">SELECTED</#if>>ano</option>
     <option value="no"<#if weekly=="no">SELECTED</#if>>ne</option>
    </select>
   </td>
  </tr>
  <tr>
   <td width="60">M�s��n� zpravodaj</td>
   <td>
    <select name="monthly" tabindex="9">
     <#assign monthly=PARAMS.monthly?default("no")>
     <option value="yes" <#if monthly=="yes">SELECTED</#if>>ano</option>
     <option value="no"<#if monthly=="no">SELECTED</#if>>ne</option>
    </select>
   </td>
  </tr>
  <tr>
   <td width="60">&nbsp;</td>
   <td><input type="submit" value="Dokon�i" tabindex="10"></td>
  </tr>
 </table>
 <input type="hidden" name="action" value="register2">
</form>


<#include "../footer.ftl">
