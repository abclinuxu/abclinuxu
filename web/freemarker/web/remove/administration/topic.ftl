<#include "../../header.ftl">

<@lib.showMessages/>

<h2>Smazání námětu</h2>
<form action="${URL.make("/redakce/namety/edit")}" method="POST">
        <p style="white-space: nowrap">
            Opravdu chcete smazat námět <b>${TOPIC.title}</b>?
            <input type="submit" name="delete" value="Ano, smazat"/>
            <input type="submit" name="leave" value="Ne, nemazat"/>
            <input type="hidden" name="topicId" value="${TOPIC.id}"/>
            <input type="hidden" name="action" value="rm2"/>
        </p>
<form>

<#include "../../footer.ftl">