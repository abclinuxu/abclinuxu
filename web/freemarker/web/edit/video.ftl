<#include "../header.ftl">

<h1>Úprava videa</h1>

<@lib.showMessages/>

<p>
Podporovanými servery jsou YouTube a Google Video.
</p>

<p>
    <form action="${URL.make("/videa/edit")}" method="POST" enctype="multipart/form-data">
        <span class="required">Titulek</span><br />
            <input type="text" name="title" value="${PARAMS.title?if_exists}" size="40">
            <div class="error">${ERRORS.title?if_exists}</div><br />
        <span class="required">Link na video</span><br />
            <input type="text" name="url" value="${PARAMS.url?if_exists}" size="40">
            <div class="error">${ERRORS.url?if_exists}</div><br />
        Popis<br />
            <textarea name="description" class="siroka" rows="5" tabindex="3">${PARAMS.description?if_exists?html}</textarea>
            <div class="error">${ERRORS.description?if_exists}</div><br />
        <input type="submit" name="submit" value="Dokonči" class="button">
        <input type="hidden" name="action" value="edit2">
        <input type="hidden" name="rid" value="${PARAMS.rid}">
    </form>
</p>

<#include "../footer.ftl">
