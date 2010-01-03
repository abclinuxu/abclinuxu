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

<@lib.addForm URL.make("/honorare/"+RELATION.id)>
    <@lib.addInput true, "authorId", "Číslo autora" />

    <@lib.addInput false, "published", "Datum publikování", 40>
        <input type="button" id="datetime_btn" value="...">
        <script type="text/javascript">
            Calendar.setup({inputField:"published",ifFormat:"%Y-%m-%d %H:%M",showsTime:true,button:"datetime_btn",singleClick:false,step:1,firstDay:1});
        </script>
    </@lib.addInput>

    <@lib.addInput false, "paid", "Datum zaplacení", 40 />
    <@lib.addInput true, "amount", "Částka", 10 />
    <@lib.addTextArea false, "note", "Poznámka", 4 />
    <@lib.addSubmit "Pokračuj" />

    <#if PARAMS.action=="add" || PARAMS.action="add2" >
        <@lib.addHidden "action", "add2" />
    <#else>
        <@lib.addHidden "action", "edit2" />
    </#if>
</@lib.addForm>

<#include "../footer.ftl">
