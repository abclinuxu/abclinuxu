<#assign plovouci_sloupec>
    <div class="s_sekce">
        <ul>
            <li><a href="/clanky/show/3500?text=sekce+${RELATION.id}">Po¾ádejte o vytvoøení podsekce</a></li>
            <#if CATEGORY.isOpen()>
                <li><a href="${URL.make("/edit/"+RELATION.id+"?action=add")}">Vlo¾ novou polo¾ku</a></li>
            </#if>
            <#if USER?exists && USER.hasRole("category admin")>
                <li><a href="${URL.noPrefix("/EditCategory?action=add&amp;rid="+RELATION.id+"&amp;categoryId="+CATEGORY.id)}">mkdir</a>,
                    <a href="${URL.noPrefix("/EditCategory?action=edit&amp;rid="+RELATION.id+"&amp;categoryId="+CATEGORY.id)}">edit</a>,
                    <a href="${URL.noPrefix("/EditRelation/"+RELATION.id+"?action=remove&amp;prefix="+URL.prefix)}">rmdir</a>,
                    <a href="${URL.noPrefix("/SelectRelation?rid="+RELATION.id+"&amp;url=/EditRelation&amp;action=add&amp;prefix="+URL.prefix)}">link</a></li>
            </#if>
            <#if USER?exists && USER.hasRole("move relation")>
                <li><a href="${URL.noPrefix("/SelectRelation?rid="+RELATION.id+"&amp;prefix="+URL.prefix+"&amp;url=/EditRelation&amp;action=move")}">Pøesunout</a></li>
                <li><a href="${URL.noPrefix("/EditRelation/"+RELATION.id+"?action=moveAll&amp;prefix="+URL.prefix)}">Pøesuò obsah</a></li>
            </#if>
            <#if USER?exists && USER.hasRole("root")>
                <li><a href="${URL.noPrefix("/EditRelation/"+RELATION.id+"?action=showACL")}">ACL</a></li>
            </#if>
        </ul>
    </div>
    
    <div class="s_nad_h1"><div class="s_nad_pod_h1">
        <h1>Filtrovanie</h1>
    </div></div>

    <div class="s_sekce">
	<form action="${RELATION.url?default("/software/show/"+RELATION.id)}" method="GET">
	Uzivatelske rozhranie:
	<#assign uiType = filters.uiType?default("")>
	<ul>
	    <li><input type="checkbox" name="filterUIType" value="xwindows"<#if uiType!="" && uiType.contains("xwindows")> CHECKED</#if>>X Windows</input>
	    <ul><li><input type="checkbox" name="filterUIType" value="qt"<#if uiType!="" && uiType.contains("qt")> CHECKED</#if>>Qt/KDE</input></li>
	        <li><input type="checkbox" name="filterUIType" value="gtk"<#if uiType!="" && uiType.contains("gtk")> CHECKED</#if>>Gtk/Gnome</input></li></ul>
	    </li>
	    <li><input type="checkbox" name="filterUIType" value="console"<#if uiType!="" && uiType.contains("console")> CHECKED</#if>>Konzole</input>
	    <ul><li><input type="checkbox" name="filterUIType" value="ncurses"<#if uiType!="" && uiType.contains("ncurses")> CHECKED</#if>>ncurses</input></li></ul>
	    </li>
	</ul>
	<div id="more_filters">
	Licencia:
	<#assign licenses = filters.licenses?default("")>
	<ul>
	    <li><input type="checkbox" name="filterLicenses" value="gpl"<#if licenses!="" && licenses.contains("gpl")> CHECKED</#if>>GPL</input></li>
	    <li><input type="checkbox" name="filterLicenses" value="lgpl"<#if licenses!="" && licenses.contains("lgpl")> CHECKED</#if>>LGPL</input></li>
	    <li><input type="checkbox" name="filterLicenses" value="bsd"<#if licenses!="" && licenses.contains("bsd")> CHECKED</#if>>BSD</input></li>
	    <li><input type="checkbox" name="filterLicenses" value="mpl"<#if licenses!="" && licenses.contains("mpl")> CHECKED</#if>>MPL</input></li>
	    <li><input type="checkbox" name="filterLicenses" value="apl"<#if licenses!="" && licenses.contains("apl")> CHECKED</#if>>APL</input></li>
	    <li><input type="checkbox" name="filterLicenses" value="oss"<#if licenses!="" && licenses.contains("oss")> CHECKED</#if>>OSS compliant</input></li>
	    <li><input type="checkbox" name="filterLicenses" value="asis"<#if licenses!="" && licenses.contains("asis")> CHECKED</#if>>"as is"</input></li>
	    <li><input type="checkbox" name="filterLicenses" value="comerce"<#if licenses!="" && licenses.contains("comerce")> CHECKED</#if>>komercna</input></li>
	</ul>
	SW Platforma:
	<#assign swPlatforms = filters.swPlatforms?default("")>
	<ul>
	    <li><input type="checkbox" name="filterSWPlatforms" value="linux"<#if swPlatforms!="" && swPlatforms.contains("linux")> CHECKED</#if>>Linux</input></li>
	    <li><input type="checkbox" name="filterSWPlatforms" value="bsd"<#if swPlatforms!="" && swPlatforms.contains("bsd")> CHECKED</#if>>BSD</input></li>
	    <li><input type="checkbox" name="filterSWPlatforms" value="win32"<#if swPlatforms!="" && swPlatforms.contains("win32")> CHECKED</#if>>Windows</input></li>
	</ul>
	HW Platforma:
	<#assign hwPlatforms = filters.hwPlatforms?default("")>
	<ul>
	    <li><input type="checkbox" name="filterHWPlatforms" value="x86"<#if hwPlatforms!="" && hwPlatforms.contains("x86")> CHECKED</#if>>x86 (PC)</input></li>
	    <li><input type="checkbox" name="filterHWPlatforms" value="x64"<#if hwPlatforms!="" && hwPlatforms.contains("x64")> CHECKED</#if>>x64 (AMD64)</input></li>
	    <li><input type="checkbox" name="filterHWPlatforms" value="ppc"<#if hwPlatforms!="" && hwPlatforms.contains("ppc")> CHECKED</#if>>PPC (Apple)</input></li>
	</ul>
	Programovaci jazyk:
	<#assign pLangs = filters.pLangs?default("")>
	<ul>
	    <li><input type="checkbox" name="filterProgramingLanguages" value="c"<#if pLangs!="" && pLangs.contains("c")> CHECKED</#if>>C</input></li>
	    <li><input type="checkbox" name="filterProgramingLanguages" value="cpp"<#if pLangs!="" && pLangs.contains("cpp")> CHECKED</#if>>C++</input></li>
	    <li><input type="checkbox" name="filterProgramingLanguages" value="java"<#if pLangs!="" && pLangs.contains("java")> CHECKED</#if>>Java</input></li>
	    <li><input type="checkbox" name="filterProgramingLanguages" value="py"<#if pLangs!="" && pLangs.contains("py")> CHECKED</#if>>Python</input></li>
	    <li><input type="checkbox" name="filterProgramingLanguages" value="perl"<#if pLangs!="" && pLangs.contains("bash")> CHECKED</#if>>Perl</input></li>
	    <li><input type="checkbox" name="filterProgramingLanguages" value="bash"<#if pLangs!="" && pLangs.contains("bash")> CHECKED</#if>>Bash</input></li>
	</ul>
	Dalsie moznosti:
	<ul>
	    <li><a href="${RELATION.url?default("/software/show/"+RELATION.id)}?action=filter">Zrusit vsetky filtre</a></li>
	</ul>
	</div>
	<a id="more_filters_link" onclick="more_filters_link.style.display='none'; more_filters.style.display='block';" style="display:none;">Dalsie moznosti filtrovania<br/></a>
	<script language="javascript">
	    var more_filter_div = document.getElementById("more_filters");
	    var more_filter_link = document.getElementById("more_filters_link");
	    
	    <#if filters.size()==0>
		more_filter_link.style.display="inline";
		more_filters.style.display="none";
	    </#if>
	</script>
	<input type="submit" value="Potvrdit">
	<input type="hidden" name="action" value="filter">
	</form>
    </div>
</#assign>

<#include "../header.ftl">

<div class="hw">
<h1>Sekce ${TOOL.xpath(CATEGORY.data,"/data/name")}</h1>

<@lib.showMessages/>

<#if TOOL.xpath(CATEGORY,"data/note")?exists>
 ${TOOL.render(TOOL.element(CATEGORY.data,"data/note"),USER?if_exists)}
</#if>

<#if CATEGORIES?exists>
    <table class="sw-sekce">
      <thead>
        <tr>
          <td colspan="3">Sekce</td>
        </tr>
      </thead>
        <#list CATEGORIES as sekce>
            <#if sekce_index%3==0><tr></#if>
            <td>
                <a href="${URL.make(sekce.url?default("/dir/"+sekce.id))}">
                <#if TOOL.childIcon(sekce)?exists><img src="${TOOL.childIcon(sekce)}" class="ikona" alt="${TOOL.childName(sekce)}"></#if>${TOOL.childName(sekce)}</a>
            </td>
            <#if sekce_index%3==2></tr></#if>
        </#list>
    </table>
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
                <td class="td02">${DATE.show(polozka.child.updated,"CZ_FULL")}</td>
            </tr>
        </#list>
      </tbody>
    </table>
</#if>

</div>

<#include "../footer.ftl">
