<@lib.addRTE textAreaId="desc" formId="form" menu="wiki" />
<#include "../header.ftl">

<h1>Nový desktop</h1>

<@lib.showMessages/>

<p>
    Tato stránka slouží k nahrání snímku vašeho desktopu, tedy screenshotu pracovního
    prostředí vašeho počítače (KDE, GNOME apod.). Sekce není určena ke vkládání různých
    fotek, ať už vašeho stolu, nebo něčeho jiného. Jméno distribuce do titulku obvykle nepatří,
    důležitější je <a href="/slovnik/wm">správce oken</a> a téma. Nahraný obrázek již nepůjde změnit,
    budete moci upravit jen titulek a popis. Desktop je možné smazat, jen dokud pod ním
    nejsou cizí komentáře.
</p>

<@lib.addForm URL.make("/desktopy/edit"), "name='form'", true>
    <@lib.addInput true, "name", "Titulek", 40 />
    <@lib.addFormField true, "Obrázek", "Maximální velikost obrázku je omezena na 1&nbsp;megabajt, minimální rozměry jsou 640x480."+
                " Nejvhodnější formát je obvykle PNG, můžete použít i JPG či GIF.">

        <@lib.addFileBare "screenshot" />
    </@lib.addFormField>

    <@lib.addFormField false, "URL tématu", "Adresa tématu nebo pozadí použitého v desktopu.">
        <@lib.addInputBare "theme", 40 />
    </@lib.addFormField>

    <@lib.addTextArea false, "desc", "Popis", 20>
        <@lib.showRTEControls "desc"/>
    </@lib.addTextArea>

    <@lib.addSubmit "Dokonči" />
    <@lib.addHidden "action", "add2" />
</@lib.addForm>

<#include "../footer.ftl">