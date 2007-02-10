<#include "../header.ftl">

<@lib.showMessages/>

<h1>Úprava ovladače</h1>

<p>Sekce Ovladačů slouží pro shromažďování informací ohledně ovladačů,
které nejsou standardní součástí jádra. Jedná se buď o Open Source
projekty vytvářené a udržované komunitou, nebo o proprietární ovladače
s uzavřeným kódem, vyvíjené obvykle výrobcem.</p>

<p>Úprava položky ovladače je snadná a díky historii verzí i bezpečná.
Nejdříve můžete změnit název ovladače, je-li nevhodný. Pak zadejte aktuální
verzi ovladače a případně upravte adresu, odkud je možné stáhnout tento ovladač.
Do poznámky vložte informace o novinkách této verze a jiných změnách.
Nebojte se celou poznámku aktualizovat, předchozí text bude dostupný v historii
ovladače.</p>

<#if PARAMS.preview?exists>
 <h2>Náhled příspěvku</h2>

 <table cellspacing=0 border=1 cellpadding=5 align="center">
  <tr>
    <td>Jméno</td><td>${PARAMS.name?if_exists}</td>
  </tr>
  <tr>
    <td>Verze</td><td>${PARAMS.version?if_exists}</td>
  </tr>
  <tr>
    <td>Adresa</td>
    <td>
      <a href="${PARAMS.url?if_exists}">${TOOL.limit(PARAMS.url?if_exists,50," ..")}</a>
    </td>
  </tr>
  <tr>
    <td valign="top">Poznámka</td><td>${TOOL.render(PARAMS.note?if_exists,USER?if_exists)}</td>
  </tr>
 </table>
</#if>

<h2>Zde zadejte své úpravy</h2>

<form action="${URL.make("/edit")}" method="POST">
 <table cellpadding="0" border="0" style="margin-top: 1em;">
  <tr>
   <td class="required">Jméno</td>
   <td>
    <input type="text" name="name" value="${PARAMS.name?if_exists}" size="30" maxlength="30" tabindex="1">
    <div class="error">${ERRORS.name?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td class="required">Verze</td>
   <td>
    <input type="text" name="version" value="${PARAMS.version?if_exists}" size="30" tabindex="2">
    <div class="error">${ERRORS.version?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td class="required">URL</td>
   <td>
    <input type="text" name="url" value="${PARAMS.url?default("http://")}" size="70" tabindex="3">
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
   <td colspan="2" align="center">
    <input type="submit" name="preview" value="Náhled">
    <input type="submit" name="submit" value="Dokonči">
   </td>
  </tr>
 </table>
 <input type="hidden" name="action" value="edit2">
 <input type="hidden" name="rid" value="${RELATION.id}">
</form>


<#include "../footer.ftl">
