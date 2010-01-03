<#include "../header.ftl">

<@lib.showMessages/>

<h1>Vytvořit štítek</h1>

<p>
    Štítky slouží ke kategorizaci dokumentů na tomto portále. Návštěvníci mohou
    oštítkovat různé dokumenty a následně podle těchto štítků vyhledávat související
    dokumenty. Každý štítek musí mít unikátní jméno, které smí obsahovat pouze znaky
    české abecedy, číslice, podtržítko, plus a pomlčku.
    <#if USER?? && USER.hasRole("tag admin")>
        Nadřazený štítek můžete definovat pro vztah mezi konkrétnějším a obecnějším štítkem,
        například mysql&nbsp;-&nbsp;databáze či java&nbsp;-&nbsp;programování.
    </#if>
</p>

<@lib.addForm URL.make("/stitky/edit")>
    <@lib.addInput true, "title", "Titulek" />
    <#if USER?? && USER.hasRole("tag admin")>
        <@lib.addFormField false, "Nadřazený štítek", "ID štítku, například programovani z URL /stitky/programovani">
            <@lib.addInputBare "parent" />
        </@lib.addFormField>
    </#if>

    <@lib.addSubmit "Dokonči", "submit" />
    <@lib.addHidden "action", "add2" />
</@lib.addForm>

<#include "../footer.ftl">
