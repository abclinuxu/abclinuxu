<#include "../header.ftl">

<h1>Vložení videa</h1>

<@lib.showMessages/>

<p>
    Na této stránce můžete přidat nové linuxové video. Podporovanými servery jsou Stream.cz, YouTube a Google Video.
    Zadejte krátké jméno, pod kterým bude video dostupné na našem portálu, URL adresu detailu videa z podporovaných
    serverů a popisek videa (nepovinné). Po odeslání bude video načteno a odkaz na něj uložen. Video pak budete moci
    referencovat z článku nebo blogu a dále bude dostupné ve výpise všech videí.
</p>

<p>
    <form action="${URL.make("/videa/edit")}" method="POST" enctype="multipart/form-data">
        <span class="required">Titulek</span><br />
            <input type="text" name="title" value="${PARAMS.title!}" size="40">
            <div class="error">${ERRORS.title!}</div><br />
        <span class="required">Link na video</span><br />
            <input type="text" name="url" value="${PARAMS.url!}" size="40">
            <div class="error">${ERRORS.url!}</div><br />
        Popis<br />
            <textarea name="description" class="siroka" rows="5" tabindex="3">${PARAMS.description!?html}</textarea>
            <div class="error">${ERRORS.description!}</div><br />
        <input type="submit" name="submit" value="Dokonči" class="button">
        <input type="hidden" name="action" value="add2">
        <input type="hidden" name="rid" value="${PARAMS.rid}">
        <input type="hidden" name="redirect" value="${PARAMS.redirect!}">
    </form>
</p>

<#include "../footer.ftl">
