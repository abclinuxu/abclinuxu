<@lib.addRTE textAreaId="note" formId="form" menu="wiki" />
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

<@lib.addForm URL.make("/edit"), "name='form'">
    <@lib.addInput true, "name", "Jméno", 30 />
    <@lib.addSelect true, "category", "Kategorie">
        <#list CATEGORIES as category>
            <@lib.addOption "category", category.name, category.key />
        </#list>
    </@lib.addSelect>
    <@lib.addInput true, "version", "Verze" />
    <@lib.addInput true, "url", "URL", 70, "", "http://" />
    <@lib.addTextArea true, "note", "Poznámka", 20>
        <@lib.showRTEControls "note"/>
    </@lib.addTextArea>

    <@lib.addFormField>
        <@lib.addSubmitBare "Náhled", "preview" />
        <@lib.addSubmitBare "Dokonči", "finish" />
    </@lib.addFormField>

    <@lib.addHidden "action", "add2" />
</@lib.addForm>

<#include "../footer.ftl">
