<#include "../header.ftl">

<h1>Upload příloh</h1>

<@lib.showMessages/>

<form action="${URL.make("/inset/"+RELATION.id)}" method="POST" enctype="multipart/form-data">
    <table>
    <tr><td>Příloha</td><td><input type="file" name="attachment"></td></tr>
    <tr><td>Příloha</td><td><input type="file" name="attachment"></td></tr>
    <tr><td>Příloha</td><td><input type="file" name="attachment"></td></tr>
    <tr>
        <td>&nbsp;</td>
        <td>
            <input type="hidden" name="action" value="addFile2">
            <input type="submit" value="Nahrát">
        </td>
    </tr>
    </table>
</form>

<#include "../footer.ftl">
