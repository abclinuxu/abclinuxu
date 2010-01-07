<@lib.addRTE textAreaId="note" formId="form" menu="wiki" />
<#include "../header.ftl">

<@lib.showMessages/>

<h1>Úprava ovladače</h1>

<p>
    Pokud vyšla nová verze ovladače nebo jste našli nějakou chybku
    v popisu ovladače, tento formulář slouží pro  zadání úprav.
    V případě nové verze se nebojte smazat celý text a opravdu popsat
    jen novinky oproti minulé verzi, v historii změn si čtenář může
    přečíst popisy předchozích verzí. Pokud má ovladač více vývojových
    řad, do políčka verze dávejte jen číslo aktuální řady.
</p>

<#if PARAMS.preview??>
 <h2>Náhled příspěvku</h2>

 <table cellspacing=0 border=1 cellpadding=5 align="center">
  <tr>
    <td>Jméno</td><td>${PARAMS.name!}</td>
  </tr>
  <tr>
    <td>Kategorie ovladače</td><td><#if CATEGORY??>${CATEGORY.name}</#if></td>
  <tr>
  <tr>
    <td>Verze</td><td>${PARAMS.version!}</td>
  </tr>
  <tr>
    <td>Adresa</td>
    <td>
      <a href="${PARAMS.url!?html}">${TOOL.limit(PARAMS.url!,50," ..")?html}</a>
    </td>
  </tr>
  <tr>
    <td valign="top">Poznámka</td><td>${TOOL.render(PARAMS.note!,USER!)}</td>
  </tr>
 </table>
</#if>

<h2>Zde zadejte své úpravy</h2>

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

    <@lib.addFormField false, "Popis změny", "Text bude zobrazen v historii dokumentu">
        <@lib.addInputBare "rev_descr" />
    </@lib.addFormField>

    <@lib.addFormField>
        <@lib.addSubmitBare "Náhled", "preview" />
        <@lib.addSubmitBare "Dokonči", "finish" />
    </@lib.addFormField>

    <@lib.addHidden "action", "edit2" />
    <@lib.addHidden "rid", RELATION.id />
</@lib.addForm>

<#include "../footer.ftl">
