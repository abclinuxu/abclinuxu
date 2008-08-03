<#include "../header.ftl">

<@lib.showMessages/>

<h2>Vytvoření skupiny</h2>

<p>
Při vytváření skupiny dojde k vytvoření podportálu, který má vlastní sekci pro články, vlastní wiki, poradnu a sekci pro akce. Má vlastní administrátorskou skupinu, jejíž členové mohou definovat ostatní adminy.
</p>

<form action="${URL.make("/skupiny/edit")}" method="POST" name="portalForm" enctype="multipart/form-data">
    <table cellpadding="5" border="0">
        <tr>
            <td class="required">Název</td>
            <td>
                <input type="text" name="title" size="40" maxlength="50" tabindex="1" value="${PARAMS.title?if_exists?html}">
                <div class="error">${ERRORS.title?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td class="required">
                Stručný popis
                <a class="info" href="#">?<span class="tooltip">Text, který bude zobrazen na v postraním sloupci na jednotlivých stránkách skupiny.</span></a>
            </td>
            <td>
                <div class="form-edit">
                    <a href="javascript:insertAtCursor(document.portalForm.descShort, '<b>', '</b>');" id="serif" title="Vložit značku tučně"><b>B</b></a>
                    <a href="javascript:insertAtCursor(document.portalForm.descShort, '<i>', '</i>');" id="serif" title="Vložit značku kurzíva"><i>I</i></a>
                    <a href="javascript:insertAtCursor(document.portalForm.descShort, '<a href=&quot;&quot;>', '</a>');" id="mono" title="Vložit značku odkazu">&lt;a&gt;</a>
                    <a href="javascript:insertAtCursor(document.portalForm.descShort, '<p>', '</p>');" id="mono" title="Vložit značku odstavce">&lt;p&gt;</a>
                    <a href="javascript:insertAtCursor(document.portalForm.descShort, '<code>', '</code>');" id="mono" title="Vložit značku pro písmo s pevnou šířkou">&lt;code&gt;</a>
                </div>

                <textarea name="descShort" cols="80" rows="15" tabindex="2">${PARAMS.descShort?if_exists?html}</textarea>
                <div class="error">${ERRORS.descShort?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td class="required">
                Popis
                <a class="info" href="#">?<span class="tooltip">Text, který bude zobrazen na úvodní stránce skupiny a ve výpisu skupin.</span></a>
            </td>
            <td>
                <div class="form-edit">
                    <a href="javascript:insertAtCursor(document.portalForm.desc, '<b>', '</b>');" id="serif" title="Vložit značku tučně"><b>B</b></a>
                    <a href="javascript:insertAtCursor(document.portalForm.desc, '<i>', '</i>');" id="serif" title="Vložit značku kurzíva"><i>I</i></a>
                    <a href="javascript:insertAtCursor(document.portalForm.desc, '<a href=&quot;&quot;>', '</a>');" id="mono" title="Vložit značku odkazu">&lt;a&gt;</a>
                    <a href="javascript:insertAtCursor(document.portalForm.desc, '<p>', '</p>');" id="mono" title="Vložit značku odstavce">&lt;p&gt;</a>
                    <a href="javascript:insertAtCursor(document.portalForm.desc, '<code>', '</code>');" id="mono" title="Vložit značku pro písmo s pevnou šířkou">&lt;code&gt;</a>
                </div>

                <textarea name="desc" cols="80" rows="15" tabindex="2">${PARAMS.desc?if_exists?html}</textarea>
                <div class="error">${ERRORS.desc?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td>
                Ikonka
                <a class="info" href="#">?<span class="tooltip">Ikonka zobrazovaná ve výpisu skupin a v pravém sloupci ve skupině.</span></a>
            </td>
            <td>
                <input type="file" name="icon" size="20" tabindex="3"> Rozměry maximálně 100&times;100.
                <div class="error">${ERRORS.icon?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td class="required">
                UID prvního admina
                <a class="info" href="#">?<span class="tooltip">Počáteční člen administrátorské skupiny.</span></a>
            </td>
            <td>
                <input type="text" name="admin" size="40" maxlength="50" tabindex="4" value="${PARAMS.admin?if_exists}">
                <div class="error">${ERRORS.admin?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td class="required">
                URL
                <a class="info" href="#">?<span class="tooltip">Například /gentoo.</span></a>
            </td>
            <td>
                <input type="text" name="url" size="40" maxlength="50" tabindex="5" value="${PARAMS.url?if_exists}">
                <div class="error">${ERRORS.url?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td width="120">&nbsp;</td>
            <td><input type="submit" VALUE="Vytvoř" tabindex="6"></td>
        </tr>
    </table>
    <input type="hidden" name="action" value="add2">
</form>

<#include "../footer.ftl">