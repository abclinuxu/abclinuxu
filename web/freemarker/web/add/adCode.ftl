<#include "../header.ftl">

<@lib.showMessages/>

<h1>Vložení dalšího reklamního kódu</h1>

<p>
    Zde můžete přidat další reklamní kód k reklamní pozici.
    Tato funkce je užitečná tehdy, když chcete pro určitou pozici
    zobrazovat různé reklamy v závislosti na aktuální URL adrese.
    Například aby články měly svůj vlastní reklamní kód.
</p>

<p>
    Regulární výraz slouží pro určení, zda aktuální URL adresa má
    být obsloužena tímto kódem či nikoliv. Obvykle postačí napsat
    začátek URL adresy (/clanky), pro složitější konstrukce kontaktujte
    programátory. Pokud se žádný kód nebude hodit k aktuální URL adrese,
    použije se hlavní reklamní kód z pozice.
</p>

<p>
    Příznak dynamického kódu nastavte jen tehdy, obsahuje-li reklamní kód
    programovací instrukce jazyku Freemarker a musí se nejdříve zpracovat.
    To je potřeba například i pro makra na zobrazení aktuální ceny produktu
    z eshopu 64bit.cz.
</p>

<form action="${URL.noPrefix("/EditAdvertisement")}" method="POST" name="form">
    <table class="siroka" border="0" cellpadding="5">
        <tr>
            <td width="90" class="required">Název</td>
            <td>
                <input type="text" name="name" value="${PARAMS.name?if_exists?html}" size="60" tabindex="0">
                <div class="error">${ERRORS.name?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td width="90" class="required">Regulární výraz</td>
            <td>
                <select id="regexp-select" onchange="changeRegexp(this)"></select>
                <input type="text" name="regexp" id="regexp" value="${PARAMS.regexp?if_exists?html}" size="60" tabindex="1">
                <div class="error">${ERRORS.regexp?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td width="90">Popis</td>
            <td>
                <textarea name="desc" rows="3" class="siroka" tabindex="2">${PARAMS.desc?if_exists?html}</textarea>
                <div class="error">${ERRORS.desc?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td width="90">Výchozí reklamní kód</td>
            <td>
                <textarea name="htmlCode" rows="15" class="siroka" tabindex="3">${PARAMS.htmlCode?if_exists?html}</textarea>
                <div class="error">${ERRORS.htmlCode?if_exists}</div>
            </td>
        </tr>
                <tr>
            <td width="90">Dynamický kód</td>
            <td>
                <input type="checkbox" name="dynamic"<#if PARAMS.dynamic?if_exists>checked</#if> tabindex="4">
            </td>
        </tr>
        <tr>
            <td width="90">&nbsp;</td>
            <td>
                <input tabindex="5" type="submit" name="finish" value="Dokonči">
            </td>
        </tr>
    </table>
    <input type="hidden" name="action" value="addCode2">
    <input type="hidden" name="rid" value="${PARAMS.rid}">
</form>

<script type="text/javascript"><!--
regexps = new Array();
pos = 0;

<#list TOOL.getStandardAdRegexps().entrySet() as entry>
        regexps[pos++] = new Array("${entry.key?js_string}", "${entry.value?js_string}");
</#list>

var sel = document.getElementById("regexp-select");
var text = document.getElementById("regexp");

sel.selIndex = -1;

opt = new Option("---", "---");
sel.options[0] = opt;

for(var i=0;i<regexps.length;i++)
{
    opt = new Option(regexps[i][1], regexps[i][0]);
    sel.options[i+1] = opt;

    if(regexps[i][0] == text.value)
        sel.selIndex = i+1;
}

function changeRegexp(sel)
{
    if(sel.value != "---")
        text.value = sel.value;
}

//--></script>

<#include "../footer.ftl">
