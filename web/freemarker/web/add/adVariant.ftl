<#assign html_header>
    <script type="text/javascript" src="/data/site/scripts-adtags.js"></script>
</#assign>

<#include "../header.ftl">

<@lib.showMessages/>

<h1>Přidání varianty reklamního kódu</h1>

<p>
Zde můžete přidat novou variantu k existujícímu reklamnímu kódu. To se hodí, pokud starý kód již není aktuální, ale
nechcete jej přepsat novým nebo pokud potřebujete mít kód zobrazující se jen pro určité štítky. Pokud budete zadávat
štítky, buď si naklikejte výběr nebo napište do vstupního políčka jejich identifikátory oddělené čárkou. žádná varianta
stejného kódu nesmí sdílet stejný štítek.
</p>
<p>
Vytvořená varianta bude neaktivní a musíte ji ručně aktivovat. Platí podmínka, že v jeden okamžik smí být aktivní jen jedna
varianta se stejnými parametry.
</p>

<@lib.addForm URL.noPrefix("/EditAdvertisement"), "name='form'">
    <@lib.addTextArea false, "desc", "Popis", 3 />
    <@lib.addFormField true, "Štítky">
        <div id="tagpicker">
            <@lib.addInputBare "tags", 60 />
            <script type="text/javascript">new StitkyAdvertLink();</script>
        </div>
    </@lib.addFormField>

    <@lib.addTextArea true, "htmlCode", "Kód", 15, "class='siroka'" />
    <@lib.addCheckbox "dynamic", "Dynamický kód" />

    <@lib.addSubmit "Dokonči", "finish" />
    <@lib.addHidden "code", PARAMS.code />
    <@lib.addHidden "rid", PARAMS.rid />
    <@lib.addHidden "action", "addVariant2" />
</@lib.addForm>

<#include "../footer.ftl">
