<#include "../header.ftl">

<@lib.showMessages/>

<p>Na této stánce si můžete upravit seznam vašich
oblíbených odkazů. Oblíbené odkazy jsou blogy
či stránky, které pravidelně sledujete a doporučujete
čtenářům vašeho blogu. Zobrazí se v pravém sloupci.
<a href="/blog/${BLOG.subType}">Zpět na váš blog</a>.</p>

<#assign LINKS = BLOG_XML.data.custom.links>
<#if LINKS?size!=0>
    <h2>Odkazy</h2>
    <table border="0">
        <#list LINKS.link as link>
            <tr>
                <td><a href="${link}">${link.@caption}</a></td>
                <td>
                    Akce:
                    <a href="${URL.make("/blog/edit/"+REL_BLOG.id+"?position="+link_index+"&amp;action=editLink")}">upravit</a>
                    <a href="${URL.make("/blog/edit/"+REL_BLOG.id+"?position="+link_index+"&amp;action=rmLink")}">smazat</a>
                    <#if link_index!=0>
                        <a href="${URL.make("/blog/edit/"+REL_BLOG.id+"?position="+link_index+"&amp;action=mvLinkUp")}">nahoru</a>
                    </#if>
                    <#if link_has_next>
                        <a href="${URL.make("/blog/edit/"+REL_BLOG.id+"?position="+link_index+"&amp;action=mvLinkDown")}">dolů</a>
                    </#if>
                </td>
            </tr>
        </#list>
    </table>
<#else>
</#if>

<form action="${URL.make("/blog/edit/"+REL_BLOG.id)}" method="POST" name="form">
<h3>Vložit nový odkaz</h3>
<table border="0">
    <tr>
        <td class="required">URL</td>
        <td>
            <input type="text" name="url" title="URL odkazu" value="${PARAMS.url?if_exists}" size="30">
            <div class="error">${ERRORS.url?if_exists}</div>
        </td>
    </tr>
    <tr>
        <td class="required">Popis</td>
        <td>
            <input type="text" name="title" title="Popis odkazu" value="${PARAMS.title?if_exists}" size="30">
            <div class="error">${ERRORS.title?if_exists}</div>
        </td>
    </tr>
    <tr>
        <td><input type="submit" name="finish" value="Vytvořit"></td>
        <td></td>
    </tr>
</table>
<input type="hidden" name="action" value="addLink">
</form>


<#include "../footer.ftl">
