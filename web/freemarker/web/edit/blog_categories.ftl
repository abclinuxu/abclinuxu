<#include "../header.ftl">

<@lib.showMessages/>

<p>Na této stánce si mù¾ete upravovat seznam va¹ich
kategorií zápisù. Mù¾ete zde pøidat novou kategorii
nebo pøejmenovat existující kategorii. Mazání prozatím
není dostupné.</p>

<h2>Kategorie</h2>

<form action="${URL.make("/blog/edit/"+REL_BLOG.id)}" method="POST" name="form">

<table border="0">
    <#list CATEGORIES?keys as category>
        <tr>
            <td>${CATEGORIES[category]}</td>
            <td><a href="${URL.make("/blog/edit/"+REL_BLOG.id+"?cid="+category+"&amp;action=editCategory")}">upravit</a></td>
        </tr>
    </#list>
    <tr>
        <td><input type="text" name="category" size="20"></td>
        <td><input type="submit" name="finish" value="Vytvoøit"></td>
    </tr>
</table>

<input type="hidden" name="action" value="addCategory">
</form>


<#include "../footer.ftl">
