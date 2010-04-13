<#include "../header.ftl">

<div class="no-col-ad">
    <@lib.advertisement id="hypertext2nahore" />
    <@lib.advertisement id="square" />
    <@lib.advertisement id="hypertext2dole" />
</div>

<@lib.showMessages/>


<h1>Autoři</h1>
<#if USER?? && TOOL.permissionsFor(USER, RELATION).canModify()>
    <p><a href="${URL.noPrefix("/sprava/redakce/autori/edit?action=add")}">Přidat</a></p>
</#if>

<p><a href="/clanky/novinky/pojdte-psat-pro-abclinuxu.cz">Jak se stát autorem</a></p>

<table class="autori">
    <tr>
        <td>Jméno</td>
        <td>Počet článků</td>
    </tr>
    <#list AUTHORS as author>
        <tr>
            <td>
                <a href="${author.url}">${TOOL.childName(author)}</a>
            </td>
            <td align="right">
                <a href="${author.url}">${COUNTS.get(author.id)!0}</a>
            </td>
        </tr>
    </#list>
</table>


<#include "../footer.ftl">
