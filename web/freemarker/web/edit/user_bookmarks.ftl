<#include "../header.ftl">

<h1>Seznam záložek</h1>

<p>
Zde můžete spravovat seznam svých záložek. Do záložek
si můžete umístit libovolný dokument na AbcLinuxu přes
menu v hlavičce stránky, odkud máte ke svým záložkám
také rychlý přístup. Díky tomu jsou vaše oblíbené
stránky kdykoliv při ruce.
</p>

<@lib.showMessages/>

<#if (BOOKMARKS?size > 0)>
    <p>Ve vašich záložkách jsou následující stránky:</p>
    <form action="${URL.noPrefix("/EditUser"+MANAGED.id+"?action=fromBookmarks")}" method="POST">
    <table border="1">
        <thead>
            <tr>
                <th></th>
                <th>Název</th>
                <th>Typ</th>
                <th>Poslední změna</th></th>
            </tr>
        </thead>
        <#list BOOKMARKS as item>
            <tr>
                <td><input type="checkbox" name="rid" value="${item.relation.id}"></td>
                <td>
                    <#if item.relation.initialized>
                        <a href="${URL.getRelationUrl(item.relation, item.prefix)}">${item.title}</a>
                    <#else>
                        <strike>${item.title}</strike>
                    </#if>
                </td>
                <td>
                    <#if item.type=='article'>článek
                    <#elseif item.type=='content'>dokument
                    <#elseif item.type=='dictionary'>pojem
                    <#elseif item.type=='discussion'>diskuse
                    <#elseif item.type=='driver'>ovladač
                    <#elseif item.type=='faq'>FAQ
                    <#elseif item.type=='hardware'>hardware
                    <#elseif item.type=='news'>zprávička
                    <#elseif item.type=='other'>ostatní
                    <#elseif item.type=='poll'>anketa
                    <#elseif item.type=='section'>sekce
                    <#elseif item.type=='software'>software
                    <#elseif item.type=='story'>blog
                    <#elseif item.type=='author'>autor
                    <#elseif item.type=='series'>seriál
                    </#if>
                </td>
                <td>
                    <#if item.relation.initialized>
                        ${DATE.show(item.relation.child.updated?default(item.relation.child.created), "SMART")}
                    <#else>
                        smazáno
                    </#if>
                </td>
            </li>
        </#list>
        </table>
        <p>
            <input type="hidden" name="ticket" value="${USER.getSingleProperty('ticket')}">
            <input type="submit" value="Odstranit ze záložek">
        </p>
    </form>
<#else>
    <p>Nemáte žádné záložky.</p>
</#if>

<#include "../footer.ftl">
