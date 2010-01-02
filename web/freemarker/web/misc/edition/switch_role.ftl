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
                <label>
                    <input type="radio" name="desiredRole" value="author">
                    Autor - píše články, přijímá náměty
                </label>
            </td>
        </tr>
        <tr>
            <td>
                <label>
                    <input type="radio" name="desiredRole" value="editor">
                    Redaktor – spravuje autory a obsah, nemá právo vidět nebo upravovat finance
                </label>
            </td>
        </tr>
        <tr>
            <td>
                <label>
                    <input type="radio" name="desiredRole" value="editorInChief">
                    Šéfredaktor – spravuje veškerý obsah
                </label>
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