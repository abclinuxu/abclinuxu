<@lib.addRTE textAreaId="intro" formId="form" menu="blog" />
<#include "../header.ftl">

<@lib.showMessages/>

<h2>Úvod</h2>

<p>Tato stránka slouží k upravení vzhledu vašeho blogu. Můžete
zde nastavit titulek celé stránky (hodnota značky HTML&gt;HEAD&gt;TITLE)
titulek a popis blogu či počet zobrazovaných zápisů na jedné stránce
archivu. Titulek blogu není název blogu (používaný v URL), ten
nastavíte <a href="${URL.noPrefix("/blog/edit/"+REL_BLOG.id+"?action=rename")}">zde</a>.</p>

<p>Popis blogu můžete využít například ke krátké informaci o své osobě,
přidat obrázek, odkazy na své přátelé nebo blogy, které čtete.
</p>

<@lib.addForm URL.make("/blog/edit/"+REL_BLOG.id), "name='form'">
    <@lib.addFormField true, "Titulek stránky", "Zde nastavíte titulek celé stránky">
        <@lib.addInputBare "htitle", 40 />
    </@lib.addFormField>
    <@lib.addFormField false, "Titulek blogu", "Zde nastavíte titulek celé stránky">
        <@lib.addInputBare "title", 40 />
    </@lib.addFormField>
    <@lib.addTextArea false, "intro", "Popis blogu", 20>
        <@lib.showRTEControls "intro"/>
    </@lib.addTextArea>

    <@lib.addSubmit "Dokonči", "finish" />
    <@lib.addHidden "action", "custom2" />
</@lib.addForm>

<#include "../footer.ftl">
