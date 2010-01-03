<#include "../header.ftl">

<@lib.showMessages/>

<h2>Linkování relace</h2>

<p>Chystáte se nalinkovat <b>${TOOL.childName(SELECTED)}</b>
do <b>${TOOL.childName(CURRENT)}</b>.</p>

<p>Pokud chcete změnit jméno relace, zde máte možnost.
Nechte tento formulář prázdný, pokud si přejete ponechat
původní jméno.</p>

<@lib.addForm URL.noPrefix("/EditRelation")>
    <@lib.addInput false, "name", "Nové jméno", 40 />
    <@lib.addSubmit "Dokonči" />
    <@lib.addHidden "action", "add2" />
    <@lib.addHidden "prefix", PARAMS.prefix />
    <@lib.addHidden "rid", PARAMS.rid />
    <@lib.addHidden "selectedId", PARAMS.selectedId />
</@lib.addForm>

<#include "../footer.ftl">
