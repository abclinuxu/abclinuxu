<#include "../header.ftl">

<@lib.showMessages/>

<h1>�prava pojmu</h1>

<p>
    Zadejte jm�no pojmu a jeho vysv�tlen�. Pojem by m�l b�t bu� linuxov� nebo po��ta�ov�. Pokud je
    pot�eba, upravte jm�no pojmu. M�lo by b�t ps�no mal�mi p�smeny, velk� p�smena pou�ijte jen pro
    ust�len� zkratky (nap��klad SCSI). Prvn� znak jm�na pojmu mus� b�t p�smeno (a-z).
    URL se p�i �prav� nem�n�, pokud se v�m nel�b�, kontaktujte administr�tory.
</p>

<p>
    Pokud v popisu nepou�ijete formatovac� znaky &lt;br&gt; nebo &lt;p&gt;, syst�m automaticky
    nahrad� pr�zdn� ��dky zna�kou pro nov� odstavec.
</p>

<#if PARAMS.preview?exists>
    <h2>N�hled</h2>

    <fieldset>
        <legend>N�hled</legend>
        <h3>${PARAMS.name?if_exists}</h3>
        <#if PARAMS.desc?exists>
            <div class="dict-item">
            ${TOOL.render(PARAMS.desc,USER?if_exists)}
            </div>
        </#if>
    </fieldset>
</#if>

<form action="${URL.make("/edit")}" method="POST" name="dictForm">
    <table cellpadding="0" border="0" width="100%">
        <tr>
            <td class="required">Pojem</td>
            <td>
                <input tabindex="1" type="text" name="name" value="${PARAMS.name?if_exists}" size="30" maxlength="30" tabindex="1">
                <div class="error">${ERRORS.name?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td class="required">Popis</td>
            <td>
                <div class="form-edit">
                    <a href="javascript:insertAtCursor(document.dictForm.desc, '&lt;b&gt;', '&lt;/b&gt;');" id="serif" title="Vlo�it zna�ku tu�n�"><b>B</b></a>
                    <a href="javascript:insertAtCursor(document.dictForm.desc, '&lt;i&gt;', '&lt;/i&gt;');" id="serif" title="Vlo�it zna�ku kurz�va"><i>I</i></a>
                    <a href="javascript:insertAtCursor(document.dictForm.desc, '&lt;a href=&quot;&quot;&gt;', '&lt;/a&gt;');" id="mono" title="Vlo�it zna�ku odkazu">&lt;a&gt;</a>
                    <a href="javascript:insertAtCursor(document.dictForm.desc, '&lt;p&gt;', '&lt;/p&gt;');" id="mono" title="Vlo�it zna�ku odstavce">&lt;p&gt;</a>
                    <a href="javascript:insertAtCursor(document.dictForm.desc, '&lt;pre&gt;', '&lt;/pre&gt;');" id="mono" title="Vlo�it zna�ku form�tovan�ho textu. Vhodn� pro konfigura�n� soubory �i v�pisy.">&lt;pre&gt;</a>
                    <a href="javascript:insertAtCursor(document.dictForm.desc, '&lt;code&gt;', '&lt;/code&gt;');" id="mono" title="Vlo�it zna�ku pro p�smo s pevnou ���kou">&lt;code&gt;</a>
                    <a href="javascript:insertAtCursor(document.dictForm.desc, '&amp;lt;', '');" id="mono" title="Vlo�it p�smeno &lt;">&lt;</a>
                    <a href="javascript:insertAtCursor(document.dictForm.desc, '&amp;gt;', '');" id="mono" title="Vlo�it p�smeno &gt;">&gt;</a>
                </div>

                <textarea tabindex="2" name="desc" class="siroka" rows="20" tabindex="2">${PARAMS.desc?if_exists?html}</textarea>
                <div class="error">${ERRORS.desc?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td></td>
            <td>
                <input tabindex="3" type="submit" name="preview" value="N�hled">
                <input tabindex="4" type="submit" name="submit" value="Dokon�i">
            </td>
        </tr>
    </table>
    <input type="hidden" name="action" value="edit2">
    <input type="hidden" name="rid" value="${RELATION.id}">
</form>

<#include "../footer.ftl">
