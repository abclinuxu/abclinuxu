<#include "../../header.ftl">

<@lib.showMessages/>

<@lib.showSignPost "Rozcestník">
<ul>
    <li>
        <a href="/sprava/redakce/?action=switch">Změnit roli</a>
    </li>
</ul>
</@lib.showSignPost>

<p>
    Vítejte v redakčním systému. Zde můžete spravovat články, honoráře, náměty, autory a  další.
</p>

<table>
    <tr>
        <td style="vertical-align: top">
            <ul>
                <li><a href="/sprava/redakce/clanky">Články</a> N/A</li>
                <li><a href="/sprava/redakce/namety">Náměty</a></li>
                <li><a href="/sprava/redakce/serialy">Seriály</a> N/A</li>
                <li><a href="/sprava/redakce/klicova-slova">Klíčová slova</a> N/A</li>
            </ul>
        </td>
        <td style="vertical-align: top">
            <ul>
                <#if ROLE=="EDITOR_IN_CHIEF">
                    <li><a href="/sprava/redakce/honorare">Honoráře</a> N/A</li>
                </#if>
                <li><a href="/sprava/redakce/autori">Autoři</a></li>
                <li><a href="/sprava/redakce/smlouvy">Smlouvy</a></li>
                <li><a href="/sprava/redakce/statistiky">Statistiky</a> N/A</li>
            </ul>
        </td>
        <td style="vertical-align: top">
            <ul>
                <li><a href="/sprava/redakce/zpravicky">Zprávičky</a> N/A</li>
                <li><a href="/sprava/redakce/ankety">Ankety</a> N/A</li>
                <li><a href="/sprava/redakce/udalosti">Události</a> N/A</li>
                <li><a href="/sprava/redakce/maily">Maily</a> N/A</li>
            </ul>
        </td>
    </tr>
</table>

<#include "../../footer.ftl">