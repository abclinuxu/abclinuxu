<#if COMMENTED_TEXT??>
    <@lib.addRTE textAreaId="text" formId="replyForm" menu="comment" commentedText="${COMMENTED_TEXT}" />
<#else>
    <@lib.addRTE textAreaId="text" formId="replyForm" menu="comment" />
</#if>
<#include "../header.ftl">

<@lib.showMessages/>

<#if PREVIEW??>
 <p>
    Prohlédněte si vzhled vašeho komentáře. Zkontrolujte
    pravopis, obsah i tón vašeho textu. Někdy to vážně
    chce chladnou hlavu. Opravte chyby a zvolte tlačítko <code>Náhled&nbsp;komentáře</code>.
    Pokud jste s příspěvkem spokojeni, stiskněte tlačítko <code>Dokonči</code>.
 </p>
</#if>

<#if THREAD??>
 <h2>Příspěvek, na který reagujete</h2>
 <@lib.showThread THREAD, 0, TOOL.createEmptyDiscussionWithAttachments(DISCUSSION), false />
</#if>

<#if PREVIEW??>
 <h2>Náhled vašeho příspěvku</h2>
 <div style="padding-left: 30pt">
  <@lib.showThread PREVIEW, 0, TOOL.createEmptyDiscussion(), false />
 </div>
</#if>

<h2>Váš komentář</h2>

<@lib.addForm URL.make("/EditDiscussion"), "name='replyForm'", true>
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

    <#if PARENT_TITLE??>
        <#assign title=PARENT_TITLE>
        <#assign title="Re: "+title>
    <#elseif THREAD??>
        <#assign title=THREAD.title!>
        <#if !title.startsWith("Re: ")><#assign title="Re: "+title></#if>
    </#if>

    <@lib.addInput true, "title", "Titulek", 60, "", title! />
    <@lib.addTextArea true, "text", "Váš komentář", 20, "class='siroka'">
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
        <#if PREVIEW??>
            <@lib.addSubmitBare "Zopakuj náhled komentáře", "preview" />
            <@lib.addSubmitBare "Dokonči", "finish" />
        <#else>
            <@lib.addSubmitBare "Náhled komentáře", "preview" />
        </#if>
    </@lib.addFormField>

    <@lib.addHidden "action", "add2" />
    <@lib.addHidden "rid", RELATION.id />
    <@lib.addHidden "dizId", DISCUSSION.id />

    <#if THREAD??>
        <@lib.addHidden "threadId", THREAD.id />
    </#if>
    <#if PARAMS.url??>
        <@lib.addHidden "url", PARAMS.url />
    </#if>

</@lib.addForm>

<#include "/include/napoveda-k-auto-formatovani.txt">

<#include "../footer.ftl">
