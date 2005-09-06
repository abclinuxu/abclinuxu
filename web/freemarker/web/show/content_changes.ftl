<#include "../header.ftl">

<h1>Pøehled stránek</h1>

<p>Na této stránce najdete pøehledný seznam dokumentù,
jejich adres a datumu poslední úpravy. Pokud si chcete nechat
zobrazit jen dokumenty, které byly zmìnìny od urèitého data,
vyplòte toto datum do formuláøe a ode¹lete jej. Formát data
je <code>2005-09-06 20:55</code>.</p>

<form action="${URL.make("/zmeny/"+RELATION.id)}" method="POST">
    <input type="text" name="since" value="${PARAMS.since?default("")}" size="16" maxlength="16" taborder="1">
    <input type="submit" value="Zobraz">
</form>

<#if ! DATA?has_content>
    <p>
        <#if (PARAMS.since?if_exists?size>0)>
            Danému filtru nevyhovuje ¾ádná stránka.
        <#else>
            Pod touto stránkou nejdou ¾ádné podstránky.
        </#if>
    </p>
<#else>
    <#list DATA as relation>
        <#assign autor=TOOL.createUser(relation.child.owner)>
        <p>
            <a href="${relation.url}">${TOOL.reverseLimit(relation.url,70,"..")}</a><br>
            ${TOOL.childName(relation)},
            <a href="/Profile/${autor.id}">${autor.nick?default(autor.name)}</a>,
            ${DATE.show(relation.child.updated, "CZ_FULL")}
        </p>
    </#list>
</#if>

<#include "../footer.ftl">
