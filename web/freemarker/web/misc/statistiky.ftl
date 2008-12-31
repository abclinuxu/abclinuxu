<#include "../header.ftl">

<@lib.showMessages/>

<h2>Statistiky návštěvnosti</h2>

TODO: zobrazit jako graf

<table>
    <#list DATA as page>
        <tr>
            <td>${page[0]}</td>
            <td>${page[1]}</td>
            <td>${page[2]}</td>
        </tr>
    </#list>
</table>

<form>
    <input type="radio" name="type" value="monthly"<#if PARAMS.type! == 'monthly'> checked</#if>>Měsíčně<br>
    <input type="radio" name="type" value="period"<#if PARAMS.type! == 'period'> checked</#if>>Za zvolené období
    <input type="text" name="start" value="${PARAMS.start!}" size=15> -
    <input type="text" name="stop" value="${PARAMS.stop!}" size=15><br>
    <input type="radio" name="type" value="day"<#if PARAMS.type?default('day')=='day'> checked</#if>>Za zvolený den
    <input type="text" name="day" value="${PARAMS.day!}" size=15><br>
    <input type="submit" value="Zobrazit">
</form>

<#include "../footer.ftl">
