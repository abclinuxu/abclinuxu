<#include "../header.ftl">

<@lib.showMessages/>

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
    <input type="radio" name="type" value="monthly"<#if PARAMS.type?default('monthly')=='monthly'> checked</#if>>Mìsíènì<br>
    <input type="radio" name="type" value="period"<#if PARAMS.type?if_exists=='period'> checked</#if>>Za zvolené období
    <input type="text" name="start" value="${PARAMS.start?if_exists}" size=15> -
    <input type="text" name="stop" value="${PARAMS.stop?if_exists}" size=15><br>
    <input type="radio" name="type" value="day"<#if PARAMS.type?if_exists=='day'> checked</#if>>Za zvoleny den
    <input type="text" name="day" value="${PARAMS.day?if_exists}" size=15><br>
    <input type="submit" value="Zobrazit">
</form>

<#include "../footer.ftl">
