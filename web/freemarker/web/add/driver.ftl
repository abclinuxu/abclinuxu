<#import "/web/rte-macro.ftl" as rte>
<@rte.addRTE textAreaId="note" formId="form" inputMode="wiki" />
<#include "../header.ftl">

<@lib.showMessages/>

<h1>Databáze ovladačů</h1>

<p>Právě se chystáte vložit do databáze nový ovladač. Pokud jste si
jisti, že tento ovladač v databázi chybí, pokračujte
a vyplňte tento formulář. Pokud jste se ale nedívali, prosím
vraťte se <a href="/ovladace">zpět</a> a zkontrolujte, zda jej nepřidal někdo před vámi.
V tom případě jej otevřete a pro aktualizaci zvolte odkaz <i>Upravit</i>.
</p>

<p>Vytvoření položky ovladače je snadné. Nejdříve vyplňte jméno
ovladače, případně hardwaru. Ovladače jsou zařazeny do kategorií
podle typu hardwaru - pokud není v seznamu žádná vhodná kategorie,
vyberte položku "Bez kategorie". Pak vložte verzi ovladače a adresu,
odkud je možné jej stáhnout. Poslední položkou je poznámka,
kam patří informace o schopnostech ovladače, či změnách oproti minulé
verzi.
</p>

<#if PARAMS.preview??>
 <h2>Náhled příspěvku</h2>

 <table cellspacing=0 border=1 cellpadding=5 align="center">
  <tr>
    <td>Jméno ovladače</td><td>${PARAMS.name!}</td>
  </tr>
  <tr>
    <td>Kategorie ovladače</td><td><#if CATEGORY??>${CATEGORY.name}</#if></td>
  <tr>
    <td>Verze ovladače</td><td>${PARAMS.version!}</td>
  </tr>
  <tr>
    <td>URL ovladače</td>
    <td>
      <a href="${PARAMS.url!}">${TOOL.limit(PARAMS.url!,50," ..")}</a>
    </td>
  </tr>
  <tr>
    <td valign="top">Poznámka</td><td>${TOOL.render(PARAMS.note!,USER!)}</td>
  </tr>
 </table>
</#if>

<h2>Nový ovladač</h2>

<form action="${URL.make("/edit")}" method="POST" name="form">
 <table cellpadding="0" border="0" style="margin-top: 1em;" class="siroka">
  <tr>
   <td class="required">Jméno</td>
   <td>
    <input type="text" name="name" value="${PARAMS.name!}" size="30" maxlength="30" tabindex="1">
    <div class="error">${ERRORS.name!}</div>
   </td>
  </tr>
  <tr>
   <td class="required">Kategorie</td>
   <td>
    <select name="category" tabindex="2">
     <#assign selected = PARAMS.category?default("NONE")>
     <#list CATEGORIES as category>
      <option value="${category.key}"
       <#if category.key=selected>selected</#if> >
       ${category.name}
      </option>
     </#list>
    </select>
   </td>
  </tr>
  <tr>
   <td class="required">Verze</td>
   <td>
    <input type="text" name="version" value="${PARAMS.version!}" size="30" tabindex="3">
    <div class="error">${ERRORS.version!}</div>
   </td>
  </tr>
  <tr>
   <td class="required">URL</td>
   <td>
    <input type="text" name="url" value="${PARAMS.url?default("http://")}" size="50" tabindex="4">
    <div class="error">${ERRORS.url!}</div>
   </td>
  </tr>
  <tr>
   <td colspan="2" class="required">Poznámka</td>
  </tr>
  <tr>
   <td colspan="2">
    <@lib.showError key="note"/>
    <@rte.showFallback "note"/>
    <textarea name="note" class="siroka" rows="20" tabindex="5">${PARAMS.note!?html}</textarea>
   </td>
  </tr>
  <tr>
   <td colspan="2" align="center">
    <input type="submit" name="preview" value="Náhled">
    <input type="submit" name="submit" value="Dokonči">
   </td>
  </tr>
 </table>
 <input type="hidden" name="action" value="add2">
</form>


<#include "../footer.ftl">
