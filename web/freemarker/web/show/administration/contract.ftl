<#include "../../header.ftl">

<#if LAYOUT != "print">
    <@lib.showSignPost "Rozcestník">
    <ul>
       <li>
           <a href="${URL.make("/sprava/redakce/smlouvy/show/" + CONTRACT.relationId + "?varianta=print")}">Verze pro tisk</a>
       </li>
    </ul>
    </@lib.showSignPost>
</#if>

<@lib.showMessages/>

<#if EDITOR??>
	<table>
        <tr>
            <td>Název</td>
            <td>${CONTRACT.title}</td>
        </tr>
        <tr>
            <td>Autor</td>
            <td>
                <@lib.showAuthor topic.author />
            </td>
        </tr>
        <tr>
            <td>Podepsáno</td>
            <td>${DATE.show(CONTRACT.signed, "ISO_DMY")}</td>
        </tr>
    </table>
</#if>

${CONTRACT.content}

<#include "../../footer.ftl">