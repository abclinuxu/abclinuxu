<#assign plovouci_sloupec>
    <div class="s_sekce">
        <ul>
            <!-- <li><a href="/clanky/show/3500?text=sekce+${RELATION.id}">Po¾ádat o vytvoøení podsekce</a></li> -->
            <#if CATEGORY.isOpen()>
                <li><a href="${URL.make("/edit/"+RELATION.id+"?action=add")}">Vlo¾it novou polo¾ku</a></li>
            </#if>
            <#if USER?exists && USER.hasRole("category admin")>
                <hr />
                <li><a href="${URL.noPrefix("/EditCategory?action=add&amp;rid="+RELATION.id+"&amp;categoryId="+CATEGORY.id)}">mkdir</a>,
                    <a href="${URL.noPrefix("/EditCategory?action=edit&amp;rid="+RELATION.id+"&amp;categoryId="+CATEGORY.id)}">edit</a>,
                    <a href="${URL.noPrefix("/EditRelation/"+RELATION.id+"?action=remove&amp;prefix="+URL.prefix)}">rmdir</a>,
                    <a href="${URL.noPrefix("/SelectRelation?rid="+RELATION.id+"&amp;url=/EditRelation&amp;action=add&amp;prefix="+URL.prefix)}">link</a></li>
            </#if>
            <#if USER?exists && USER.hasRole("move relation")>
                <li><a href="${URL.noPrefix("/SelectRelation?rid="+RELATION.id+"&amp;prefix="+URL.prefix+"&amp;url=/EditRelation&amp;action=move")}">Pøesunout sekci</a></li>
                <li><a href="${URL.noPrefix("/EditRelation/"+RELATION.id+"?action=moveAll&amp;prefix="+URL.prefix)}">Pøesunout obsah</a></li>
            </#if>
            <#if USER?exists && USER.hasRole("root")>
                <li><a href="${URL.noPrefix("/EditRelation/"+RELATION.id+"?action=showACL")}">ACL</a></li>
            </#if>
        </ul>
    </div>
    
    <div class="s_nad_h1"><div class="s_nad_pod_h1">
        <h1>Filtr</h1>
    </div></div>

    <div class="s_sekce">
	<form action="${RELATION.url?default("/software/show/"+RELATION.id)}" method="GET">

	<b>U¾ivatelské rozhraní:</b>

	<#assign uiType = filters.uiType?default("")>
	<div class="sw-strom">
           <div class="uroven-1">
              <label><input type="checkbox" name="filterUIType" value="xwindows"<#if uiType!="" && uiType.contains("xwindows")> CHECKED</#if>>X Window System</input></label>
	      <div>
                 <label><input type="checkbox" name="filterUIType" value="qt"<#if uiType!="" && uiType.contains("qt")> CHECKED</#if>>Qt</input></label>
                 <div>
                    <label><input type="checkbox" name="filterUIType" value="kde"<#if uiType!="" && uiType.contains("kde")> CHECKED</#if>>KDE</input></label>
                 </div>
              </div>
	      <div>
                 <label><input type="checkbox" name="filterUIType" value="gtk"<#if uiType!="" && uiType.contains("gtk")> CHECKED</#if>>GTK+</input></label>
                 <div>
                    <label><input type="checkbox" name="filterUIType" value="gnome"<#if uiType!="" && uiType.contains("gnome")> CHECKED</#if>>Gnome</input></label>
                 </div>
              </div>
           </div>
           <div class="uroven-1">
              <label><input type="checkbox" name="filterUIType" value="console"<#if uiType!="" && uiType.contains("console")> CHECKED</#if>>Konzole</input></label>
              <div>
                 <label><input type="checkbox" name="filterUIType" value="cli"<#if uiType!="" && uiType.contains("cli")> CHECKED</#if>>Pøíkazové rozhr. (CLI)</input></label>
              </div>
              <div>
                 <label><input type="checkbox" name="filterUIType" value="tui"<#if uiType!="" && uiType.contains("tui")> CHECKED</#if>>Textové rozhr. (TUI)</input></label>
              </div>
              <div>
                 <label><input type="checkbox" name="filterUIType" value="grconsole"<#if uiType!="" && uiType.contains("grconsole")> CHECKED</#if>>Grafické rozhr.</input></label>
              </div>
	   </div>
        </div>

      <div id="more_filters">

	<b>Licence:</b>

	<#assign licenses = filters.licenses?default("")>
	<div class="sw-strom">
           <label><input type="checkbox" name="filterLicenses" value="gpl"<#if licenses!="" && licenses.contains("gpl")> CHECKED</#if>>GPL</input></label><br />
           <label><input type="checkbox" name="filterLicenses" value="lgpl"<#if licenses!="" && licenses.contains("lgpl")> CHECKED</#if>>LGPL</input></label><br />
           <label><input type="checkbox" name="filterLicenses" value="bsd"<#if licenses!="" && licenses.contains("bsd")> CHECKED</#if>>BSD</input></label><br />
           <label><input type="checkbox" name="filterLicenses" value="mpl"<#if licenses!="" && licenses.contains("mpl")> CHECKED</#if>>MPL</input></label><br />
           <label><input type="checkbox" name="filterLicenses" value="apl"<#if licenses!="" && licenses.contains("apl")> CHECKED</#if>>APL</input></label><br />
           <label><input type="checkbox" name="filterLicenses" value="oss"<#if licenses!="" && licenses.contains("oss")> CHECKED</#if>>OSS compliant</input></label><br />
           <label><input type="checkbox" name="filterLicenses" value="freeware"<#if licenses!="" && licenses.contains("freeware")> CHECKED</#if>>freeware</input></label><br />
           <label><input type="checkbox" name="filterLicenses" value="commercial"<#if licenses!="" && licenses.contains("commercial")> CHECKED</#if>>komerèní</input></label><br />
           <label><input type="checkbox" name="filterLicenses" value="other"<#if licenses!="" && licenses.contains("other")> CHECKED</#if>>jiná</input></label>
	</div>

	<a href="${RELATION.url?default("/software/show/"+RELATION.id)}?action=filter">Zru¹it filtry</a>
	
      </div> <!-- more_filters -->

	<a id="more_filters_link" onclick="more_filters_link.style.display='none'; more_filters.style.display='block';" style="display:none;">Dalsie moznosti filtrovania<br /></a>

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

<div class="sw">

<h1>${TOOL.xpath(CATEGORY.data,"/data/name")}</h1>

<@lib.showMessages/>

<#if TOOL.xpath(CATEGORY,"data/note")?exists>
 ${TOOL.render(TOOL.element(CATEGORY.data,"data/note"),USER?if_exists)}
</#if>

<#if CATEGORIES?exists>
    <!--<table class="sw-sekce">
      <thead>
        <tr>
          <td colspan="3">Sekce</td>
        </tr>
      </thead>
      <tbody>
        <#list CATEGORIES as sekce>
            <#if sekce_index%3==0><tr></#if>
            <td>
                <a href="${URL.make(sekce.url?default("/dir/"+sekce.id))}"><#if TOOL.childIcon(sekce)?exists><img src="${TOOL.childIcon(sekce)}" class="ikona" alt="${TOOL.childName(sekce)}"></#if> ${TOOL.childName(sekce)}</a>
            </td>
            <#if sekce_index%3==2></tr></#if>
        </#list>
      </tbody>
    </table>-->

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
