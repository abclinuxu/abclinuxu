<#include "../header.ftl">

<p>Zadejte odpov�di na tuto ot�zku. Zkontrolujte pravopis ot�zky i odpov�d�.
M��ete zadat a� p�t odpov�d� na tuto ot�zku. Po odesl�n� formul��e se ot�zka
sma�e a spolu s odpov��mi se vyrenderuje pomoc� �ablony /include/misc/talk_question.ftl
a vlo�� na konec �l�nku.</p>

<form action="${URL.make("/edit/"+RELATION.id)}" method="POST">
    <p>
        Tazatel: <input type="text" name="name" value="${PARAMS.name?if_exists}" size=40><br>
        Ot�zka<br>
        <textarea name="content" cols="80" rows="4">${PARAMS.content?if_exists?html}</textarea>
    </p>

    <p>
        Jm�no odpov�daj�c�ho 1: <input type="text" name="responder1" value="${PARAMS.responder1?if_exists}" size=40><br>
        Odpov�� 1<br>
        <textarea name="reply1" cols="80" rows="4">${PARAMS.reply1?if_exists?html}</textarea>
    </p>

    <p>
        <input type="hidden" name="id" value="${PARAMS.id}">
        <input type="hidden" name="action" value="submitReply">
        <input type="submit" value="Ulo�">
    </p>

    <p>
        Jm�no odpov�daj�c�ho 2: <input type="text" name="responder2" value="${PARAMS.responder2?if_exists}" size=40><br>
        Odpov�� 2<br>
        <textarea name="reply2" cols="80" rows="4">${PARAMS.reply2?if_exists?html}</textarea>
    </p>

    <p>
        Jm�no odpov�daj�c�ho 3: <input type="text" name="responder3" value="${PARAMS.responder3?if_exists}" size=40><br>
        Odpov�� 3<br>
        <textarea name="reply3" cols="80" rows="4">${PARAMS.reply3?if_exists?html}</textarea>
    </p>

    <p>
        Jm�no odpov�daj�c�ho 4: <input type="text" name="responder4" value="${PARAMS.responder4?if_exists}" size=40><br>
        Odpov�� 4<br>
        <textarea name="reply4" cols="80" rows="4">${PARAMS.reply4?if_exists?html}</textarea>
    </p>

    <p>
        Jm�no odpov�daj�c�ho 5: <input type="text" name="responder5" value="${PARAMS.responder5?if_exists}" size=40><br>
        Odpov�� 2<br>
        <textarea name="reply5" cols="80" rows="4">${PARAMS.reply5?if_exists?html}</textarea>
    </p>
</form>


<#include "../footer.ftl">
