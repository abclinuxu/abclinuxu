<#assign html_header>
    <script type="text/javascript" src="/data/site/scripts-adtags.js"></script>
</#assign>

<#include "../header.ftl">

<@lib.showMessages/>

<h1>Úprava varianty reklamního kódu</h1>

<@lib.addForm URL.noPrefix("/EditAdvertisement"), "name='form'">
    <@lib.addTextArea false, "desc", "Popis", 3 />
    <@lib.addFormField true, "Štítky">
        <div id="tagpicker">
            <@lib.addInputBare "tags", 60 />
            <script type="text/javascript">new StitkyAdvertLink();</script>
        </div>
    </@lib.addFormField>

    <@lib.addTextArea true, "htmlCode", "Kód", 15 />
    <@lib.addCheckbox "dynamic", "Dynamický kód" />

    <@lib.addSubmit "Dokonči", "finish" />
    <@lib.addHidden "code", PARAMS.code />
    <@lib.addHidden "variant", PARAMS.variant />
    <@lib.addHidden "rid", PARAMS.rid />
    <@lib.addHidden "action", "editVariant2" />
</@lib.addForm>

<#include "../footer.ftl">
