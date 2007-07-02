<#include "../header.ftl">
<#import "../misc/lib-bazar.ftl" as bazarlib>

<@lib.showMessages/>

<p>
    Pro inzeráty platí následující pravidla. Prosíme, přečtěte si je
    a řiďte se jimi. V krajním případě mohou administrátoři váš inzerát
    smazat.
</p>

<ul>
    <li>
        Inzeráty jsou bezplatnou službou čtenářům tohoto portálu. Výdělečná činnost
        není povolena. V případě pochybností rozhoduje provozovatel. Případné komerčně
        laděné inzeráty je nutné předjednat dopředu s inzertním oddělením provozovatele.
    </li>
    <li>
        Inzeráty musí být v souladu se zaměřením tohoto portálu. Vhodné inzeráty jsou
        například prodej <a href="/hardware">hardwaru</a> či spotřební elektroniky,
        <a href="/software">linuxového softwaru</a>, odborné literatury,
        plyšových tučňáků apod.
    </li>
    <li>
        Je zakázáno vkládat inzerát se stejným či podobným obsahem dříve než za celých
        uplynulých 7 dní. Inzerát nesmí porušovat místní zákony.
    </li>
    <!--<li>
        Inzeráty budou po 30 dnech automaticky smazány. Pokud ale uspějete dříve, smažte
        prosím inzerát ručně.
    </li>-->
    <li>
        Do <b>titulku</b> napište, co prodáváte či hledáte. Čtenáři se pak budou lépe orientovat
        ve výpise inzerátů. Do <b>kontaktu</b> napište své telefonní číslo, Jabber, ICQ apod. Necháte-li
        jej prázdný, systém automaticky nabídne emailový kontakt přes formulář ve vašem profilu.
    </li>
</ul>
<br />

<#if PARAMS.preview?exists>
    <fieldset>
        <legend>Náhled</legend>
        <@bazarlib.showBazaarAd PREVIEW, USER />
    </fieldset>
</#if>
<br />

<form action="${URL.make("/edit")}" method="POST" name="form">
    <table cellpadding="5" class="siroka">
        <tr>
            <td class="required">Titulek</td>
            <td>
                <input tabindex="1" type="text" name="title" size="40" value="${PARAMS.title?if_exists?html}">
                <div class="error">${ERRORS.title?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td class="required">Typ inzerátu</td>
            <td>
                <@lib.showOption "type", "sell", "Prodej", "radio", "tabindex='2'" />
                <@lib.showOption "type", "buy", "Koupě", "radio", "tabindex='3'" />
                <div class="error">${ERRORS.type?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td>Cena</td>
            <td>
                <input tabindex="4" type="text" name="price" size="40" value="${PARAMS.price?if_exists?html}">
                <div class="error">${ERRORS.price?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td>Kontakt</td>
            <td>
                <input tabindex="5" type="text" name="contact" size="40" value="${PARAMS.contact?if_exists?html}">
                <div class="error">${ERRORS.contact?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td class="required">Obsah inzerátu</td>
            <td>
                <div class="form-edit">
                    <a href="javascript:insertAtCursor(document.form.text, '<b>', '</b>');" id="serif" title="Vložit značku tučně"><b>B</b></a>
                    <a href="javascript:insertAtCursor(document.form.text, '<i>', '</i>');" id="serif" title="Vložit značku kurzíva"><i>I</i></a>
                    <a href="javascript:insertAtCursor(document.form.text, '<a href=&quot;&quot;>', '</a>');" id="mono" title="Vložit značku odkazu">&lt;a&gt;</a>
                    <a href="javascript:insertAtCursor(document.form.text, '<p>', '</p>');" id="mono" title="Vložit značku odstavce">&lt;p&gt;</a>
                    <a href="javascript:insertAtCursor(document.form.text, '<pre>', '</pre>');" id="mono" title="Vložit značku formátovaného textu. Vhodné pro konfigurační soubory či výpisy.">&lt;pre&gt;</a>
                    <a href="javascript:insertAtCursor(document.form.text, '<code>', '</code>');" id="mono" title="Vložit značku pro písmo s pevnou šířkou">&lt;code&gt;</a>
                </div>
                <textarea tabindex="6" name="text" class="siroka" rows="20">${PARAMS.text?if_exists?html}</textarea><br>
                <div class="error">${ERRORS.text?if_exists}</div>
            </td>
        </tr>
        <#if PARAMS.action=="edit" || PARAMS.action="edit2" >
            <tr>
                <td>
                    Popis změny
                    <a class="info" href="#">?<span class="tooltip">Text bude zobrazen v historii dokumentu</span></a>
                </td>
                <td>
                    <input tabindex="5" type="text" name="rev_descr" size="40" value="${PARAMS.rev_descr?if_exists}">
                    <div class="error">${ERRORS.rev_descr?if_exists}</div>
                </td>
            </tr>
        </#if>
        <tr>
            <td colspan="2" align="center">
                <input tabindex="7" type="submit" name="preview" value="Náhled">
                <input tabindex="8" type="submit" name="submit" value="Dokonči">
            </td>
        </tr>
    </table>
    <#if PARAMS.action=="add" || PARAMS.action="add2" >
        <input type="hidden" name="action" value="add2">
    <#else>
        <input type="hidden" name="action" value="edit2">
    </#if>
    <input type="hidden" name="rid" value="${RELATION.id}">
</form>

<#include "/include/napoveda-k-auto-formatovani.txt">

<#include "../footer.ftl">
