<#assign html_header>
    <script type="text/javascript" src="/data/site/scripts-adtags.js"></script>
</#assign>

<#include "../header.ftl">

<@lib.showMessages/>

<h1>Úprava varianty reklamního kódu</h1>

<form action="${URL.noPrefix("/EditAdvertisement")}" method="POST" name="form">
    <table class="siroka" border="0" cellpadding="5">
        <tr>
            <td width="90">Popis</td>
            <td>
                <textarea name="desc" rows="3" class="siroka" tabindex="1">${PARAMS.desc?if_exists?html}</textarea>
                <div class="error">${ERRORS.desc?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td width="90" class="required">Štítky</td>
            <td id="tagpicker">
                <input type="text" name="tags" id="tags" value="${PARAMS.tags?if_exists?html}" size="60" tabindex="2">
                <div class="error">${ERRORS.tags?if_exists}</div>
                <script type="text/javascript">new StitkyAdvertLink();</script>
            </td>
        </tr>
        <tr>
            <td width="90" class="required">Kód</td>
            <td>
                <textarea name="htmlCode" rows="3" class="siroka" tabindex="3">${PARAMS.htmlCode?if_exists?html}</textarea>
                <div class="error">${ERRORS.htmlCode?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td width="90">&nbsp;</td>
            <td>
                <label><input type="checkbox" name="dynamic" value="yes" tabindex="4" <#if PARAMS.dynamic?default("no")=="yes">checked</#if>>Dynamický kód</label>
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
    <input type="hidden" name="variant" value="${PARAMS.variant}">
    <input type="hidden" name="rid" value="${PARAMS.rid}">
    <input type="hidden" name="action" value="editVariant2">
</form>

<#include "../footer.ftl">
