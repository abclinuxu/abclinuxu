<#assign html_header>
    <link rel="stylesheet" type="text/css" media="all" href="/data/site/calendar/calendar-system.css" />
    <script type="text/javascript" src="/data/site/calendar/calendar.js"></script>
    <script type="text/javascript" src="/data/site/calendar/calendar-en.js"></script>
    <script type="text/javascript" src="/data/site/calendar/calendar-cs-utf8.js"></script>
    <script type="text/javascript" src="/data/site/calendar/calendar-setup.js"></script>
</#assign>
<#include "../header.ftl">

<@lib.showMessages/>

<h2>Honorář</h2>

<form action="${URL.make("/honorare/"+RELATION.id)}" method="POST">
    <table width=100 border=0 cellpadding=5>
        <tr>
            <td width="90" class="required">Číslo autora</td>
            <td>
                <input type="text" name="authorId" value="${PARAMS.authorId!}" size=60 tabindex=1>
                <div class="error">${ERRORS.authorId!}</div>
            </td>
        </tr>
        <tr>
            <td width="90" class="required">Datum publikování</td>
            <td>
                <input type="text" name="published" id="datetime_input" value="${PARAMS.published!}" size=40 tabindex=2>
                <input type="button" id="datetime_btn" value="..."><script type="text/javascript">cal_setupDate()</script>
                <div class="error">${ERRORS.published!}</div>
            </td>
        </tr>
        <tr>
            <td width="90">Datum zaplacení</td>
            <td>
                <input type="text" name="paid" value="${PARAMS.paid!}" size=40 tabindex=3>
                <div class="error">${ERRORS.paid!}</div>
            </td>
        </tr>
        <tr>
            <td width="90" class="required">Částka</td>
            <td>
                <input type="text" name="amount" value="${PARAMS.amount!}" size=10 tabindex=4>
                <div class="error">${ERRORS.amount!}</div>
            </td>
        </tr>
        <tr>
            <td width="90">Poznámka</td>
            <td>
                <textarea name="note" cols="60" rows="4" tabindex="5">${PARAMS.note!}</textarea>
                <div class="error">${ERRORS.note!}</div>
            </td>
            </tr>
            <tr>
            <td width="90">&nbsp;</td>
            <td><input type="submit" value="Pokračuj" tabindex="8"></td>
        </tr>
    </table>
    <#if PARAMS.action=="add" || PARAMS.action="add2" >
        <input type="hidden" name="action" value="add2">
    <#else>
        <input type="hidden" name="action" value="edit2">
    </#if>
</form>


<#include "../footer.ftl">
