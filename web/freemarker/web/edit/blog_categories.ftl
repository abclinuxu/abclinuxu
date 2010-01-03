<#include "../header.ftl">

<@lib.showMessages/>

<p>Na této stánce si můžete upravovat seznam vašich
kategorií zápisů. Můžete zde přidat novou kategorii
nebo přejmenovat existující kategorii. Mazání prozatím
není dostupné. <a href="/blog/${BLOG.subType}">Zpět na váš blog</a>.</p>

<h2>Kategorie</h2>

<table border="0">
    <#list CATEGORIES as category>
        <tr>
            <td>
            <#if category.url??>
                <a href="/blog/${BLOG.subType + "/" + category.url}">${category.name}</a>
            <#else>
                ${category.name}
            </#if>
            </td>
            <td><a href="${URL.make("/blog/edit/"+REL_BLOG.id+"?cid="+category.id+"&amp;action=editCategory")}">upravit</a></td>
        </tr>
    </#list>
</table>

<@lib.addForm URL.make("/blog/edit/"+REL_BLOG.id), "name='form'">
    <@lib.addInput true, "category", "Název", 20 />
    <@lib.addSubmit "Vytvořit", "finish" />
    <@lib.addHidden "action", "addCategory" />
</@lib.addForm>

<#include "../footer.ftl">
