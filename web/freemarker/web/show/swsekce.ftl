<#import "../macros.ftl" as lib>
<#assign plovouci_sloupec>
    <div class="s_sekce">
        <ul>
            <!-- <li><a href="/clanky/show/3500?text=sekce+${RELATION.id}">Po¾ádat o vytvoøení podsekce</a></li> -->
            <#if CATEGORY.isOpen()>
                <li><a href="${URL.make("/edit/"+RELATION.id+"?action=add")}">Vlo¾it novou polo¾ku</a></li>
            </#if>
            <li><a href="/software/alternativy">Alternativy k aplikacím</a></li>
            <li><a href="/History?type=software">Poslední upravené polo¾ky</a></li>
            <li>
                <form action="" method="get">
                    <input type="text" name="name" size="20">
                    <input type="hidden" name="action" value="search">
                    <input type="submit" value="Hledej aplikaci">
                </form>                
            </li>
            <#if USER?exists && USER.hasRole("category admin")>
                <hr />
                <li><a href="${URL.noPrefix("/EditCategory?action=add&amp;rid="+RELATION.id+"&amp;categoryId="+CATEGORY.id)}">mkdir</a>,
                    <a href="${URL.noPrefix("/EditCategory?action=edit&amp;rid="+RELATION.id+"&amp;categoryId="+CATEGORY.id)}">edit</a>,
                    <a href="${URL.noPrefix("/EditRelation/"+RELATION.id+"?action=remove&amp;prefix="+URL.prefix)}">rmdir</a>,
                    <a href="${URL.noPrefix("/SelectRelation?rid="+RELATION.id+"&amp;url=/EditRelation&amp;action=add&amp;prefix="+URL.prefix)}">link</a>
                </li>
            </#if>
            <#if USER?exists && USER.hasRole("move relation")>
                <li>
                    <a href="${URL.noPrefix("/SelectRelation?rid="+RELATION.id+"&amp;prefix="+URL.prefix+"&amp;url=/EditRelation&amp;action=move")}">Pøesunout sekci</a>
                </li>
                <li>
                    <a href="${URL.noPrefix("/EditRelation/"+RELATION.id+"?action=moveAll&amp;prefix="+URL.prefix)}">Pøesunout obsah</a>
                </li>
            </#if>
        </ul>
    </div>

    <div class="s_nad_h1"><div class="s_nad_pod_h1">
        <h1>Filtr</h1>
    </div></div>

    <div class="s_sekce">
    <form action="${RELATION.url?default("/software/show/"+RELATION.id)}" method="GET">
        <b>U¾ivatelské rozhraní:</b>

        <#assign uiType = FILTERS.ui?default("")>
        <div>
            <@lib.showOption2 "ui", "xwindows", UI_PROPERTY["xwindows"], "checkbox", uiType />
            <div>
                <@lib.showOption2 "ui", "qt", UI_PROPERTY["qt"], "checkbox", uiType />
                <div>
                    <@lib.showOption2 "ui", "kde", UI_PROPERTY["kde"], "checkbox", uiType />
                </div>
            </div>
            <div>
                <@lib.showOption2 "ui", "gtk", UI_PROPERTY["gtk"], "checkbox", uiType />
                <div>
                    <@lib.showOption2 "ui", "gnome", UI_PROPERTY["gnome"], "checkbox", uiType />
                </div>
            </div>
            <div>
                <@lib.showOption2 "ui", "motif", UI_PROPERTY["motif"], "checkbox", uiType />
            </div>
            <div>
                <@lib.showOption2 "ui", "java", UI_PROPERTY["java"], "checkbox", uiType />
            </div>
            <div>
                <@lib.showOption2 "ui", "tk", UI_PROPERTY["tk"], "checkbox", uiType />
            </div>
        </div>
        <div>
            <@lib.showOption2 "ui", "console", UI_PROPERTY["console"], "checkbox", uiType />
            <div>
                <@lib.showOption2 "ui", "cli", UI_PROPERTY["cli"], "checkbox", uiType />
            </div>
            <div>
                <@lib.showOption2 "ui", "tui", UI_PROPERTY["tui"], "checkbox", uiType />
            </div>
            <div>
                <@lib.showOption2 "ui", "grconsole", UI_PROPERTY["grconsole"], "checkbox", uiType />
            </div>
        </div>

        <b>Licence:</b>

        <#assign licenses = FILTERS.license?default("")><br>
        <@lib.showOption2 "license", "gpl", LICENSE_PROPERTY["gpl"], "checkbox", licenses /><br>
        <@lib.showOption2 "license", "lgpl", LICENSE_PROPERTY["lgpl"], "checkbox", licenses /><br>
        <@lib.showOption2 "license", "bsd", LICENSE_PROPERTY["bsd"], "checkbox", licenses /><br>
        <@lib.showOption2 "license", "mpl", LICENSE_PROPERTY["mpl"], "checkbox", licenses /><br>
        <@lib.showOption2 "license", "apl", LICENSE_PROPERTY["apl"], "checkbox", licenses /><br>
        <@lib.showOption2 "license", "oss", LICENSE_PROPERTY["oss"], "checkbox", licenses /><br>
        <@lib.showOption2 "license", "freeware", LICENSE_PROPERTY["freeware"], "checkbox", licenses /><br>
        <@lib.showOption2 "license", "commercial", LICENSE_PROPERTY["commercial"], "checkbox", licenses /><br>
        <@lib.showOption2 "license", "other", LICENSE_PROPERTY["other"], "checkbox", licenses /><br>

        <input type="submit" value="Potvrdit">
        <input type="hidden" name="action" value="filter">
	</form>
    </div>
</#assign>

<#include "../header.ftl">

<div class="sw">

<h1>${TOOL.xpath(CATEGORY.data,"/data/name")}</h1>

<@lib.showMessages/>

<#if TOOL.xpath(CATEGORY,"data/note")?exists>
 ${TOOL.render(TOOL.element(CATEGORY.data,"data/note"),USER?if_exists)}
</#if>

<#if CATEGORIES?exists>
    <p><small><b>Sekce</b></small></p> <!-- *** DOCASNE *** -->
      <ul>
        <#list SORT.byName(CATEGORIES) as sekce>
            <li><a href="${URL.make(sekce.url?default("/dir/"+sekce.id))}">${TOOL.childName(sekce)}</a></li>
        </#list>
      </ul>

</#if>

<#if ITEMS?exists>
    <table class="sw-polozky">
      <thead>
        <tr>
            <td class="td01">Jméno</td>
            <td class="td04">Poslední úprava</td>
        </tr>
      </thead>
      <tbody>
        <#list SORT.byName(ITEMS) as polozka>
            <tr>
                <td class="td01">
                    <a href="${URL.make(polozka.url?default("/show/"+polozka.id))}">${TOOL.childName(polozka)}</a>
                </td>
                <td class="td04">${DATE.show(polozka.child.updated,"CZ_FULL")}</td>
            </tr>
        </#list>
      </tbody>
    </table>
</#if>

</div>

<#include "../footer.ftl">
