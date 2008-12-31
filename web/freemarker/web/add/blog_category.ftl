<#include "../header.ftl">

<@lib.showMessages/>

<p>Opravdu si přejete vytvořit následující kategorii?</p>

<form action="${URL.make("/blog/edit/"+REL_BLOG.id)}" method="POST" name="form">
    <input type="text" name="category" value="${PARAMS.category!}" size="20">
    <input type="submit" name="finish" value="Dokonči">
    <div class="error">${ERRORS.category!}</div>
    <input type="hidden" name="action" value="addCategory2">
</form>


<#include "../footer.ftl">
