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

<form action="${URL.noPrefix("/EditAdvertisement")}" method="POST" name="form">
    <table class="siroka" border="0" cellpadding="5">
        <tr>
            <td width="90">Popis</td>
            <td>
                <textarea name="desc" rows="3" class="siroka" tabindex="1">${PARAMS.desc!?html}</textarea>
                <div class="error">${ERRORS.desc!}</div>
            </td>
        </tr>
        <tr>
            <td width="90" class="required">Štítky</td>
            <td id="tagpicker">
                <input type="text" name="tags" id="tags" value="${PARAMS.tags!?html}" size="60" tabindex="2">
                <div class="error">${ERRORS.tags!}</div>
            </td>
            <script type="text/javascript">new StitkyAdvertLink();</script>
        </tr>
        <tr>
            <td width="90" class="required">Kód</td>
            <td>
                <textarea name="htmlCode" rows="20" class="siroka" tabindex="3">${PARAMS.htmlCode!?html}</textarea>
                <div class="error">${ERRORS.htmlCode!}</div>
            </td>
        </tr>
        <tr>
            <td width="90">&nbsp;</td>
            <td>
                <label><input type="checkbox" name="dynamic" value="yes" tabindex="4">Dynamický kód</label>
            </td>
        </tr>
        <tr>
            <td width="90">&nbsp;</td>
            <td>
                <input tabindex="5" type="submit" name="finish" value="Dokonči">
            </td>
        </tr>
    </table>
    <input type="hidden" name="code" value="${PARAMS.code}">
    <input type="hidden" name="rid" value="${PARAMS.rid}">
    <input type="hidden" name="action" value="addVariant2">
</form>

<#include "../footer.ftl">
