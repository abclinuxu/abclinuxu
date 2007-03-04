<#import "../macros.ftl" as lib>
<#assign html_header>
    <script type="text/javascript" src="/data/site/treemenu.js"></script>
</#assign>
<#assign plovouci_sloupec>
    <div class="s_sekce">
        <ul>
            <#if CATEGORY.isOpen()>
                <li><a href="${URL.make("/edit/"+RELATION.id+"?action=add")}">Vložit novou položku</a></li>
            </#if>
            <li><a href="/clanky/show/3500?text=sekce+${RELATION.url}">Požádat o vytvoření podsekce</a></li>
            <li><a href="/software/alternativy">Alternativy k aplikacím</a></li>
            <#if USER?exists && USER.hasRole("category admin")>
                <hr />
                <li>
                    <a href="${URL.noPrefix("/EditCategory?action=edit&amp;rid="+RELATION.id+"&amp;categoryId="+CATEGORY.id)}">edit</a>,
                    <a href="${URL.noPrefix("/EditCategory?action=add&amp;rid="+RELATION.id+"&amp;categoryId="+CATEGORY.id)}">mkdir</a>,
                    <a href="${URL.noPrefix("/EditRelation/"+RELATION.id+"?action=remove&amp;prefix="+URL.prefix)}">rmdir</a>
                </li>
            </#if>
            <#if USER?exists && USER.hasRole("move relation")>
                <li>
                    <a href="${URL.noPrefix("/SelectRelation?rid="+RELATION.id+"&amp;prefix="+URL.prefix+"&amp;url=/EditRelation&amp;action=move")}">Přesunout sekci</a>
                </li>
                <li>
                    <a href="${URL.noPrefix("/EditRelation/"+RELATION.id+"?action=moveAll&amp;prefix="+URL.prefix)}">Přesunout obsah</a>
                </li>
            </#if>
        </ul>
    </div>

    <div class="s_nadpis">
      <a class="info" href="#">?<span class="tooltip">Kliknutím na kategorii (např. <b><i>Uživatelské rozhraní</i></b>) rozbalíte seznam filtrů.</span></a>
      Filtr
    </div>

    <div class="s_sekce">
    <form action="${RELATION.url?default("/software/show/"+RELATION.id)}" method="GET">
        <#assign userInterfaces = FILTERS.ui?default([])>
        <div class="filterHeader" onclick="prepni_plochu('ui')">Uživatelské rozhraní (${userInterfaces?size})</div>
        <div class="collapsible tree hidden" id="ui">
            <@lib.showOption2 "ui", "xwindows", UI_PROPERTY["xwindows"], "checkbox", userInterfaces />
            <div>
                <@lib.showOption2 "ui", "qt", UI_PROPERTY["qt"], "checkbox", userInterfaces />
                <div>
                    <@lib.showOption2 "ui", "kde", UI_PROPERTY["kde"], "checkbox", userInterfaces />
                </div>
            </div>
            <div>
                <@lib.showOption2 "ui", "gtk", UI_PROPERTY["gtk"], "checkbox", userInterfaces />
                <div>
                    <@lib.showOption2 "ui", "gnome", UI_PROPERTY["gnome"], "checkbox", userInterfaces />
                </div>
            </div>
            <div>
                <@lib.showOption2 "ui", "motif", UI_PROPERTY["motif"], "checkbox", userInterfaces />
            </div>
            <div>
                <@lib.showOption2 "ui", "java", UI_PROPERTY["java"], "checkbox", userInterfaces />
            </div>
            <div>
                <@lib.showOption2 "ui", "tk", UI_PROPERTY["tk"], "checkbox", userInterfaces />
            </div>

            <@lib.showOption2 "ui", "console", UI_PROPERTY["console"], "checkbox", userInterfaces />
            <div>
                <@lib.showOption2 "ui", "cli", UI_PROPERTY["cli"], "checkbox", userInterfaces />
            </div>
            <div>
                <@lib.showOption2 "ui", "tui", UI_PROPERTY["tui"], "checkbox", userInterfaces />
            </div>
            <div>
                <@lib.showOption2 "ui", "grconsole", UI_PROPERTY["grconsole"], "checkbox", userInterfaces />
            </div>
        </div>

        <#assign licenses = FILTERS.license?default([])>
        <div class="filterHeader" onclick="prepni_plochu('license')">Licence (${licenses?size})</div>
        <div class="collapsible hidden" id="license">
            <@lib.showOption2 "license", "gpl", LICENSE_PROPERTY["gpl"], "checkbox", licenses /><br>
            <@lib.showOption2 "license", "lgpl", LICENSE_PROPERTY["lgpl"], "checkbox", licenses /><br>
            <@lib.showOption2 "license", "bsd", LICENSE_PROPERTY["bsd"], "checkbox", licenses /><br>
            <@lib.showOption2 "license", "mpl", LICENSE_PROPERTY["mpl"], "checkbox", licenses /><br>
            <@lib.showOption2 "license", "apl", LICENSE_PROPERTY["apl"], "checkbox", licenses /><br>
            <@lib.showOption2 "license", "oss", LICENSE_PROPERTY["oss"], "checkbox", licenses /><br>
            <@lib.showOption2 "license", "freeware", LICENSE_PROPERTY["freeware"], "checkbox", licenses /><br>
            <@lib.showOption2 "license", "commercial", LICENSE_PROPERTY["commercial"], "checkbox", licenses /><br>
            <@lib.showOption2 "license", "other", LICENSE_PROPERTY["other"], "checkbox", licenses /><br>
        </div>

        <input type="submit" value="Nastavit" class="button">
        <input type="hidden" name="action" value="filter">
	</form>
    </div>
</#assign>

<#include "../header.ftl">

<div class="sw">

<@lib.advertisement id="gg-sw-item" />

<h1>${TOOL.xpath(CATEGORY.data,"/data/name")}</h1>

<@lib.showMessages/>

<#if TOOL.xpath(CATEGORY,"data/note")?exists>
 ${TOOL.render(TOOL.element(CATEGORY.data,"data/note"),USER?if_exists)}
</#if>

<#if (CATEGORIES?exists && CATEGORIES?size > 0)>
    <#if (DEPTH > 1)>
        <p>
            <a href="javascript:ddtreemenu.flatten('treemenu1', 'expand')">Vše rozbalit</a> |
            <a href="javascript:ddtreemenu.flatten('treemenu1', 'contact')">Vše sbalit</a>
            <a class="info" href="#">?<span class="tooltip">
                Kliknutím na ikonku adresáře rozbalíte podmenu. Kliknutím na název kategorie do ní rovnou vstoupíte.
            </span></a>
        </p>
    </#if>

    <@lib.listTree CATEGORIES, "treemenu1" />

    <script type="text/javascript">
      ddtreemenu.createTree("treemenu1", true)
    </script>
</#if>

<#if ITEMS?exists>
    <@lib.showSoftwareList ITEMS />
</#if>

</div>

<#include "../footer.ftl">
