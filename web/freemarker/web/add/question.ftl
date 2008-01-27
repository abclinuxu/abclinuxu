<#include "../header.ftl">

<@lib.showMessages/>

<h1>Pokládání dotazu</h1>

<ul>
    <li>
        Přečtěte si článek <a href="/clanky/navody/jak-resit-problemy">Jak řešit problémy</a>.
    </li>
    <li>
        Zkusili jste <a href="/hledani" title="Vyhledávání">hledání</a>
        a prošli jste si <a href="/faq" title="FAQ Linux">Často kladené otázky</a> (FAQ)?
    </li>
    <li>
        Fórum slouží jen k řešení problémů s Linuxem (případně Unixy), co tuto definici
        nesplňuje (například oznámení), bude smazáno.
    </li>
    <li>Pokud máte problém s MS&nbsp;Windows a není zde příčinná souvislost
        s Linuxem, obraťte se na Microsoft, tady odpověď nedostanete.</li>
</ul>

<h2>Jak psát titulek</h2>

<p>Dobře zvolený titulek přiláká pozornost čtenářů, špatný zaručeně odradí zkušené uživatele, kteří
by vám mohli pomoci. Admini mohou špatně formulovaný titulek upravit.</p>

<ul>
    <li>
        Popište větou, v čem přesně spočívá váš problém.
    </li>
    <li>
        <b>Nepoužívejte</b> slova jako <i>help</i>, <i>pomoc</i>, <i>poraďte</i>, <i>prosím</i>,
        <i>začátečník</i> či <i>lama</i>.
    </li>
    <li>
        NEPIŠTE VELKÝMI PÍSMENY nebo <b>tučně</b> a nepoužívejte vykřičníky - je to nezdvořilé.
    </li>
    <li>
        Jeden otazník úplně stačí. Opravdu.
    </li>
</ul>

<h2>Jak popsat problém</h2>

<ul>
    <li>
        Snažte se uvést co nejvíce relevantních informací. Například:
            <ul>
                <li>druh hardwaru</li>
                <li>verze aplikace</li>
            </ul>
        (Ostatní čtenáři nemají křišťálovou kouli, aby to sami uhádli.)
    </li>

    <li>
        Popište postup, který nevede k cíli. Uveďte, jestli jste postupovali podle nějakého návodu.
        Pokud ano, vložte na něj odkaz.
    </li>

    <li>
        Často je dobré vložit ukázku konfiguračního souboru, výpis
        <code>dmesg</code> či <code>lspci</code> (HTML značka <code>&lt;PRE&gt;</code>). Nicméně
        vkládejte jen skutečně zajímavé části související s problémem, maximálně deset až
        patnáct řádek.
    </li>

    <li>
        Pokud přijdete na řešení sami, vložte jej do diskuse. Pomůžete tak ostatním čtenářům.
    </li>
</ul>

<p>
    <b>Do jednoho dotazu nevkládejte více problémů</b>. Diskusi pak není možné vhodně zařadit do
    příslušného diskusního fóra a není možné ji výstižně pojmenovat. Pro uživatele,
    který by později hledal odpověď na některý z uvedených problémů, by bylo obtížné takovou
    diskusi vyhledat. Dotazy obsahující více problémů mohou být administrátory uzamčeny, přičemž
    tazatel bude požádán, aby jednotlivé problémy popsal v samostatných diskusích.
</p>

<#if ! USER?exists>
 <h1 class="st_nadpis">Proč se přihlásit</h1>

 <p>Registrovaní čtenáři si mohou nechat sledovat diskusi, takže jim budou emailem posílány
 reakce ostatních čtenářů. Zároveň si budete moci ve svém profilu snadno vyhledat
 tento dotaz. Proto je výhodné se přihlásit. Nemáte-li u nás ještě účet,
 <a href="${URL.noPrefix("/EditUser?action=add")}">zaregistrujte se</a>.
 </p>
</#if>

<form action="${URL.make("/EditDiscussion")}" method="POST" name="form" enctype="multipart/form-data">
    <table class="siroka" cellpadding="5">
        <#if ! USER?exists>
            <tr>
                <td class="required">Login a heslo</td>
                <td>
                    <input tabindex="1" type="text" name="LOGIN" size="8">
                    <input tabindex="2" type="password" name="PASSWORD" size="8">
                </td>
            </tr>
            <tr>
                <td class="required">nebo vaše jméno</td>
                <td>
                    <input tabindex="3" type="text" size="30" name="author" value="${PARAMS.author?if_exists?html}">
                </td>
            </tr>
            <#if ! USER_VERIFIED?if_exists>
                <tr>
                    <td class="required">Aktuální rok</td>
                    <td>
                        <input type="text" size="4" name="antispam" value="${PARAMS.antispam?if_exists?html}" tabindex="4">
                        <a class="info" href="#">?<span class="tooltip">Vložte aktuální rok. Jedná se o ochranu před spamboty.
                        Po úspěšném ověření se uloží cookie (včetně vašeho jména) a tato kontrola přestane být prováděna.</span></a>
                    </td>
                </tr>
            </#if>
        </#if>
        <tr>
            <td class="required">Titulek</td>
            <td>
            <input tabindex="4" type="text" name="title" size="40" maxlength="70">
            </td>
        </tr>
        <tr>
            <td class="required">Dotaz</td>
            <td>
                <div class="form-edit">
                    <a href="javascript:insertAtCursor(document.form.text, '<b>', '</b>');" id="serif" title="Vložit značku tučně"><b>B</b></a>
                    <a href="javascript:insertAtCursor(document.form.text, '<i>', '</i>');" id="serif" title="Vložit značku kurzíva"><i>I</i></a>
                    <a href="javascript:insertAtCursor(document.form.text, '<a href=&quot;&quot;>', '</a>');" id="mono" title="Vložit značku odkazu">&lt;a&gt;</a>
                    <a href="javascript:insertAtCursor(document.form.text, '<p>', '</p>');" id="mono" title="Vložit značku odstavce">&lt;p&gt;</a>
                    <a href="javascript:insertAtCursor(document.form.text, '<pre>', '</pre>');" id="mono" title="Vložit značku formátovaného textu. Vhodné pro konfigurační soubory či výpisy.">&lt;pre&gt;</a>
                    <a href="javascript:insertAtCursor(document.form.text, '<code>', '</code>');" id="mono" title="Vložit značku pro písmo s pevnou šířkou">&lt;code&gt;</a>
                </div>
                <textarea tabindex="5" name="text" class="siroka" rows="20"></textarea><br>
            </td>
        </tr>
        <tr>
            <td>Příloha</td>
            <td>
                <input type="file" name="attachment" tabindex="6">
                <@lib.showHelp>Například výpis logu, konfigurační soubor, snímek obrazovky a podobně.</@lib.showHelp>
            </td>
        </tr>
        <tr>
            <td>&nbsp;</td>
            <td><input tabindex="7" type="submit" name="preview" value="Náhled"></td>
        </tr>
    </table>
    <input type="hidden" name="action" value="addQuez2">
    <input type="hidden" name="rid" value="${PARAMS.rid}">
</form>

<#include "/include/napoveda-k-auto-formatovani.txt">


<#include "../footer.ftl">
