<#include "../header.ftl">

<@lib.showMessages/>

<p>
    Na této stránce můžete nahrát obrázek. Maximální velikost je omezena
    na půl megabajtu, podporovány jsou formáty JPG, PNG a GIF (pro obrázky
    programů je nejvhodnější formát PNG).
</p>

<form action="${URL.make("/desktopy/edit")}" method="POST" enctype="multipart/form-data">
    <table cellpadding="0" border="0" style="margin-top: 1em;">
        <tr>
            <td class="required">Titulek</td>
            <td>
                <input type="text" name="name" value="${PARAMS.name?if_exists}" size="40" tabindex="1">
                <div class="error">${ERRORS.name?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td class="required">Obrázek</td>
            <td>
                <input type="file" name="screenshot" size="40" tabindex="2">
                <div class="error">${ERRORS.screenshot?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td width="60">Popis</td>
            <td>
                <textarea name="desc" class="siroka" tabindex="3">${PARAMS.desc?if_exists?html}</textarea>
                <div class="error">${ERRORS.desc?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td colspan="2" align="left">
                <input type="submit" name="submit" value="Dokonči" tabindex="4">
            </td>
        </tr>
    </table>
    <input type="hidden" name="action" value="add2">
</form>

<#include "../footer.ftl">