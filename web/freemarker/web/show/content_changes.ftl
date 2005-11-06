<#include "../header.ftl">

<h1>P�ehled str�nek</h1>

<p>Na t�to str�nce najdete p�ehledn� seznam dokument�,
jejich adres a datumu posledn� �pravy. Pokud si chcete nechat
zobrazit jen dokumenty, kter� byly zm�n�ny od ur�it�ho data,
vypl�te toto datum do formul��e a ode�lete jej. Form�t data
je <code>${DATE.show(NOW, "ISO")}</code>.</p>

<form action="${URL.make("/zmeny/"+RELATION.id)}" method="POST">
    <input type="text" name="since" value="${PARAMS.since?default("")}" size="16" maxlength="16" taborder="1">
    <input type="submit" value="Zobraz">
</form>

<#if ! DATA?has_content>
    <p>
        <#if (PARAMS.since?if_exists?size>0)>
            Dan�mu filtru nevyhovuje ��dn� str�nka.
        <#else>
            Pod touto str�nkou nejsou ��dn� podstr�nky.
        </#if>
    </p>
<#else>
    <table>
        <tr>
            <th>URL</th>
            <th>Posledn� zm�na</th>
            <th>Znak�</th>
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
