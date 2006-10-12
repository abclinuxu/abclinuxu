<#include "../header.ftl">
<#import "../misc/lib-bazar.ftl" as bazarlib>

<@lib.showMessages/>

<p>
    Pro inzer�ty plat� n�sleduj�c� pravidla, pros�me p�e�t�te si je
    a �i�te se jimi. V krajn�m p��pad� mohou administr�to�i v� inzer�t
    smazat.
</p>

<ul>
    <li>
        Inzer�ty jsou bezplatnou slu�bou �ten���m tohoto port�lu. V�d�le�n� �innost
        nen� povolena. V p��pad� pochybnost� rozhoduje provozovatel. P��padn� komer�n�
        lad�n� inzer�ty je nutn� p�edjednat dop�edu s inzertn�m odd�len�m provozovatele.
    </li>
    <li>
        Inzer�ty mus� b�t v souladu se zam��en�m tohoto port�lu. Vhodn� inzer�ty jsou
        nap��klad prodej hardwaru �i spot�ebn� elektroniky, linuxov�ho softwaru, odborn� literatury,
        ply�ov�ch tu���k� apod.
    </li>
    <li>
        Je zak�z�no vkl�dat inzer�t se stejn�m �i podobn�m obsahem d��ve ne� za cel�ch
        uplynul�ch 7 dn�. Inzer�t nesm� poru�ovat m�stn� z�kony.
    </li>
    <li>
        Inzer�ty budou po 30 dnech automaticky smaz�ny. Pokud ale usp�jete d��ve, sma�te
        pros�m inzer�t ru�n�.
    </li>
    <li>
        Do titulku napi�te, co prod�v�te �i hled�te. �ten��i se pak budou l�pe orientovat
        ve v�pise inzer�t�. Do kontaktu napi�te sv� telefonn� ��slo, ICQ apod. Nech�te-li
        jej pr�zdn�, syst�m automaticky nab�dne emailov� kontakt p�es formul�� ve va�em profilu.
    </li>
</ul>


<#if PARAMS.preview?exists>
    <fieldset style="margin-top: 1em;">
        <legend>N�hled</legend>
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
            <td class="required">Typ inzer�tu</td>
            <td>
                <@lib.showOption "type", "sell", "Prodej", "radio", "tabindex='2'" />
                <@lib.showOption "type", "buy", "Koup�", "radio", "tabindex='3'" />
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
            <td class="required">Obsah inzer�t</td>
            <td>
                <div class="form-edit">
                    <a href="javascript:insertAtCursor(document.form.text, '<b>', '</b>');" id="serif" title="Vlo�it zna�ku tu�n�"><b>B</b></a>
                    <a href="javascript:insertAtCursor(document.form.text, '<i>', '</i>');" id="serif" title="Vlo�it zna�ku kurz�va"><i>I</i></a>
                    <a href="javascript:insertAtCursor(document.form.text, '<a href=&quot;&quot;>', '</a>');" id="mono" title="Vlo�it zna�ku odkazu">&lt;a&gt;</a>
                    <a href="javascript:insertAtCursor(document.form.text, '<p>', '</p>');" id="mono" title="Vlo�it zna�ku odstavce">&lt;p&gt;</a>
                    <a href="javascript:insertAtCursor(document.form.text, '<pre>', '</pre>');" id="mono" title="Vlo�it zna�ku form�tovan�ho textu. Vhodn� pro konfigura�n� soubory �i v�pisy.">&lt;pre&gt;</a>
                    <a href="javascript:insertAtCursor(document.form.text, '<code>', '</code>');" id="mono" title="Vlo�it zna�ku pro p�smo s pevnou ���kou">&lt;code&gt;</a>
                </div>
                <textarea tabindex="4" name="text" cols="80" rows="20">${PARAMS.text?if_exists?html}</textarea><br>
                <div class="error">${ERRORS.text?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td colspan="2" align="center">
                <input tabindex="5" type="submit" name="preview" value="N�hled">
                <input tabindex="6" type="submit" name="submit" value="Dokon�i">
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
