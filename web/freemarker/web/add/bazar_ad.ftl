<#include "../header.ftl">

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
        nap��klad prodej hardwaru, linuxov�ho softwaru, odborn� literatury, ply�ov�ch
        tu���k� apod.
    </li>
    <li>
        Je zak�z�no vkl�dat inzer�t se stejn�m �i podobn�m obsahem d��ve ne� za cel�ch
        uplynul�ch 7 dn�.
    </li>
    <li>
        Inzer�t nesm� poru�ovat m�stn� z�kony.
    </li>
    <li>
        Ve v�pise inzer�t� se zobraz� titulek a typ (prodej, koup�, darov�n�), proto
        si na jeho obsahu dejte z�le�et. V p��pad� pot�eby inzer�t upravte.
    </li>
    <li>
        Nezapome�te do t�la inzer�tu napsat, jak�m zp�sobem v�s maj� z�jemci kontaktovat.
        Ve va�em profilu je tla��tko, p�es kter� v�m mohou poslat email, tak�e jej nemus�te
        zve�ej�ovat a vystavovat se riziku spambot�.
    </li>
</ul>


<#if PARAMS.preview?exists>
    <fieldset style="margin-top: 1em;">
        <legend>N�hled</legend>
        <h2 style="margin-bottom: 1em;">${TOOL.xpath(PREVIEW, "/data/title")?if_exists}</h2>
        <div>
            ${TOOL.render(TOOL.xpath(PREVIEW.data,"/data/text")?if_exists, USER?if_exists)}
        </div>
    </fieldset>
</#if>


<form action="${URL.make("/edit")}" method="POST" name="form">
    <table cellpadding="5" style="margin-top:1em">
        <tr>
            <td class="required">Titulek</td>
            <td>
                <input tabindex="1" type="text" name="title" size="80" value="${PARAMS.title?if_exists?html}">
                <div class="error">${ERRORS.title?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td class="required">Typ inzer�tu</td>
            <td>
                <@lib.showOption "type", "buy", "Prodej", "radio", "tabindex='2'" />
                <@lib.showOption "type", "give", "Darov�n�", "radio", "tabindex='3'" />
                <@lib.showOption "type", "sell", "Koup�", "radio", "tabindex='4'" />
                <div class="error">${ERRORS.type?if_exists}</div>
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
                <textarea tabindex="3" name="text" cols="80" rows="20">${PARAMS.text?if_exists?html}</textarea><br>
                <div class="error">${ERRORS.text?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td colspan="2" align="center">
                <input tabindex="4" type="submit" name="preview" value="N�hled">
                <input tabindex="5" type="submit" name="submit" value="Dokon�i">
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
