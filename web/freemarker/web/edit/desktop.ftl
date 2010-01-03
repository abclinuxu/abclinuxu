<@lib.addRTE textAreaId="desc" formId="form" menu="wiki" />
<#include "../header.ftl">

<h1>Úprava desktopu</h1>

<@lib.showMessages/>

<p>
    Tato stránka slouží k úpravě popisu vašeho desktopu. Jméno distribuce do titulku obvykle nepatří,
    důležitější je <a href="/slovnik/wm">správce oken</a> a téma. Nahraný obrázek nejde změnit,
    můžete upravit jen titulek a popis. Desktop je možné smazat, jen dokud pod ním nejsou cizí komentáře.
</p>

<@lib.addForm URL.make("/desktopy/edit"), "name='form'">
    <@lib.addInput true, "name", "Titulek", 40 />
    <@lib.addFormField false, "URL tématu", "Adresa tématu nebo pozadí použitého v desktopu.">
        <@lib.addInputBare "theme" />
    </@lib.addFormField>

    <@lib.addTextArea false, "desc", "Popis", 20>
        <@lib.showRTEControls "desc"/>
    </@lib.addTextArea>

    <@lib.addSubmit "Dokonči" />
    <@lib.addHidden "action", "edit2" />
    <@lib.addHidden "rid", RELATION.id />
</@lib.addForm>

<#include "../footer.ftl">
