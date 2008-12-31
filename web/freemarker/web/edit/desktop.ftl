<#include "../header.ftl">

<@lib.showMessages/>

<h1>Úprava desktopu</h1>

<p>
<form action="${URL.make("/edit")}" method="POST">
  <span class="required">Titulek</span><br />
    <input type="text" name="name" value="${PARAMS.name!?html}" size="40" tabindex="1">
    <div class="error">${ERRORS.name!}</div><br />
  Popis<br />
    <textarea name="desc" rows="5" class="siroka" tabindex="2">${PARAMS.desc!?html}</textarea>
    <div class="error">${ERRORS.desc!}</div><br />
    <input type="submit" name="submit" value="Dokonči" tabindex="3" class="button">
    <input type="hidden" name="action" value="edit2">
    <input type="hidden" name="rid" value="${RELATION.id}">
</form>
</p>

<#include "../footer.ftl">
