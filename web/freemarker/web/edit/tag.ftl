<#include "../header.ftl">

<@lib.showMessages/>

<h1>Upravit štítek</h1>

<p>
    Každý štítek musí mít unikátní jméno, které smí obsahovat pouze znaky
    české abecedy, číslice, podtržítko, plus a pomlčku. Editací se nezmění
    url štítku. Nadřazený štítek můžete definovat pro vztah mezi konkrétnějším
    a obecnějším štítkem, například mysql&nbsp;-&nbsp;databáze či
    java&nbsp;-&nbsp;programování.
</p>

<@lib.addForm URL.make("/stitky/edit")>
    <@lib.addInput true, "title", "Titulek", 30 />
    <@lib.addFormField false, "Nadřazený štítek", "Id štítku, například programovani z URL /stitky/programovani">
        <@lib.addInputBare "parent" />
    </@lib.addFormField>
    <@lib.addSubmit "Dokonči" />
</@lib.addForm>

<#include "../footer.ftl">
