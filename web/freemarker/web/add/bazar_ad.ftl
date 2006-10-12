<#include "../header.ftl">
<#import "../misc/lib-bazar.ftl" as bazarlib>

<@lib.showMessages/>

<p>
    Pro inzeráty platí následující pravidla, prosíme pøeètìte si je
    a øiïte se jimi. V krajním pøípadì mohou administrátoøi vá¹ inzerát
    smazat.
</p>

<ul>
    <li>
        Inzeráty jsou bezplatnou slu¾bou ètenáøùm tohoto portálu. Výdìleèná èinnost
        není povolena. V pøípadì pochybností rozhoduje provozovatel. Pøípadné komerènì
        ladìné inzeráty je nutné pøedjednat dopøedu s inzertním oddìlením provozovatele.
    </li>
    <li>
        Inzeráty musí být v souladu se zamìøením tohoto portálu. Vhodné inzeráty jsou
        napøíklad prodej hardwaru èi spotøební elektroniky, linuxového softwaru, odborné literatury,
        ply¹ových tuèòákù apod.
    </li>
    <li>
        Je zakázáno vkládat inzerát se stejným èi podobným obsahem døíve ne¾ za celých
        uplynulých 7 dní. Inzerát nesmí poru¹ovat místní zákony.
    </li>
    <li>
        Inzeráty budou po 30 dnech automaticky smazány. Pokud ale uspìjete døíve, sma¾te
        prosím inzerát ruènì.
    </li>
    <li>
        Do titulku napi¹te, co prodáváte èi hledáte. Ètenáøi se pak budou lépe orientovat
        ve výpise inzerátù. Do kontaktu napi¹te své telefonní èíslo, ICQ apod. Necháte-li
        jej prázdný, systém automaticky nabídne emailový kontakt pøes formuláø ve va¹em profilu.
    </li>
</ul>


<#if PARAMS.preview?exists>
    <fieldset style="margin-top: 1em;">
        <legend>Náhled</legend>
        <@bazarlib.showBazaarAd PREVIEW, USER />
    </fieldset>
</#if>


<form action="${URL.make("/edit")}" method="POST" name="form">
    <table cellpadding="5" style="margin-top:1em">
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
                <@lib.showOption "type", "buy", "Koupì", "radio", "tabindex='3'" />
                <div class="error">${ERRORS.type?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td>Cena</td>
            <td>
                <input tabindex="2" type="text" name="price" size="40" value="${PARAMS.price?if_exists?html}">
                <div class="error">${ERRORS.price?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td>Kontakt</td>
            <td>
                <input tabindex="3" type="text" name="contact" size="40" value="${PARAMS.contact?if_exists?html}">
                <div class="error">${ERRORS.contact?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td class="required">Obsah inzerát</td>
            <td>
                <div class="form-edit">
                    <a href="javascript:insertAtCursor(document.form.text, '<b>', '</b>');" id="serif" title="Vlo¾it znaèku tuènì"><b>B</b></a>
                    <a href="javascript:insertAtCursor(document.form.text, '<i>', '</i>');" id="serif" title="Vlo¾it znaèku kurzíva"><i>I</i></a>
                    <a href="javascript:insertAtCursor(document.form.text, '<a href=&quot;&quot;>', '</a>');" id="mono" title="Vlo¾it znaèku odkazu">&lt;a&gt;</a>
                    <a href="javascript:insertAtCursor(document.form.text, '<p>', '</p>');" id="mono" title="Vlo¾it znaèku odstavce">&lt;p&gt;</a>
                    <a href="javascript:insertAtCursor(document.form.text, '<pre>', '</pre>');" id="mono" title="Vlo¾it znaèku formátovaného textu. Vhodné pro konfiguraèní soubory èi výpisy.">&lt;pre&gt;</a>
                    <a href="javascript:insertAtCursor(document.form.text, '<code>', '</code>');" id="mono" title="Vlo¾it znaèku pro písmo s pevnou ¹íøkou">&lt;code&gt;</a>
                </div>
                <textarea tabindex="4" name="text" cols="80" rows="20">${PARAMS.text?if_exists?html}</textarea><br>
                <div class="error">${ERRORS.text?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td colspan="2" align="center">
                <input tabindex="5" type="submit" name="preview" value="Náhled">
                <input tabindex="6" type="submit" name="submit" value="Dokonèi">
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
