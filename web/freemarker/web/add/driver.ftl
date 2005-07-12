<#include "../header.ftl">

<@lib.showMessages/>

<h1 class="st_nadpis">Úvod</h1>

<p>Právì se chystáte vlo¾it do databáze nový ovladaè. Pokud jste si
jisti, ¾e tento ovladaè v databázi chybí, pokraèujte
a vyplòte tento formuláø. Pokud jste se ale nedívali, prosím
vra»te se zpìt a zkontrolujte, zda jej nepøidal nìkdo pøed vámi.
V tom pøípadì jej otevøete a pro aktualizaci zvolte odkaz <i>Vlo¾ novou verzi</i>.
</p>

<p>Sekce Ovladaèù slou¾í pro shroma¾ïování informací ohlednì ovladaèù,
které nejsou standardní souèástí jádra. Jedná se buï o Open Source
projekty vytváøené a udr¾ované komunitou, nebo o proprietární ovladaèe
s uzavøeným kódem, vyvíjené obvykle výrobcem.
</p>

<p>Vytvoøení polo¾ky ovladaèe je snadné. Nejdøíve vyplòte jméno
ovladaèe, pøípadnì hardwaru. Pak vlo¾te verzi ovladaèe a adresu,
odkud je mo¾né jej stáhnout. Poslední polo¾kou je poznámka,
kam patøí informace o schopnostech ovladaèe, èi zmìnách oproti minulé
verzi.
</p>

<#if PARAMS.preview?exists>
 <h1 class="st_nadpis">Náhled pøíspìvku</h1>

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

<h1 class="st_nadpis">Nový ovladaè</h1>

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
