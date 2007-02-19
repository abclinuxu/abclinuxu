<#include "../header.ftl">

<h1>Poradna</h1>

<p>Poradna slouží jako místo, kde mohou nováčci i zkušení
uživatelé Linuxu vzájemně komunikovat a pomáhat si. Pro každou oblast
jsme vytvořili jedno fórum, abyste snáze nalezli hledané informace.
</p>

<p>
Chcete-li se na něco zeptat, musíte si nejdříve zvolit diskusním fórum.
Fóra jsou logicky členěny do několika sekcí, rozmyslete se, kam asi
dotaz patří. Otevřete fórum, najdete v něm odkaz na položení dotazu.
Nejdříve si ale fórum prohlédněte, zda už se někdo před vámi na totéž
neptal. Prvním krokem položení dotazu je hledání v naší obrovské databázi.
</p>

<ul>
    <li>
        <form action="/Search" method="get">
          <input type="text" class="text" name="query">
          <input type="hidden" name="type" value="otazka">
          <input class="button" type="submit" value="prohledej poradnu">
        </form>
    </li>
    <li>
        <a href="/History?from=0&amp;count=25&amp;orderBy=update&amp;orderDir=desc&amp;type=discussions">seznam živých diskusí</a>
    </li>
    <li>
        <a href="nntp://news.gmane.org/gmane.user-groups.linux.czech">news rozhraní k diskusnímu fóru</a>
    </li>
    <li>
        zasílání příspěvků emailem si můžete zapnout ve svém profilu
    </li>
</ul>

<h1>Hardware</h1>

<p>Sekce sdružující diskusní fóra týkající se instalace, nastavení
a používání rozličného hardwaru pod Linuxem.
</p>

<@listForum HARDWARE />

<h1>Nastavení</h1>

<p>Diskusní fóra na téma nastavení Linuxu, jeho prostředí, služeb
a připojení k síti či Internetu.
</p>

<@listForum SETTINGS />

<h1>Aplikace</h1>

<p>Většina vašich dotazů bude patřit do těchto diskusních fór. Zabývají
se různými aplikacemi. Každé fórum je určeno pro jednu třídu aplikací,
v názvu pak má typického reprezentanta. Například do fóra
<i>Prohlížeče,&nbsp;Mozilla</i> patří i dotazy na Operu či Lynx, nebo ve fóru
<i>Multimédia,&nbsp;MPlayer</i> hledejte diskuse i o Xine, XMMS a dalších
multimediálních programech.
</p>

<@listForum APPS />

<h1>Distribuce</h1>

<p>Diskusní fóra vyhrazená pro speciality jednotlivých distribucí.
95% dotazů patří do sekce <i>Hardware</i>, <i>Nastavení</i> nebo
<i>Aplikace</i>. Zde pokládejte dotazy, jen pokud <b>opravdu</b>
týkají dané distribuce a nikoliv i ostatních. Než zde položíte
dotaz, projděte si fóra v předešlých sekcích.
</p>

<@listForum DISTROS />

<h1>Ostatní</h1>

<p>Diskuse, které nejdou zařadit jinam. Patří zde i dotazy
na komunitu Open Source, diskuse nad licencemi a také
otázky ohledně tohoto portálu a jeho služeb.
</p>

<@listForum VARIOUS />

<#macro listForum FORUM>
    <table border="0" cellspacing="6" class="siroka">
        <tr>
            <th>Fórum</th>
            <th>Dotazů</th>
            <th>Poslední dotaz</th>
            <th>Stav</th>
            <th>Reakcí</th>
            <th>Poslední</th>
        </tr>
        <#list FORUM as forum>
            <tr>
                <td>
                    <a href="${forum.url}">${forum.name}</a>
                </td>
                <td>${forum.size}</td>
                <#if forum.lastQuestion?exists>
                    <#assign diz = forum.lastQuestion>
                    <td>
                        <a href="/forum/show/${diz.relationId}">${TOOL.limit(diz.title,60," ..")}</a>
                    </td>
                    <td>
                        <@lib.showDiscussionState diz />
                    </td>
                    <td>${diz.responseCount}</td>
                    <td>${DATE.show(diz.updated,"SMART")}</td>
                <#else>
                    <td></td>
                    <td></td>
                    <td></td>
                    <td></td>
                </#if>
            </tr>
        </#list>
    </table>
</#macro>

<#include "../footer.ftl">
