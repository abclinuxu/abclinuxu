<#include "../header.ftl">
<#import "../misc/lib-bazar.ftl" as bazarlib>

<@lib.showMessages/>

<h1>Smazání inzerátu</h1>

<p>
    Opravdu chcete smazat následující inzerát?
</p>

<form action="${URL.make("/edit")}" method="POST">
    <input type="submit" value="Smazat">
    <input type="hidden" name="action" value="rm2">
    <input type="hidden" name="rid" value="${RELATION.id}">
</form>

<fieldset style="margin-top: 1em;">
    <legend>Náhled</legend>
    <@bazarlib.showBazaarAd RELATION.child, USER />
</fieldset>


<#include "../footer.ftl">
