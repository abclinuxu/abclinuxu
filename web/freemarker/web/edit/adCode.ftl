<#include "../header.ftl">

<@lib.showMessages/>

<h1>Úprava reklamního kódu</h1>

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
</p>

<@lib.addForm URL.noPrefix("/EditAdvertisement"), "name='form'">
    <@lib.addInput true, "name", "Název", 60 />
    <@lib.addFormField true, "Regulární výraz">
        <select id="regexp-select" onchange="changeRegexp(this)"></select>
        <@lib.addInputBare "regexp", 60 />
    </@lib.addFormField>
    <@lib.addTextArea false, "desc", "Popis", 3, "class='siroka'" />
    <@lib.addSubmit "Dokonči", "finish" />

    <@lib.addHidden "action", "editCode2" />
    <@lib.addHidden "code", PARAMS.code />
    <@lib.addHidden "rid", PARAMS.rid />
</@lib.addForm>

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
