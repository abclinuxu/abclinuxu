<#include "../header.ftl">

<@lib.showMessages/>

<h2>Cenzura komentáře</h2>

 <@lib.showThread THREAD, 0, TOOL.createEmptyDiscussionWithAttachments(DISCUSSION), false />

<p>Napište zde důvod, proč cenzurujete tento příspěvek.</p>

<@lib.addForm URL.make("/EditDiscussion")>
    <@lib.addTextArea true, "text", "Komentář", 5 />
    <@lib.addSubmit "Dokonči" />
    <@lib.addHidden "action", "censore2" />
    <@lib.addHidden "rid", RELATION.id />
    <@lib.addHidden "dizId", PARAMS.dizId />
    <@lib.addHidden "threadId", PARAMS.threadId />
</@lib.addForm>

<#include "../footer.ftl">
