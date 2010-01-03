<#include "../header.ftl">

<h1>Vložení videa</h1>

<@lib.showMessages/>

<p>
    Na této stránce můžete přidat nové linuxové video. Podporovanými servery jsou Stream.cz, YouTube a Google Video.
    Zadejte krátké jméno, pod kterým bude video dostupné na našem portálu, URL adresu detailu videa z podporovaných
    serverů a popisek videa (nepovinné). Po odeslání bude video načteno a odkaz na něj uložen. Video pak budete moci
    referencovat z článku nebo blogu a dále bude dostupné ve výpise všech videí.
</p>

<@lib.addForm URL.make("/videa/edit"), "", true>
    <@lib.addInput true, "title", "Titulek", 40 />
    <@lib.addInput true, "url", "Link na video", 40 />
    <@lib.addTextArea false, "description", 5, "class='siroka'" />
    <@lib.addSubmit "Dokonči" />
    <@lib.addHidden "action", "add2" />
    <@lib.addHidden "rid", PARAMS.rid />
    <@lib.addHidden "redirect", PARAMS.redirect! />
</@lib.addForm>

<#include "../footer.ftl">
