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

<form action="${URL.make("/EditDiscussion")}" method="POST" name="form" enctype="multipart/form-data">
    <table class="siroka" cellpadding="5">
        <#if ! USER??>
            <tr>
                <td class="required">Zadejte vaše jméno</td>
                <td>
                    <input tabindex="4" type="text" size="30" name="author" value="${PARAMS.author!}">
                    <div class="error">${ERRORS.author!}</div><br>
                    nebo <a href="/Profile?action=login">se přihlašte</a>.
                </td>
            </tr>
            <#if ! USER_VERIFIED!false>
                <tr>
                    <td class="required">Aktuální rok</td>
                    <td>
                        <input type="text" size="4" name="antispam" value="${PARAMS.antispam!?html}" tabindex="4">
                        <a class="info" href="#">?<span class="tooltip">Vložte aktuální rok. Jedná se o ochranu před spamboty.
                        Po úspěšném ověření se uloží cookie (včetně vašeho jména) a tato kontrola přestane být prováděna.</span></a>
                        <div class="error">${ERRORS.antispam!}</div>
                    </td>
                </tr>
            </#if>
        </#if>
        <tr>
            <td class="required">Titulek</td>
            <td>
                <input tabindex="4" type="text" name="title" size="40" maxlength="70" value="${PARAMS.title!?html}">
                <div class="error">${ERRORS.title!}</div>
            </td>
        </tr>
        <tr>
            <td class="required">Dotaz</td>
            <td>
                <@lib.showError key="text"/>
                <@lib.showRTEControls "text"/>
                <textarea tabindex="5" name="text" id="text" class="siroka" rows="20">${PARAMS.text!?html}</textarea>
            </td>
        </tr>
        <tr>
            <td valign="top">Příloha</td>
            <td>
                Vložení přílohy: <input type="file" name="attachment" tabindex="6">
                <@lib.showHelp>Například výpis logu, konfigurační soubor, snímek obrazovky a podobně.</@lib.showHelp>
                <@lib.showError key="attachment" />
                <#if ATTACHMENTS??>
                    <ul>
                        <#list ATTACHMENTS as file>
                            <li>${file.name} (${file.size} bytů)  | <label><input type="checkbox" name="rmAttachment" value="${file_index}">Smazat</label></li>
                        </#list>
                    </ul>
                </#if>
            </td>
        </tr>
        <tr>
            <td>&nbsp;</td>
            <td>
                <input tabindex="7" type="submit" name="preview" value="Zopakuj náhled dotazu">
                <input tabindex="8" type="submit" name="finish" value="Dokonči">
            </td>
        </tr>
    </table>
    <input type="hidden" name="action" value="addQuez2">
    <input type="hidden" name="rid" value="${PARAMS.rid}">
</form>

<#include "/include/napoveda-k-auto-formatovani.txt">

<#include "../footer.ftl">
