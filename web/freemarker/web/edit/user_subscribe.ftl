<#include "../header.ftl">

<@lib.showMessages/>

<p>N� port�l pro v�s p�ipravil dv� atraktivn�
slu�by. Prvn� z nich je T�denn� souhrn a je
ur�en t�m, kte�� nemaj� �as denn� n�s nav�t�vovat,
ale necht�j� p�ij�t o ��dn� na�e �l�nky �i zpr�vi�ky.
Pokud si jej p�ihl�s�te, ka�d� v�kend v�m za�leme emailem seznam
�l�nk� a v�echny zpr�vi�ky, kter� jsme dan� t�den vydali.
</p>

<p>Dal�� slu�bou je Zpravodaj port�lu AbcLinuxu.cz.
Pokud si jej p�ihl�s�te, za��tkem ka�d�ho m�s�ce
obdr��te email se spoustou zaj�mavost� ze sv�ta
Linuxu i z na�eho port�lu.
</p>

<p>Novinkou v testovac�m re�imu je emailov� rozhran�
k diskusn�mu f�ru. Pro ka�d� nov� p��sp�vek diskuse
um�st�n� v n�kter�m f�ru se ode�le v�em p�ihl�en�m
u�ivatel�m email s jeho obsahem a adresou, na kter� je
mo�n� odpov�d�t.
</p>

<p>Pro va�i ochranu nejd��ve zadejte sou�asn� heslo.
</p>

<form action="${URL.noPrefix("/EditUser")}" method="POST">
 <table width="100%" border=0 cellpadding=5>
  <tr>
   <td class="required" width="60">Heslo</td>
   <td>
    <input type="password" name="PASSWORD" size="16" tabindex="1" class="pole">
    <div class="error">${ERRORS.PASSWORD?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="60">T�denn� souhrn</td>
   <td>
    <select name="weekly" tabindex="2">
     <#assign weekly=PARAMS.weekly?if_exists>
     <option value="yes" <#if weekly=="yes">SELECTED</#if>>ano</option>
     <option value="no"<#if weekly=="no">SELECTED</#if>>ne</option>
    </select>
   </td>
  </tr>
  <tr>
   <td width="60">M�s��n� zpravodaj</td>
   <td>
    <select name="monthly" tabindex="3">
     <#assign monthly=PARAMS.monthly?if_exists>
     <option value="yes" <#if monthly=="yes">SELECTED</#if>>ano</option>
     <option value="no"<#if monthly=="no">SELECTED</#if>>ne</option>
    </select>
   </td>
  </tr>
  <tr>
   <td width="60">Diskusn� f�rum</td>
   <td>
    <select name="forum" tabindex="4">
     <#assign forum=PARAMS.forum?if_exists>
     <option value="yes" <#if forum=="yes">SELECTED</#if>>ano</option>
     <option value="no"<#if forum=="no">SELECTED</#if>>ne</option>
    </select>
   </td>
  </tr>
  <tr>
   <td width="60">&nbsp;</td>
   <td><input type="submit" value="Dokon�i" tabindex="5" class="buton"></td>
  </tr>
 </table>
 <input type="hidden" name="action" value="subscribe2">
 <input type="hidden" name="uid" value="${MANAGED.id}">
</form>


<#include "../footer.ftl">
