<#include "../header.ftl">

<@lib.showMessages/>

<p>Opravdu si p�ejete vytvo�it n�sleduj�c� kategorii?</p>

<form action="${URL.make("/blog/edit/"+REL_BLOG.id)}" method="POST" name="form">
    <input type="text" name="category" value="${PARAMS.category?if_exists}" size="20">
    <input type="submit" name="finish" value="Dokon�i">
    <div class="error">${ERRORS.category?if_exists}</div>
    <input type="hidden" name="action" value="addCategory2">
</form>


<#include "../footer.ftl">
