<#include "../header.ftl">

<p>Nach�z�te se na str�nce online rozhovoru. Je ur�ena pro
redaktora, aby mohl rychle zad�vat ot�zky a rozes�lat je
jednotliv�m ��astn�k�m. Kdy� pak p�ijme odpov�di, jednodu�e
je p�i�ad� k ot�zce, provede korekturu a p�esune zodpov�zenou
ot�zku do �l�nku.
</p>

<p>
    <#if XML.data.talk.addresses.email?size gt 0>
        Ot�zky budou zas�l�ny na tyto adresy:
        <#list XML.data.talk.addresses.email as email>${email}<#if email_has_next>, </#if></#list>.
    <#else>
        ��dn� emailov� adresy nejsou zad�ny!
    </#if>
    <a href="${URL.make("/edit/"+RELATION.id+"?action=talkEmails")}">Uprav emailov� adresy</a>
</p>

<@lib.showMessages/>

<h3>Nov� ot�zka</h3>

<form action="${URL.make("/edit/"+RELATION.id)}" method="POST">
    Jm�no tazatele: <input type="text" name="name" value="${PARAMS.name?if_exists}" size=40 tabindex=1><br>
    Ot�zka<br>
    <textarea name="content" cols="80" rows="4" tabindex="2">${PARAMS.content?if_exists?html}</textarea>
    <div class="error">${ERRORS.name?if_exists}${ERRORS.content?if_exists}</div>
    <input type="hidden" name="action" value="addQuestion">
    <input type="submit" value="Ulo�" tabindex="3">
</form>

<#list XML.data.talk.question?if_exists as question>
    <p>
        <b>${question.@id}. ot�zka</b>, autor: ${question.@name}<br>
        ${question}<br>
        <a href="${URL.make("/edit/"+RELATION.id+"?action=sendQuestion&amp;id="+question.@id)}">Poslat emailem</a>
        <a href="${URL.make("/edit/"+RELATION.id+"?action=addReply&amp;id="+question.@id)}">P�idat odpov��</a>
        <a href="${URL.make("/edit/"+RELATION.id+"?action=removeQuestion&amp;id="+question.@id)}">Smazat</a>
    </p>
</#list>

<#include "../footer.ftl">
