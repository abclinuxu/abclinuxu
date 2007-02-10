<#include "../header.ftl">

<p>Zadejte odpovědi na tuto otázku. Zkontrolujte pravopis otázky i odpovědí.
Můžete zadat až pět odpovědí na tuto otázku. Po odeslání formuláře se otázka
smaže a spolu s odpověďmi se vyrenderuje pomocí šablony /include/misc/talk_question.ftl
a vloží na konec článku.</p>

<form action="${URL.make("/edit/"+RELATION.id)}" method="POST">
    <p>
        Tazatel: <input type="text" name="name" value="${PARAMS.name?if_exists}" size=40><br>
        Otázka<br>
        <textarea name="content" cols="80" rows="4">${PARAMS.content?if_exists?html}</textarea>
    </p>

    <p>
        Jméno odpovídajícího 1: <input type="text" name="responder1" value="${PARAMS.responder1?if_exists}" size=40><br>
        Odpověď 1<br>
        <textarea name="reply1" cols="80" rows="4">${PARAMS.reply1?if_exists?html}</textarea>
    </p>

    <p>
        <input type="hidden" name="id" value="${PARAMS.id}">
        <input type="hidden" name="action" value="submitReply">
        <input type="submit" value="Ulož">
    </p>

    <p>
        Jméno odpovídajícího 2: <input type="text" name="responder2" value="${PARAMS.responder2?if_exists}" size=40><br>
        Odpověď 2<br>
        <textarea name="reply2" cols="80" rows="4">${PARAMS.reply2?if_exists?html}</textarea>
    </p>

    <p>
        Jméno odpovídajícího 3: <input type="text" name="responder3" value="${PARAMS.responder3?if_exists}" size=40><br>
        Odpověď 3<br>
        <textarea name="reply3" cols="80" rows="4">${PARAMS.reply3?if_exists?html}</textarea>
    </p>

    <p>
        Jméno odpovídajícího 4: <input type="text" name="responder4" value="${PARAMS.responder4?if_exists}" size=40><br>
        Odpověď 4<br>
        <textarea name="reply4" cols="80" rows="4">${PARAMS.reply4?if_exists?html}</textarea>
    </p>

    <p>
        Jméno odpovídajícího 5: <input type="text" name="responder5" value="${PARAMS.responder5?if_exists}" size=40><br>
        Odpověď 2<br>
        <textarea name="reply5" cols="80" rows="4">${PARAMS.reply5?if_exists?html}</textarea>
    </p>
</form>


<#include "../footer.ftl">
