<#include "../header.ftl">

<@lib.showMessages/>

<p>Chystáte se smazat níže zobrazený komentář. Jedná se o naprosto
ojedinělou akci, kterou byste měl používat jen ve výjimečných případech.
V drtivé většině případů byste měli použít cenzuru. Použít jej můžete
například na komentáře nějakého šílence (ještírci, paranoici),
zakázanou reklamu a spamy a podobně.</p>

<p>Tato funkce je rekurzivní! Smaže tedy kompletní vlákno
včetně všech potomků!</p>

<form action="${URL.make("/EditDiscussion")}" method="POST">
 <input type="submit" value="Smazat">
 <input type="hidden" name="action" value="rm2">
 <input type="hidden" name="rid" value="${RELATION.id}">
 <input type="hidden" name="dizId" value="${PARAMS.dizId}">
 <input type="hidden" name="threadId" value="${PARAMS.threadId}">
</form>

<#if THREAD?exists>
 <h1>Náhled příspěvku</h1>
 <@lib.showThread THREAD, 0, TOOL.createEmptyDiscussionWithAttachments(DISCUSSION), false />
</#if>


<#include "../footer.ftl">
