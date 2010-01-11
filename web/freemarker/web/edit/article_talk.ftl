<#include "../header.ftl">


<h2>Online rozhovor</h2>

<p>Nacházíte se na stránce online rozhovoru. Je určena pro
redaktora, aby mohl rychle zadávat otázky a rozesílat je
jednotlivým účastníkům. Když pak přijme odpovědi, jednoduše
je přiřadí k otázce, provede korekturu a přesune zodpovězenou
otázku do článku.</p>

<p>
    <#if XML.data.talk.addresses.email?size gt 0>
        Otázky budou zasílány na tyto adresy:
        <#list XML.data.talk.addresses.email as email>${email}<#if email_has_next>, </#if></#list>.
    <#else>
        Žádné emailové adresy nejsou zadány!
    </#if>
    <a href="${URL.make("/edit/"+RELATION.id+"?action=talkEmails")}">Uprav emailové adresy</a>
</p>

<@lib.showMessages/>

<h3>Nová otázka</h3>

<form action="${URL.make("/edit/"+RELATION.id)}" method="POST">
    Jméno tazatele: <input type="text" name="name" value="${PARAMS.name!}" size="40"><br>
    Otázka<br>
    <textarea name="content" cols="80" rows="4">${PARAMS.content!?html}</textarea>
    <@lib.showError "name"/>
    <@lib.showError "content" />
    <input type="hidden" name="action" value="addQuestion">
    <input type="submit" value="Ulož">
</form>

<#list XML.data.talk.question! as question>
    <p>
        <b>${question.@id}. otázka</b>, autor: ${question.@name}<br>
        ${question}<br>
        <a href="${URL.make("/edit/"+RELATION.id+"?action=sendQuestion&amp;id="+question.@id+TOOL.ticket(USER, false))}">Poslat emailem</a>
        <a href="${URL.make("/edit/"+RELATION.id+"?action=addReply&amp;id="+question.@id)}">Přidat odpověď</a>
        <a href="${URL.make("/edit/"+RELATION.id+"?action=removeQuestion&amp;id="+question.@id+TOOL.ticket(USER, false))}">Smazat</a>
    </p>
</#list>

<#include "../footer.ftl">
