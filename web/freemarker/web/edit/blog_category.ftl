<#include "../header.ftl">

<@lib.showMessages/>

<p>Na této stránce mù¾ete pøejmenovat zvolenou kategorii.</p>

<form action="${URL.make("/blog/edit/"+REL_BLOG.id)}" method="POST" name="form">
    <input type="text" name="category" value="${PARAMS.category?if_exists}" size="20">
    <input type="submit" name="finish" value="Dokonèi">
    <div class="error">${ERRORS.category?if_exists}</div>
    <input type="hidden" name="action" value="editCategory2">
    <input type="hidden" name="cid" value="${PARAMS.cid}">
</form>


<#include "../footer.ftl">
