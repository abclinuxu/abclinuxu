<#include "../header.ftl">

<@lib.showMessages/>

<p>
Dìkujeme vám, ¾e jste se rozhodli zaregistrovat
se na na¹em portálu. Vìøíme, ¾e nabízené výhody
vyvá¾í pár minut strávených registrací. Jako
malý bonus získáte vlastní domovskou stránku,
na které mù¾ete prezentovat informace o své osobì.
Zároveò v ní mù¾ete najít své diskuse, èlánky, zprávièky
èi záznamy.
</p>

<p>
Pøihla¹ovací jméno (login) musí mít nejménì tøi znaky,
maximálnì 16 znakù a to pouze písmena A a¾ Z, èíslice,
pomlèku, teèku nebo podtr¾ítko.
Login vás jednoznaènì identifikuje v systému,
proto není mo¾né pou¾ívat hodnotu, které ji¾ pou¾il
nìkdo pøed vámi.
</p>

<p>Va¹e jméno musí být nejménì pìt znakù dlouhé. Email musí být platný.
Nebojte se, budeme jej chránit pøed spammery a my vám budeme
zasílat jen ty informace, které si sami objednáte.
Po registraci vám za¹leme email s informacemi
o va¹em úètì, který si peèlivì uschovejte.
</p>

<p>Na této stránce si mù¾ete také objednat dvì atraktivní
slu¾by. První z nich je týdenní souhrn èlánkù a zprávièek a je
urèen tìm, kteøí nemají èas dennì nás nav¹tìvovat. Pokud si jej
pøihlásíte, ka¾dý víkend vám za¹leme emailem seznam
nových èlánkù a zprávièek. Druhou slu¾bou je Zpravodaj, který vychází
zaèátkem ka¾dého mìsíce. Tento email obsahuje spoustu
zajímavostí ze svìta Linuxu i z na¹eho portálu.
</p>

<form action="${URL.noPrefix("/EditUser")}" method="POST">
 <table width="100%" border=0 cellpadding=5>
  <tr>
   <td class="required" width="60">Jméno</td>
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
   <td class="required" width="60">Pøezdívka</td>
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
   <td class="required" width="60">Va¹e pohlaví</td>
   <td>
    <select name="sex" tabindex="7">
     <#assign sex=PARAMS.sex?default("man")>
     <option value="man" <#if sex=="man">SELECTED</#if>>mu¾</option>
     <option value="woman"<#if sex=="woman">SELECTED</#if>>¾ena</option>
    </select>
    <div class="error">${ERRORS.sex?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="60">Týdenní souhrn</td>
   <td>
    <select name="weekly" tabindex="8">
     <#assign weekly=PARAMS.weekly?default("no")>
     <option value="yes" <#if weekly=="yes">SELECTED</#if>>ano</option>
     <option value="no"<#if weekly=="no">SELECTED</#if>>ne</option>
    </select>
   </td>
  </tr>
  <tr>
   <td width="60">Mìsíèní zpravodaj</td>
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
   <td><input type="submit" value="Dokonèi" tabindex="10"></td>
  </tr>
 </table>
 <input type="hidden" name="action" value="register2">
</form>


<#include "../footer.ftl">
