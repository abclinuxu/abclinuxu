<@lib.addRTE textAreaId="text" formId="form" menu="comment" />
<#include "../header.ftl">

<@lib.showMessages/>

<#if !RELATION.child.subType?? || RELATION.child.subType!="subportal">
  <h1>Náhled dotazu</h1>

  <p>
      Nyní si prohlédněte vzhled vašeho dotazu. Zkontrolujte
      si pravopis, obsah i tón vašeho textu. Uvědomte si, že
      toto není placená technická podpora, ale dobrovolná
      a neplacená práce ochotných lidí. Pokud se vám text nějak nelíbí,
      opravte jej a zvolte <tt>Náhled&nbsp;dotazu</tt>. Pokud jste s ním spokojeni,
      zvolte <tt>Dokonči</tt>.
  </p>

  <#if PREVIEW??>
      <h2>Náhled vašeho dotazu</h2>
      <@lib.showThread PREVIEW, 0, TOOL.createEmptyDiscussion(), false />
  </#if>
<#else>
  <p>
    Prohlédněte si vzhled vašeho komentáře. Zkontrolujte
    pravopis, obsah i tón vašeho textu. Někdy to vážně
    chce chladnou hlavu. Opravte chyby a zvolte tlačítko <code>Náhled&nbsp;komentáře</code>.
    Pokud jste s příspěvkem spokojeni, stiskněte tlačítko <code>Dokonči</code>.
  </p>

  <#if PREVIEW??>
      <h2>Náhled vašeho přispěvku</h2>
      <@lib.showThread PREVIEW, 0, TOOL.createEmptyDiscussion(), false />
  </#if>
</#if>

<h2>Zde můžete provést své úpravy</h2>

<@lib.addForm URL.make("/EditDiscussion"), "name='form'", true>
    <#if ! USER??>
        <@lib.addInput true, "author", "Zadejte vaše jméno", 30>
            <br/> nebo <a href="/Profile?action=login">se přihlašte</a>.
        </@lib.addInput>

        <#if ! USER_VERIFIED!false>
            <@lib.addFormField true, "Zadejte aktuální rok", "Vložte aktuální rok. Jedná se o ochranu před spamboty. Po úspěšném ověření "+
                "se uloží cookie (včetně vašeho jména) a tato kontrola přestane být prováděna.">
                    <@lib.addInputBare "antispam", 4 />
            </@lib.addFormField>
        </#if>
    </#if>

    <@lib.addInput true, "title", "Titulek", 60 />
    <@lib.addTextArea true, "text", "Váš komentář", 20>
        <@lib.showRTEControls "text"/>
    </@lib.addTextArea>

    <@lib.addFormField true, "Vložení přílohy", "Například výpis logu, konfigurační soubor, snímek obrazovky a podobně.">
        <@lib.addFileBare "attachment" />
        <#if ATTACHMENTS??>
            <ul>
                <#list ATTACHMENTS as file>
                    <li>${file.name} (${file.size} bytů) | <label><input type="checkbox" name="rmAttachment" value="${file_index}">Smazat</label></li>
                </#list>
            </ul>
        </#if>
    </@lib.addFormField>

    <@lib.addFormField>
        <@lib.addSubmitBare "Zopakuj náhled dotazu", "preview" />
        <@lib.addSubmitBare "Dokonči", "finish" />
    </@lib.addFormField>

    <@lib.addHidden "action", "addQuez2" />
    <@lib.addHidden "rid", PARAMS.rid />
</@lib.addForm>

<#include "/include/napoveda-k-auto-formatovani.txt">

<#include "../footer.ftl">
