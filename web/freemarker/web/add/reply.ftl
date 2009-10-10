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

<form action="${URL.make("/EditDiscussion")}" method="POST" name="replyForm" enctype="multipart/form-data">
  <#if ! USER??>
   <p>
    <span class="required">Zadejte vaše jméno</span>
    <input tabindex="1" type="text" size="30" name="author" value="${PARAMS.author!?html}">
    <span class="error">${ERRORS.author!}</span><br>
        nebo <a href="/Profile?action=login">se přihlašte</a>.
   </p>
   <#if ! USER_VERIFIED!false>
       <p>
           <span class="required">Zadejte aktuální rok</span>
           <input type="text" size="4" name="antispam" value="${PARAMS.antispam!?html}" tabindex="2">
           <@lib.showHelp>Vložte aktuální rok. Jedná se o ochranu před spamboty. Po úspěšném ověření
           se uloží cookie (včetně vašeho jména) a tato kontrola přestane být prováděna.</@lib.showHelp>
           <@lib.showError key="antispam" />
       </p>
   </#if>
  </#if>
  <p>
   <span class="required">Titulek</span><br>
    <#if PARAMS.title??>
        <#assign title=PARAMS.title>
    <#elseif PARENT_TITLE??>
        <#assign title=PARENT_TITLE>
        <#assign title="Re: "+title>
    <#elseif THREAD??>
        <#assign title=THREAD.title!>
        <#if !title.startsWith("Re: ")><#assign title="Re: "+title></#if>
    </#if>
   <input tabindex="3" type="text" name="title" size="60" maxlength="70" value="${title!?html}">
   <div class="error">${ERRORS.title!}</div>
  </p>
    <div>
        <span class="required">Váš komentář</span>
        <@lib.showRTEControls "text"/>
        <@lib.showError key="text"/>
        <textarea tabindex="4" name="text" id="text" class="siroka" rows="20">${PARAMS.text!?html}</textarea>
    </div>
    <p>
        Vložení přílohy: <input type="file" name="attachment" tabindex="5">
        <@lib.showHelp>Například výpis logu, konfigurační soubor, snímek obrazovky a podobně.</@lib.showHelp>
        <@lib.showError key="attachment" />
        <#if ATTACHMENTS??>
            <ul>
                <#list ATTACHMENTS as file>
                    <li>${file.name} (${file.size} bytů) | <label><input type="checkbox" name="rmAttachment" value="${file_index}">Smazat</label></li>
                </#list>
            </ul>
        </#if>
    </p>
  <p>
    <#if PREVIEW??>
     <input tabindex="6" type="submit" name="preview" value="Zopakuj náhled komentáře">
     <input tabindex="7" type="submit" name="finish" value="Dokonči">
    <#else>
     <input tabindex="6" type="submit" name="preview" value="Náhled komentáře">
    </#if>
  </p>

 <input type="hidden" name="action" value="add2">
 <input type="hidden" name="rid" value="${RELATION.id}">
 <input type="hidden" name="dizId" value="${DISCUSSION.id}">
 <#if THREAD??>
  <input type="hidden" name="threadId" value="${THREAD.id}">
 </#if>
 <#if PARAMS.url??>
  <input type="hidden" name="url" value="${PARAMS.url}">
 </#if>
</form>

<#include "/include/napoveda-k-auto-formatovani.txt">

<#include "../footer.ftl">
