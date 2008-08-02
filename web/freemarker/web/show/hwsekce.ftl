<#if USER?exists && TOOL.xpath(CATEGORY,"//monitor/id[text()='"+USER.id+"']")?exists>
    <#assign monitorState="Přestaň sledovat"><#else><#assign monitorState="Sleduj sekci">
</#if>
<#assign plovouci_sloupec>
    <div class="s_sekce">
        <ul>
            <#if USER?exists && TOOL.permissionsFor(USER, RELATION).canCreate()>
                <li><a href="${URL.make("/edit/"+RELATION.id+"?action=add")}">Vložit novou položku</a></li>
            </#if>
            <li><a href="/pozadavky?url=${URL.getRelationUrl(RELATION)?url}&categoryPosition=4#form">Požádat o vytvoření podsekce</a></li>
            <li>
                 <a href="${URL.make("/EditMonitor/"+RELATION.id+"?action=toggle"+TOOL.ticket(USER?if_exists, false))}">${monitorState}</a>
                 <span title="Počet lidí, kteří sledují tuto sekci">(${TOOL.getMonitorCount(CATEGORY.data)})</span>
                 <a class="info" href="#">?<span class="tooltip">Zašle upozornění na váš email při nové položce v této a v podřazených sekcích.</span></a>
             </li>
            <#if USER?exists && TOOL.permissionsFor(USER, RELATION).canModify()>
                <hr />
                <li>
                    <a href="${URL.make("/EditCategory/"+RELATION.id+"?action=add")}">mkdir</a>,
                    <a href="${URL.make("/EditCategory/"+RELATION.id+"?action=edit")}">edit</a>,
                    <a href="${URL.noPrefix("/EditRelation/"+RELATION.id+"?action=remove&amp;prefix="+URL.prefix)}">rmdir</a>,
                    <a href="${URL.noPrefix("/EditRelation/"+RELATION.id+"?action=setURL2")}">url</a>,
                    <a href="${URL.noPrefix("/SelectRelation?rid="+RELATION.id+"&amp;url=/EditRelation&amp;action=add&amp;prefix="+URL.prefix)}">link</a>
                </li>
                <li><a href="${URL.noPrefix("/SelectRelation?rid="+RELATION.id+"&amp;prefix="+URL.prefix+"&amp;url=/EditRelation&amp;action=move")}">Přesunout sekci</a></li>
                <li><a href="${URL.noPrefix("/EditRelation/"+RELATION.id+"?action=moveAll&amp;prefix="+URL.prefix)}">Přesunout obsah</a></li>
            </#if>
        </ul>
    </div>
</#assign>
<#assign html_header>
    <script type="text/javascript" src="/data/site/treemenu.js"></script>
</#assign>

<#include "../header.ftl">

<div class="hw">

<@lib.advertisement id="gg-hw-sekce" />

<h1>${CATEGORY.title}</h1>

<@lib.showMessages/>

<#if TOOL.xpath(CATEGORY,"data/note")?exists>
 ${TOOL.render(TOOL.element(CATEGORY.data,"data/note"),USER?if_exists)}
</#if>

<#if (CATEGORIES?exists && CATEGORIES?size > 0)>
    <#if (DEPTH > 1)>
        <p>
            <a href="javascript:ddtreemenu.flatten('treeMenuHw', 'expand')">Vše rozbalit</a> |
            <a href="javascript:ddtreemenu.flatten('treeMenuHw', 'contact')">Vše sbalit</a>
            <a class="info" href="#">?<span class="tooltip">
                Kliknutím na ikonku adresáře rozbalíte podmenu. Kliknutím na název kategorie do ní rovnou vstoupíte.
            </span></a>
        </p>
    </#if>

    <@lib.listTree CATEGORIES, "treeMenuHw" />

    <script type="text/javascript">
      ddtreemenu.createTree("treeMenuHw", true)
    </script>
</#if>

<#assign map=TOOL.groupByType(CHILDREN, "Item")>
<#if map.article?exists>
    <#list SORT.byDate(map.article, "DESCENDING") as clanek>
        <@lib.showArticle clanek, "CZ_FULL" />
        <hr />
    </#list>
    <br />
</#if>

<#if map.make?exists>
    <table class="hw-polozky">
      <thead>
        <tr>
            <td class="td-nazev">Jméno</td>
            <td class="td-meta">Podpora</td>
            <td class="td-meta">Zastaralý</td>
            <td class="td-datum">Poslední úprava</td>
        </tr>
      </thead>
      <tbody>
        <#list SORT.byName(map.make) as polozka>
            <tr>
                <td>
		            <a href="${polozka.url?default("/show/"+polozka.id)}">${TOOL.childName(polozka)}</a>
        		</td>
                <td class="td-meta">
		            <#assign support=TOOL.xpath(polozka.child,"/data/support")?default("UNDEFINED")>
                    <#switch support>
                        <#case "complete">kompletní<#break>
                        <#case "partial">částečná<#break>
                        <#case "none">žádná<#break>
                        <#default>
                    </#switch>
		        </td>
                <td class="td-meta"><#if TOOL.xpath(polozka.child,"/data/outdated")?exists>ano</#if></td>
                <td class="td-datum">${DATE.show(polozka.child.updated,"CZ_FULL")}</td>
            </tr>
        </#list>
      </tbody>
    </table>
</#if>

</div>

<@lib.advertisement id="arbo-sq" />
<@lib.advertisement id="hosting90" />

<#include "../footer.ftl">
