<#include "../header.ftl">

<@lib.showMessages/>

<p>
Děkujeme vám, že jste se rozhodli zaregistrovat
se na našem portálu. Věříme, že nabízené výhody
vyváží pár minut strávených registrací. Jako
malý bonus získáte vlastní domovskou stránku,
na které můžete prezentovat informace o své osobě.
Zároveň v ní můžete najít své diskuse, články, zprávičky
či záznamy.
</p>

<p>
Přihlašovací jméno (login) musí mít nejméně tři znaky,
maximálně 16 znaků a to pouze písmena A až Z, číslice,
pomlčku, tečku nebo podtržítko.
Login vás jednoznačně identifikuje v systému,
proto není možné používat hodnotu, které již použil
někdo před vámi.
</p>

<p>Vaše jméno musí být nejméně pět znaků dlouhé. Email musí být platný.
Nebojte se, budeme jej chránit před spammery a my vám budeme
zasílat jen ty informace, které si sami objednáte.
Po registraci vám zašleme email s informacemi
o vašem účtě, který si pečlivě uschovejte.
</p>

<p>Na této stránce si můžete také objednat dvě atraktivní
služby. První z nich je týdenní souhrn článků a zpráviček a je
určen těm, kteří nemají čas denně nás navštěvovat. Pokud si jej
přihlásíte, každý víkend vám zašleme emailem seznam
nových článků a zpráviček. Druhou službou je Zpravodaj, který vychází
začátkem každého měsíce. Tento email obsahuje spoustu
zajímavostí ze světa Linuxu i z našeho portálu.
</p>

<form action="${URL.noPrefix("/EditUser")}" method="POST">
 <table class="siroka" border="0" cellpadding=5>
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
    <input type="text" name="login" id="login" value="${PARAMS.login?if_exists}" size="24" tabindex="2"
           onChange="new Ajax.Updater('loginError', '/ajax/checkLogin', {parameters: { value : $F('login')}})">
    <div class="error" id="loginError">${ERRORS.login?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td class="required" width="60">Přezdívka</td>
   <td>
    <input type="text" name="nick" id="nick" value="${PARAMS.nick?if_exists}" size="24" tabindex="3"
           onChange="new Ajax.Updater('nickError', '/ajax/checkNick', {parameters: { value : $F('nick')}})">
    <div class="error" id="nickError">${ERRORS.nick?if_exists}</div>
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
   <td class="required" width="60">Vaše pohlaví</td>
   <td>
    <select name="sex" tabindex="7">
     <#assign sex=PARAMS.sex?default("man")>
     <option value="man" <#if sex=="man">SELECTED</#if>>muž</option>
     <option value="woman"<#if sex=="woman">SELECTED</#if>>žena</option>
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
   <td width="60">Měsíční zpravodaj</td>
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
   <td><input type="submit" value="Dokonči" tabindex="10"></td>
  </tr>
 </table>
 <input type="hidden" name="action" value="register2">
</form>


<#include "../footer.ftl">
