<#include "../header.ftl">

<@lib.showMessages/>

<h2>Úvod</h2>

<p>Tato stránka je určena výhradně administrátorům. Jejím účelem
není provádět cenzuru (na to jsou jiné nástroje) nebo
zasahovat do smyslu komentáře, nýbrž opravovat chyby uživatelů.
Například špatně zvolený titulek u dotazu, titulek porušující
zásady (psán velkými písmeny apod.), nevhodné HTML značky, ...</p>

<#if PREVIEW??>
 <h2>Náhled příspěvku</h2>
 <@lib.showThread PREVIEW, 0, TOOL.createEmptyDiscussion(), false />
</#if>

<h2>Zde můžete provést své úpravy</h2>

<@lib.addForm URL.make("/EditDiscussion"), "name='editForm'">
    <@lib.addInput true, "title", "Titulek", 40 />
    <@lib.addInput false, "author_id", "Autor (ID)", 5>
        <div>Jen ve výjimečných případech (sloučení dvou kont)!</div>
    </@lib.addInput>
    <@lib.addInput false, "author", "Autor", 30>
        <div>Jen ve výjimečných případech!</div>
    </@lib.addInput>
    <@lib.addTextArea true, "text", "Komentář", 20>
        <@lib.addTextAreaEditor "text">
            <#if THREAD??>
                <a href="javascript:cituj(document.editForm.text);" id="mono" title="Vloží komentovaný příspěvek jako citaci">Citace</a>
            </#if>
        </@lib.addTextAreaEditor>
    </@lib.addTextArea>

    <@lib.addFormField>
        <@lib.addSubmitBare "Náhled", "preview" />
        <@lib.addSubmitBare "Dokonči", "finish" />
    </@lib.addFormField>

    <@lib.addHidden "action", "edit2" />
    <@lib.addHidden "rid", RELATION.id />
    <@lib.addHidden "dizId", PARAMS.dizId />
    <@lib.addHidden "threadId", PARAMS.threadId />
</@lib.addForm>

<#include "../footer.ftl">
