<#include "../header.ftl">

<@lib.showMessages/>

<h2>Potvrzení <#if USER2?exists>sloučení<#else>odstranění</#if> uživatelů</h2>

<#macro showUser(user)>
    <table border="1">
        <tr>
            <td>Jméno</td>
            <td><a href="/Profile/${user.id}">${user.name}</a></td>
        </tr>
        <tr>
            <td>Číslo</td>
            <td>${user.id}</td>
        </tr>
        <tr>
            <td>Přezdívka</td>
            <td>${user.nick?default(" ")}</td>
        </tr>
        <tr>
            <td>E-mail</td>
            <td>${user.email}</td>
        </tr>
        <tr>
            <td>Datum registrace</td>
            <td>
                <#assign registered = TOOL.xpath(user,"/data/system/registration_date")?default("UNDEFINED")>
                <#if registered != "UNDEFINED">
                    ${DATE.show(registered, "CZ_DMY")}
                <#else>
                    starší než 12. 7. 2003
                </#if>
            </td>
        </tr>
    </table>
</#macro>

<p>
    Nyní ověřte správnost vybraných uživatelů. Prováděná operace je nenávratná!
</p>

<table border="0">
    <tr>
        <td><@showUser USER1 /></td>
        <td align="center">---&gt;</td>
        <td><#if USER2?exists><@showUser USER2 /><#else>/dev/null</#if></td>
    </tr>
    <tr>
        <td></td>
        <td>
            <form action="${URL.make("/EditUser")}" method="POST">
                <input type="hidden" name="uid1" value="${PARAMS.uid1}">
                <input type="hidden" name="uid2" value="${PARAMS.uid2?if_exists}">
                <input type="hidden" name="action" value="removeMerge3">
                <input type="submit" value="Dokonči <#if USER2?exists>sloučení<#else>odstranění</#if>">
            </form>
        </td>
        <td></td>
    </tr>
</table>

<#include "../footer.ftl">
