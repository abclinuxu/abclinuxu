<#include "../header.ftl">

<@lib.showMessages/>

<p>Na t�to str�nce m��ete p�ejmenovat zvolenou kategorii.</p>

<form action="${URL.make("/blog/edit/"+REL_BLOG.id)}" method="POST" name="form">
    <input type="text" name="category" value="${PARAMS.category?if_exists}" size="20">
    <input type="submit" name="finish" value="Dokon�i">
    <div class="error">${ERRORS.category?if_exists}</div>
    <input type="hidden" name="action" value="editCategory2">
    <input type="hidden" name="cid" value="${PARAMS.cid}">
</form>


<#include "../footer.ftl">
