<#assign html_header>
    <script src="/data/site/scriptaculous.js?load=effects,controls" type="text/javascript"></script>
    <script language="javascript1.2" type="text/javascript">
        stav = true;
        function toggle(sender) {
            stav = !stav;
            if (sender.form.elements.length) {
                for (var i = 0; i < sender.form.elements.length; i++) {
                    if (sender.form.elements[i].type == 'checkbox') {
                        sender.form.elements[i].checked = stav;
                    }
                }
            }
        }
    </script>
</#assign>
<#include "../header.ftl">

<@lib.showMessages/>

<form action="/hledani" method="GET">
    <table border="0" class="siroka">
        <tr>
            <td>
              <input type="text" name="dotaz" value="${QUERY?if_exists?html}" id="autocomplete"
                     class="text" size="50" tabindex="1">
              <div id="autocomplete_choices" class="autocomplete"></div>
              <script type="text/javascript">new Ajax.Autocompleter("autocomplete", "autocomplete_choices", "/ajax/suggest", {});</script>

              <input type="submit" value="Hledej" tabindex="2">
              <a href="/SelectUser?sAction=form&amp;url=/Profile">Hledat uživatele</a>
              <#if ERRORS.dotaz?exists><div class="error">${ERRORS.dotaz}</div></#if>
              <#if PARAMS.advancedMode?default("false")=="true">
                  <input type="hidden" name="advancedMode" value="true">
                  <table border="0" width="100%">
                   <tr>
                    <td><label><input type="checkbox" name="typ" value="clanek" <#if TYPES.article>checked</#if>>Články</label></td>
                    <td><label><input type="checkbox" name="typ" value="poradna" <#if TYPES.question>checked</#if>>Poradna</label></td>
                    <td><label><input type="checkbox" name="typ" value="faq" <#if TYPES.faq>checked</#if>>FAQ</label></td>
                    <td><label><input type="checkbox" name="typ" value="pojem" <#if TYPES.dictionary>checked</#if>>Pojmy</label></td>
                   </tr>
                   <tr>
                    <td><label><input type="checkbox" name="typ" value="zpravicka" <#if TYPES.news>checked</#if>>Zprávičky</label></td>
                    <td><label><input type="checkbox" name="typ" value="diskuse" <#if TYPES.discussion>checked</#if>>Diskuse</label></td>
                    <td><label><input type="checkbox" name="typ" value="hardware" <#if TYPES.hardware>checked</#if>>Hardware</label></td>
                    <td><label><input type="checkbox" name="typ" value="ovladac" <#if TYPES.driver>checked</#if>>Ovladače</label></td>
                   </tr>
                   <tr>
                    <td><label><input type="checkbox" name="typ" value="sekce" <#if TYPES.section>checked</#if>>Sekce</label></td>
                    <td><label><input type="checkbox" name="typ" value="software" <#if TYPES.software>checked</#if>>Software</label></td>
                    <td><label><input type="checkbox" name="typ" value="blog" <#if TYPES.blog>checked</#if>>Blogy</label></td>
                    <td><label><input type="checkbox" name="typ" value="anketa" <#if TYPES.poll>checked</#if>>Ankety</label></td>
                   </tr>
                   <tr>
                    <td><label><input type="checkbox" name="typ" value="dokument" <#if TYPES.document>checked</#if>>Dokumenty</label></td>
                    <td><label><input type="checkbox" name="typ" value="bazar" <#if TYPES.bazaar>checked</#if>>Bazar</label></td>
                    <td><label><input type="checkbox" name="typ" value="osobnost" <#if TYPES.personality>checked</#if>>Osobnosti</label></td>
                   </tr>
                   <tr>
                    <td colspan="4" align="left"><label><input type="checkbox" onclick="toggle(this)">Vyber vše/nic</label></td>
                   </tr>
                  </table>
              <#else>
                  <br><a href="${CURRENT_URL}&action=toAdvanced">Rozšířené hledání</a>
              </#if>
            </td>
            <td align="right" valign="middle">
                <a href="http://lucene.apache.org/java/docs/" title="Knihovna pro fulltextové hledání"><img
                src="/images/site2/lucene.png" width="150" height="23" alt="Lucene" border="0"></a>
                <script language="javascript1.2" type="text/javascript">
                    if ((typeof window.sidebar == "object") && (typeof window.sidebar.addPanel == "function")) {
                        moz_src = "http://www.abclinuxu.cz/data/site/abclinuxu.src";
                        moz_ico = "http://www.abclinuxu.cz/images/site/abclinuxu.png";
                        moz_tit = "Portal ABC Linuxu";
                        moz_cat = "Web";
                        document.write("<br><img src=\"/images/site/mozilla.gif\" width=\"16\" height=\"16\"> <a href=\"javascript:window.sidebar.addSearchEngine(moz_src, moz_ico, moz_tit, moz_cat);\" title=\"Přidat vyhledávací modul pro Mozillu\">Mozilla/Firefox</a>&nbsp;(<a href=\"http://www.czilla.cz/sidebars/search.html\" title=\"Více o vyhledávacích modulech pro Mozillu\">?</a>)");
                    }
                </script>
                <br><a href="/doc/napoveda/hledani">Nápověda k hledání</a>
            </td>
        </tr>
    </table>
  <#if PARAMS.parent?exists><input type="hidden" name="parent" value="${PARAMS.parent}"></#if>
</form>

<#if RESULT?exists>
    <p class="search_results">
        Nalezeno ${RESULT.total} objektů (milisekund: ${SEARCH_TIME})<#t><#if (RESULT.total > 0)>,
        zobrazuji ${RESULT.thisPage.row} - ${RESULT.thisPage.row+RESULT.thisPage.size}</#if>.
        Poslední aktualizace ${DATE.show(UPDATED,"CZ_FULL")}.
    </p>

    <#list RESULT.data as doc>
        <div class="search_result">
            <!--m-->
            <a href="${doc.url}" class="search_title">${doc.titulek?default(doc.url)}</a>
            <!--n-->
            <#if doc.highlightedText?exists>
                <p class="search_fragments">${doc.highlightedText}</p>
            </#if>
            <p class="search_details">
            <#if doc.typ='sekce'>
                Sekce
            <#elseif doc.typ='hardware'>
                Hardware,
                poslední změna: ${DATE.show(doc.modified,"SMART_DMY")},
                ${doc.velikost_obsahu} znaků
            <#elseif doc.typ='poradna'>
                Dotaz v poradně,
                <#if (doc.vyreseno=="ano")>vyřešen, </#if>
                počet odpovědí: ${doc.odpovedi},
                položen: ${DATE.show(doc.created,"SMART_DMY")},
                poslední odpověď: ${DATE.show(doc.modified,"SMART_DMY")},
                ${doc.velikost_obsahu} znaků
            <#elseif doc.typ='diskuse'>
                Diskuse,
                počet reakcí: ${doc.odpovedi},
                vytvořena: ${DATE.show(doc.created,"SMART_DMY")},
                poslední reakce: ${DATE.show(doc.modified,"SMART_DMY")},
                ${doc.velikost_obsahu} znaků
            <#elseif doc.typ='software'>
                Software,
                poslední změna: ${DATE.show(doc.modified,"SMART_DMY")},
                ${doc.velikost_obsahu} znaků
            <#elseif doc.typ='ovladac'>
                Ovladač,
                poslední změna: ${DATE.show(doc.modified,"SMART_DMY")},
                ${doc.velikost_obsahu} znaků
            <#elseif doc.typ='faq'>
                FAQ,
                poslední změna: ${DATE.show(doc.modified,"SMART_DMY")},
                ${doc.velikost_obsahu} znaků
            <#elseif doc.typ='clanek'>
                Článek,
                vytvořen: ${DATE.show(doc.created,"SMART_DMY")},
                ${doc.velikost_obsahu} znaků
            <#elseif doc.typ='zpravicka'>
                Zprávička,
                vytvořena: ${DATE.show(doc.created,"SMART_DMY")},
                ${doc.velikost_obsahu} znaků
            <#elseif doc.typ='pojem'>
                Pojem,
                poslední změna: ${DATE.show(doc.modified,"SMART_DMY")},
                ${doc.velikost_obsahu} znaků
            <#elseif doc.typ='blog'>
                Blog,
                vytvořen: ${DATE.show(doc.created,"SMART_DMY")},
                ${doc.velikost_obsahu} znaků
            <#elseif doc.typ='bazar'>
                Inzerát v bazaru,
                vytvořen: ${DATE.show(doc.created,"SMART_DMY")},
                ${doc.velikost_obsahu} znaků
            </#if>
            </p>
        </div>
    </#list>

    <form action="${BASE_URL}">
        <table border="0">
            <tr>
                <th>Pozice</th>
                <th>Počet</th>
                <th>Řadit podle</th>
                <th>Směr</th>
                <td></td>
            </tr>
            <tr>
                <td>
                    <input type="text" size="4" value="${RESULT.thisPage.row}" name="from" tabindex="1">
                </td>
                <td>
                    <input type="text" size="3" value="${RESULT.pageSize}" name="count" tabindex="2">
                </td>
                <td>
                    <select name="orderBy" tabindex="3">
                        <option value="relevance"<#if PARAMS.orderBy?default("relevance")=="relevance"> selected</#if>>relevance</option>
                        <option value="update"<#if PARAMS.orderBy?default("relevance")=="update"> selected</#if>>data poslední změny</option>
                        <option value="create"<#if PARAMS.orderBy?default("relevance")=="create"> selected</#if>>data vytvoření</option>
                    </select>
                </td>
                <td>
                    <select name="orderDir" tabindex="4">
                        <option value="desc"<#if PARAMS.orderDir?default("desc")=="desc"> selected</#if>>sestupně</option>
                        <option value="asc"<#if PARAMS.orderDir?default("desc")=="asc"> selected</#if>>vzestupně</option>
                    </select>
                </td>
                <td>
                    <input type="submit" value="Zobrazit">
                </td>
            </tr>
        </table>
        ${TOOL.saveParams(PARAMS, ["orderDir","orderBy","from","count"])}
    </form>

    <#if RESULT.prevPage?exists>
        <a href="${CURRENT_URL}&from=0">0</a>
        <a href="${CURRENT_URL}&from=${RESULT.prevPage.row}">&lt;&lt;</a>
    <#else>
        0 &lt;&lt;
    </#if>

    ${RESULT.thisPage.row} - ${RESULT.thisPage.row + RESULT.thisPage.size}

    <#if RESULT.nextPage?exists>
        <a href="${CURRENT_URL}&from=${RESULT.nextPage.row?string["#"]}">&gt;&gt;</a>
        <a href="${CURRENT_URL}&from=${(RESULT.total-RESULT.pageSize)?string["#"]}">${RESULT.total}</a>
    <#else>
        &gt;&gt; ${RESULT.total}
    </#if>

</#if>

<#include "../footer.ftl">
