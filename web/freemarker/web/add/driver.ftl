<#include "../header.ftl">

<@lib.showMessages/>

<h1>Úvod</h1>

<p>Právì se chystáte vlo¾it do databáze nový ovladaè. Pokud jste si
jisti, ¾e v databázi není ulo¾ena jeho star¹í verze, pokraèujte
a vyplòte tento formuláø. Pokud jste se ale nedívali, prosím
vra»te se zpìt a zkontrolujte, zda ji¾ v databázi není pøítomen.
Pokud jej najdete, otevøete jej a zvolte odkaz <i>Vlo¾ novou verzi</i>.
</p>

<p>Polo¾ka ovladaèe slou¾í pro shroma¾ïování informací ohlednì ovladaèù,
které nejsou standardní souèástí jádra. Typicky jde buï o Open Source
projekty, kdy se nad¹enci sna¾í vytvoøit podporu pro daný hardware
(typicky ovladaèe scannerù) nebo výrobce odmítá uvolnit specifikaci
svých produktù komunitì a místo toho vyrábí vlastní ovladaèe (napøíklad
nVidia).
</p>

<p>Vytvoøení polo¾ky ovladaèe je snadné. Nejdøíve vyplòte jméno
ovladaèe, pøípadnì hardwaru. Pak vlo¾te verzi ovladaèe a adresu,
kde je mo¾né stáhnout ovladaè. Nedávejte zde základní adresu produktu,
nýbr¾ adresu, kde je mo¾né stáhnout jeho ovladaè. Poslední
polo¾kou je poznámka, kam patøí informace o tom, co ovladaè umí,
respektive jaké zmìny tato verze pøinesla.
</p>

<#if PARAMS.preview?exists>
 <h1>Náhled pøíspìvku</h1>

 <table cellspacing=0 border=1 cellpadding=5 align="center">
  <tr>
    <td>Jméno ovladaèe</td><td>${PARAMS.name?if_exists}</td>
  </tr>
  <tr>
    <td>Verze ovladaèe</td><td>${PARAMS.version?if_exists}</td>
  </tr>
  <tr>
    <td>URL ovladaèe</td>
    <td>
      <a href="${PARAMS.url?if_exists}">${TOOL.limit(PARAMS.url?if_exists,50," ..")}</a>
    </td>
  </tr>
  <tr>
    <td valign="top">Poznámka</td><td>${TOOL.render(PARAMS.note?if_exists,USER?if_exists)}</td>
  </tr>
 </table>
</#if>

<h1>Nový ovladaè</h1>

<form action="${URL.make("/edit")}" method="POST">
 <table cellpadding="0" border="0" width="100%">
  <tr>
   <td class="required">Jméno ovladaèe</td>
   <td>
    <input type="text" name="name" value="${PARAMS.name?if_exists}" size="30" maxlength="30" tabindex="1">
    <div class="error">${ERRORS.name?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td class="required">Verze ovladaèe</td>
   <td>
    <input type="text" name="version" value="${PARAMS.version?if_exists}" size="30" tabindex="2">
    <div class="error">${ERRORS.version?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td class="required">URL ovladaèe</td>
   <td>
    <input type="text" name="url" value="${PARAMS.url?default("http://")}" size="50" tabindex="3">
    <div class="error">${ERRORS.url?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td colspan="2" class="required">Poznámka</td>
  </tr>
  <tr>
   <td colspan="2">
    <textarea name="note" cols="70" rows="15" tabindex="4">${PARAMS.note?if_exists?html}</textarea>
    <div class="error">${ERRORS.note?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td>&nbsp;</td>
   <td>
    <input type="submit" name="preview" value="Náhled">
    <input type="submit" name="submit" value="Dokonèi">
   </td>
  </tr>
 </table>
 <input type="hidden" name="action" value="add2">
</form>


<#include "../footer.ftl">
