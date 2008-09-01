<#include "../header.ftl">

<@lib.showMessages/>

<#if PARAMS.action=="add" || PARAMS.action="add2" >
<h1>Přidání serveru rozcestníku</h1>
<#else>
<h1>Úprava serveru rozcestníku</h1>
</#if>

<form method="post" action="${URL.make("/EditServers")}">

<table class="siroka">
    <tr>
        <td class="required">Název serveru</td>
        <td>
            <input type="text" name="name" value="${PARAMS.name?if_exists}" size="40">
            <div class="error">${ERRORS.name?if_exists}</div>
        </td>
    </tr>
    <tr>
        <td class="required">URL RSS kanálu</td>
        <td>
            <input type="text" name="rssUrl" value="${PARAMS.rssUrl?if_exists}" size="40">
            <div class="error">${ERRORS.rssUrl?if_exists}</div>
        </td>
    </tr>
    <tr>
        <td class="required">URL webu</td>
        <td>
            <input type="text" name="url" value="${PARAMS.url?if_exists}" size="40">
            <div class="error">${ERRORS.url?if_exists}</div>
        </td>
    </tr>
    <tr>
        <td>Kontakt na správce RSS kanálu</td>
        <td>
            <input type="text" name="contact" value="${PARAMS.contact?if_exists}" size="40">
            <div class="error">${ERRORS.contact?if_exists}</div>
        </td>
    </tr>
</table>

 <#if PARAMS.action=="add" || PARAMS.action="add2" >
  <input type="hidden" name="action" value="add2">
  <#else>
  <input type="hidden" name="action" value="edit2">
 </#if>
 <input type="hidden" name="rid" value="${RELATION.id}">
 <input type="submit" value="Dokončit">
</form>

<#include "../footer.ftl">
