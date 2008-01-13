<#include "../header.ftl">

<@lib.showMessages/>

<p>Na této stánce si můžete upravovat seznam vašich
kategorií zápisů. Můžete zde přidat novou kategorii
nebo přejmenovat existující kategorii. Mazání prozatím
není dostupné. <a href="/blog/${BLOG.subType}">Zpět na váš blog</a>.</p>

<h2>Kategorie</h2>

<form action="${URL.make("/blog/edit/"+REL_BLOG.id)}" method="POST" name="form">

<table border="0">
    <#list CATEGORIES as category>
        <tr>
            <td>
            <#if category.url?exists>
                <a href="/blog/${BLOG.subType + "/" + category.url}">${category.name}</a>
            <#else>
                ${category.name}
            </#if>
            </td>
            <td><a href="${URL.make("/blog/edit/"+REL_BLOG.id+"?cid="+category.id+"&amp;action=editCategory")}">upravit</a></td>
        </tr>
    </#list>
    <tr>
        <td><input type="text" name="category" size="20"></td>
        <td><input type="submit" name="finish" value="Vytvořit"></td>
    </tr>
</table>

<input type="hidden" name="action" value="addCategory">
</form>


<#include "../footer.ftl">
