<#include "../header.ftl">

<@lib.showMessages/>

<p>Na t�to st�nce si m��ete upravovat seznam va�ich
kategori� z�pis�. M��ete zde p�idat novou kategorii
nebo p�ejmenovat existuj�c� kategorii. Maz�n� prozat�m
nen� dostupn�.</p>

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
        <td><input type="submit" name="finish" value="Vytvo�it"></td>
    </tr>
</table>

<input type="hidden" name="action" value="addCategory">
</form>


<#include "../footer.ftl">
