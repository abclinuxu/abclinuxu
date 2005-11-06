<#include "../header.ftl">

<h1>Pøehled stránek</h1>

<p>Na této stránce najdete pøehledný seznam dokumentù,
jejich adres a datumu poslední úpravy. Pokud si chcete nechat
zobrazit jen dokumenty, které byly zmìnìny od urèitého data,
vyplòte toto datum do formuláøe a ode¹lete jej. Formát data
je <code>${DATE.show(NOW, "ISO")}</code>.</p>

<form action="${URL.make("/zmeny/"+RELATION.id)}" method="POST">
    <input type="text" name="since" value="${PARAMS.since?default("")}" size="16" maxlength="16" taborder="1">
    <input type="submit" value="Zobraz">
</form>

<#if ! DATA?has_content>
    <p>
        <#if (PARAMS.since?if_exists?size>0)>
            Danému filtru nevyhovuje ¾ádná stránka.
        <#else>
            Pod touto stránkou nejsou ¾ádné podstránky.
        </#if>
    </p>
<#else>
    <table>
        <tr>
            <th>URL</th>
            <th>Poslední zmìna</th>
            <th>Znakù</th>
            <th align="left">Autor</th>
        </tr>
        <#list DATA as relation>
            <#assign item=relation.child, autor=TOOL.createUser(item.owner)>
            <tr>
                <td>
                    <a href="${relation.url}">${TOOL.reverseLimit(relation.url,50,"..")}</a>
                </td>
                <td align="right">
                    ${DATE.show(item.updated, "CZ_FULL")}
                </td>
                <td align="right">
                    ${(TOOL.removeTags(TOOL.xpath(item,"/data/content")))?length}
                </td>
                <td>
                    <a href="/Profile/${autor.id}">${autor.nick?default(autor.name)}</a>
                </td>
            </tr>
        </#list>
    </table>
</#if>

<#include "../footer.ftl">
