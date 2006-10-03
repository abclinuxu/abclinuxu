<#include "../header.ftl">

<@lib.showMessages/>

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

<form action="/Search" method="GET">
    <table border="0" class="siroka">
        <tr>
            <td>
              <input type="text" name="query" value="${QUERY?if_exists?html}" size="50" tabindex="1">
              <input type="submit" value="Hledej" tabindex="2">
              <#if ERRORS.query?exists><div class="error">${ERRORS.query}</div></#if>
              <#if PARAMS.advancedMode?default("false")=="true">
                  <input type="hidden" name="advancedMode" value="true">
                  <table border="0" width="100%">
                   <tr>
                    <td><label><input type="checkbox" name="type" value="clanek" <#if TYPES.article>checked</#if>>�l�nky</label></td>
                    <td><label><input type="checkbox" name="type" value="otazka" <#if TYPES.question>checked</#if>>Diskuzn� f�rum</label></td>    <td><label><input type="checkbox" name="type" value="faq" <#if TYPES.faq>checked</#if>>FAQ</label></td>
                    <td><label><input type="checkbox" name="type" value="pojem" <#if TYPES.dictionary>checked</#if>>Pojmy</label></td>
                   </tr>
                   <tr>
                    <td><label><input type="checkbox" name="type" value="zpravicka" <#if TYPES.news>checked</#if>>Zpr�vi�ky</label></td>
                    <td><label><input type="checkbox" name="type" value="diskuse" <#if TYPES.discussion>checked</#if>>Diskuze u obsahu</label></td>
                    <td><label><input type="checkbox" name="type" value="hardware" <#if TYPES.hardware>checked</#if>>Hardware</label></td>
                    <td><label><input type="checkbox" name="type" value="ovladac" <#if TYPES.driver>checked</#if>>Ovlada�e</label></td>
                   </tr>
                   <tr>
                    <td><label><input type="checkbox" name="type" value="sekce" <#if TYPES.section>checked</#if>>Sekce</label></td>
                    <td><label><input type="checkbox" name="type" value="software" <#if TYPES.software>checked</#if>>Software</label></td>
                    <td><label><input type="checkbox" name="type" value="blog" <#if TYPES.blog>checked</#if>>Blogy</label></td>
                    <td><label><input type="checkbox" name="type" value="anketa" <#if TYPES.poll>checked</#if>>Ankety</label></td>
                   </tr>
                   <tr>
                    <td colspan="4" align="left"><label><input type="checkbox" onclick="toggle(this)">Vyber v�e/nic</label></td>
                   </tr>
                  </table>
              <#else>
                  <br><a href="/Search?advancedMode=true<#if PARAMS.query?exists>&amp;query=${PARAMS.query?url}</#if>">Roz���en� hled�n�</a>
              </#if>
            </td>
            <td align="right" valign="middle">
                <a href="http://lucene.apache.org/java/docs/" title="Knihovna pro fulltextov� hled�n�"><img
                src="/images/site2/lucene.png" width="150" height="23" alt="Lucene" border="0"></a>
                <script language="javascript1.2" type="text/javascript">
                    if ((typeof window.sidebar == "object") && (typeof window.sidebar.addPanel == "function")) {
                        moz_src = "http://www.abclinuxu.cz/data/site/abclinuxu.src";
                        moz_ico = "http://www.abclinuxu.cz/images/site/abclinuxu.png";
                        moz_tit = "Portal ABC Linuxu";
                        moz_cat = "Web";
                        document.write("<br><img src=\"/images/site/mozilla.gif\" width=\"16\" height=\"16\"> <a href=\"javascript:window.sidebar.addSearchEngine(moz_src, moz_ico, moz_tit, moz_cat);\" title=\"P�idat vyhled�vac� modul pro Mozillu\">Mozilla/Firefox</a>&nbsp;(<a href=\"http://www.czilla.cz/sidebars/search.html\" title=\"V�ce o vyhled�vac�ch modulech pro Mozillu\">?</a>)");
                    }
                </script>
                <br><a href="/doc/napoveda/hledani">N�pov�da k hled�n�</a>
            </td>
        </tr>
    </table>
  <#if PARAMS.parent?exists><input type="hidden" name="parent" value="${PARAMS.parent}"></#if>


<#if RESULT?exists>
    <p class="search_results">
        Nalezeno ${RESULT.total} objekt� (milisekund: ${SEARCH_TIME})<#t><#if (RESULT.total > 0)>,
        zobrazuji ${RESULT.thisPage.row} - ${RESULT.thisPage.row+RESULT.thisPage.size}</#if>.
        Posledn� aktualizace ${DATE.show(UPDATED,"CZ_FULL")}.
    </p>

    <#list RESULT.data as doc>
        <div class="search_result">
            <!--m-->
            <a href="${doc.url}" class="search_title">${doc.title?default(doc.url)}</a>
            <#if (doc.typ='otazka' && doc.vyreseno=="ano")> <span class="search_solved">(vy�e�eno)</span></#if>
            <!--n-->
            <#if doc.fragments?exists>
                <p class="search_fragments">${doc.fragments}</p>
            </#if>
            <p class="search_details">
            <#if doc.typ='sekce'>
                Sekce
            <#elseif doc.typ='hardware'>
                Hardware,
                posledn� zm�na: ${DATE.show(doc.datum_zmeny,"CZ_DMY")},
                ${doc.velikost_obsahu} znak�
            <#elseif doc.typ='diskuse' ||  doc.typ='otazka'>
                Diskuse,
                po�et reakc�: ${doc.odpovedi},
                vytvo�ena: ${DATE.show(doc.datum_vytvoreni,"CZ_DMY")},
                posledn� reakce: ${DATE.show(doc.datum_zmeny,"CZ_DMY")},
                ${doc.velikost_obsahu} znak�
            <#elseif doc.typ='software'>
                Software,
                posledn� zm�na: ${DATE.show(doc.datum_zmeny,"CZ_DMY")},
                ${doc.velikost_obsahu} znak�
            <#elseif doc.typ='ovladac'>
                Ovlada�,
                posledn� zm�na: ${DATE.show(doc.datum_zmeny,"CZ_DMY")},
                ${doc.velikost_obsahu} znak�
            <#elseif doc.typ='faq'>
                FAQ,
                posledn� zm�na: ${DATE.show(doc.datum_zmeny,"CZ_DMY")},
                ${doc.velikost_obsahu} znak�
            <#elseif doc.typ='clanek'>
                �l�nek,
                vytvo�en: ${DATE.show(doc.datum_vytvoreni,"CZ_DMY")},
                ${doc.velikost_obsahu} znak�
            <#elseif doc.typ='zpravicka'>
                Zpr�vi�ka,
                vytvo�ena: ${DATE.show(doc.datum_vytvoreni,"CZ_DMY")},
                ${doc.velikost_obsahu} znak�
            <#elseif doc.typ='pojem'>
                Pojem,
                posledn� zm�na: ${DATE.show(doc.datum_zmeny,"CZ_DMY")},
                ${doc.velikost_obsahu} znak�
            <#elseif doc.typ='blog'>
                Blog,
                vytvo�en: ${DATE.show(doc.datum_vytvoreni,"CZ_DMY")},
                ${doc.velikost_obsahu} znak�
            </#if>
            </p>
        </div>
    </#list>

    <#if RESULT.prevPage?exists>
        <input type="submit" name="from_0" value="0">
        <input type="submit" name="from_${RESULT.prevPage.row}" value="&lt;&lt;">
    <#else>
        <button value="" disabled="disabled">0</button>
        <button value="" disabled="disabled">&lt;&lt;</button>
    </#if>

    <#if RESULT.nextPage?exists>
        <input type="submit" name="from_${RESULT.nextPage.row?string["#"]}" value="&gt;&gt;">
        <input type="submit" name="from_${(RESULT.total-RESULT.pageSize)?string["#"]}" value="${RESULT.total}">
    <#else>
        <button value="" disabled="disabled">&gt;&gt;</button>
        <button value="" disabled="disabled">${RESULT.total}</button>
    </#if>

</#if>

</form>

<#include "../footer.ftl">
