<#include "../../header.ftl">

<@lib.showMessages/>

<h1>Změna role</h1>

<p>
    Vyberte si roli, pod kterou se chcete v redakčním systému pohybovat v tomto sezení.
</p>

<form action="${URL.noPrefix("/sprava/redakce/")}" method="POST">
    <table border="0">
        <tr>
            <td>
                <input type="radio" name="desiredRole" value="author">
                Autor - píše články, přijímá náměty
            </td>
        </tr>
        <tr>
            <td>
                <input type="radio" name="desiredRole" value="editor">
                Redaktor – spravuje autory a obsah, nemá právo vidět nebo upravovat finance
            </td>
        </tr>
        <tr>
            <td>
                <input type="radio" name="desiredRole" value="editorInChief">
                Šéfredaktor – spravuje veškerý obsah
            </td>
        </tr>
        <tr>
            <td>
                <input type="submit" value="Přepnout roli"/>
            </td>
        </tr>
    </table>

    <input type="hidden" name="action" value="switch2"/>
</form>

<#include "../../footer.ftl">