<#import "../macros.ftl" as lib>

<#assign plovouci_sloupec>
        <div class="s_nadpis">Struktura adresářů</div>
        <div class="s_sekce">
            <#assign doc=TOOL.asNode(MANAGED.data), path="">
            <#macro dir>
                <#local spath=path>
                <#assign path=path+"/"+(.node.@name)>
                <li><a href="${URL.noPrefix("/lide/"+MANAGED.login+"/zalozky?path="+path+"&amp;orderBy="+PARAMS.orderBy?default("title"))}">${.node.@name}</a>
                    <#if .node.dir?size gt 0><ul><#recurse .node></ul></#if>
                </li>
                <#assign path=spath>
            </#macro>
            <#macro @element></#macro>
            <ul id="treemenu1" class="treeview">
                <li><a href="${URL.noPrefix("/lide/"+MANAGED.login+"/zalozky?orderBy="+PARAMS.orderBy?default("title"))}"><i>kořenový adresář</i></a>
                    <#if doc.data.links.dir?size gt 0><ul><#recurse doc.data.links></ul></#if>
                </li>
            </ul>
        </div>
        <script type="text/javascript">
            ddtreemenu.createTree("treemenu1", true);
            ddtreemenu.flatten('treemenu1', 'expand');
        </script>
        <#if USER?exists && MANAGED.id==USER.id>
            <div class="s_nadpis">Nástroje</div>
            <div class="s_sekce">
                <ul>
                    <li><a href="${URL.noPrefix("/EditBookmarks/"+MANAGED.id+"?action=addLink&amp;path="+PARAMS.path?if_exists)}">Přidat záložku</a></li>
                </ul>
            </div>
        </#if>
</#assign>
<#assign html_header>
    <script type="text/javascript" src="/data/site/treemenu.js"></script>
</#assign>

<#include "../header.ftl">

<h1>Seznam záložek</h1>

<#if USER?exists && MANAGED.id==USER.id>
    <p>
    Zde můžete spravovat seznam svých záložek. Do záložek
    si můžete umístit libovolný dokument na AbcLinuxu přes
    menu v hlavičce stránky, odkud máte ke svým záložkám
    také rychlý přístup. Díky tomu jsou vaše oblíbené
    stránky kdykoliv při ruce.
    </p>
<#else>
    <p>
    Zde si můžete prohlédnout záložky uživatele <@lib.showUser MANAGED/>.
    </p>
</#if>

<@lib.showMessages/>

<#assign directory=PARAMS.path?default("/")>
<#if directory==""><#assign directory="/"></#if>
<p><b>Adresář: ${directory}</b></p>

<#if BOOKMARKS?exists && (BOOKMARKS?size > 0)>
    <p>V tomto adresáři záložek jsou následující stránky:</p>
    <form action="${URL.noPrefix("/EditBookmarks/"+MANAGED.id)}" method="POST">
    <table border="1">
        <thead>
            <tr>
                <#if USER?exists && MANAGED.id==USER.id><th></th></#if>
                <th><a href="${URL.noPrefix("/lide/"+MANAGED.login+"/zalozky?orderBy=title&amp;path="+PARAMS.path?if_exists)}">Název</a></th>
                <th><a href="${URL.noPrefix("/lide/"+MANAGED.login+"/zalozky?orderBy=type&amp;path="+PARAMS.path?if_exists)}">Typ</a></th>
                <th><a href="${URL.noPrefix("/lide/"+MANAGED.login+"/zalozky?orderBy=modified&amp;path="+PARAMS.path?if_exists)}">Poslední změna</a></th>
            </tr>
        </thead>
        <#list BOOKMARKS as item>
            <tr>
                <#if USER?exists && MANAGED.id==USER.id>
                    <#if item.relation?exists>
                        <td><input type="checkbox" name="rid" value="${item.relation.id}"></td>
                    <#else>
                        <td><input type="checkbox" name="url" value="${item.url}"></td>
                    </#if>
                </#if>
                <td>
                    <#if item.url?exists>
                        <a rel="nofollow" href="${item.url}">${item.title}</a>
                    <#elseif item.relation?exists && item.relation.initialized>
                        <a href="${URL.getRelationUrl(item.relation, item.prefix)}">${item.title}</a>
                    <#else>
                        <strike>${item.title}</strike>
                    </#if>
                </td>
                <td>${item.getNiceType()}</td>
                <td>
                    <#if item.relation?exists && item.relation.initialized>
                        ${DATE.show(item.relation.child.updated?default(item.relation.child.created), "SMART")}
                    <#elseif item.url?exists>
                        ---
                    <#else>
                        smazáno
                    </#if>
                </td>
            </li>
        </#list>
        </table>
        <#if USER?exists && MANAGED.id==USER.id>
            <p>
                <input type="hidden" name="path" value="${PARAMS.path?if_exists}">
                <input type="hidden" name="action" value="manage">
                <input type="hidden" name="ticket" value="${USER.getSingleProperty('ticket')}">
                <input type="submit" name="remove" value="Odstranit ze záložek">
            </p>
            <p>
                Cílový adresář:
                <select name="targetPath">
                    <option>/</option>
                    <#assign path="">
                    <#macro dir>
                        <#local spath=path>
                        <#assign path=path+"/"+(.node.@name)>
                            <option>${path}</option>
                            <#recurse .node>
                        <#assign path=spath>
                    </#macro>
                    <#recurse doc.data.links>
                </select>
                <input type="submit" name="move" value="Přesunout záložky">
            </p>
        </#if>
    </form>
<#else>
    <p>Zde nejsou žádné záložky.</p>
</#if>

<#if USER?exists && MANAGED.id==USER.id>
    <hr />

    <h2>Správa adresářů</h2>
    <form action="${URL.noPrefix("/EditBookmarks/"+MANAGED.id)}" method="POST">
    <p>
        Vytvořit v <b>${directory}</b> podadresář s názvem: <input type="text" name="directoryName" width="150"> <input type="submit" value="Vytvořit">
        <input type="hidden" name="ticket" value="${USER.getSingleProperty('ticket')}">
        <input type="hidden" name="path" value="${PARAMS.path?if_exists}">
        <input type="hidden" name="action" value="createDirectory">
    </p>
    </form>

    <#if directory!="/">
    <form action="${URL.noPrefix("/EditBookmarks/"+MANAGED.id)}" method="POST">
    <p>
        Smazat aktuální adresář včetně obsahu:
        <input type="submit" value="Smazat adresář">
        <input type="hidden" name="ticket" value="${USER.getSingleProperty('ticket')}">
        <input type="hidden" name="path" value="${PARAMS.path?if_exists}">
        <input type="hidden" name="action" value="removeDirectory">
    </p>
    </form>
    </#if>
</#if>

<#include "../footer.ftl">
