<#include "../header.ftl">

<h1>P�ehled str�nek</h1>

<p>Na t�to str�nce najdete p�ehledn� seznam dokument�,
jejich adres a datumu posledn� �pravy. Pokud si chcete nechat
zobrazit jen dokumenty, kter� byly zm�n�ny od ur�it�ho data,
vypl�te toto datum do formul��e a ode�lete jej. Form�t data
je <code>2005-09-06 20:55</code>.</p>

<form action="${URL.make("/zmeny/"+RELATION.id)}" method="POST">
    <input type="text" name="since" value="${PARAMS.since?default("")}" size="16" maxlength="16" taborder="1">
    <input type="submit" value="Zobraz">
</form>

<#if ! DATA?has_content>
    <p>
        <#if (PARAMS.since?if_exists?size>0)>
            Dan�mu filtru nevyhovuje ��dn� str�nka.
        <#else>
            Pod touto str�nkou nejdou ��dn� podstr�nky.
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
