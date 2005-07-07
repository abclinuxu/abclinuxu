<#include "../header.ftl">

<@lib.showMessages/>

<script language="javascript1.2" type="text/javascript">
    stav = true;
</script>

<form action="/Search" method="GET">
    <table border="0" width="100%">
        <tr>
            <td>
              <input type="text" name="query" value="${QUERY?if_exists?html}" size="50" tabindex="1">
              <input type="submit" value="Hledej" tabindex="2">
              <#if ERRORS.query?exists><div class="error">${ERRORS.query}</div></#if>
              <#if PARAMS.advancedMode?default("false")=="true">
                  <input type="hidden" name="advancedMode" value="true">
                  <table border="0">
                   <tr>
                    <td><label><input type="checkbox" name="type" value="clanek" <#if TYPES.article>checked</#if>>Èlánky</label></td>
                    <td><label><input type="checkbox" name="type" value="diskuse" <#if TYPES.discussion>checked</#if>>Diskuse</label></td>
                    <td><label><input type="checkbox" name="type" value="zpravicka" <#if TYPES.news>checked</#if>>Zprávièky</label></td>
                    <td><label><input type="checkbox" name="type" value="sekce" <#if TYPES.section>checked</#if>>Sekce</label></td>
                    <td><label><input type="checkbox" name="type" value="blog" <#if TYPES.blog>checked</#if>>Blogy</label></td>
                   </tr>
                   <tr>
                    <td><label><input type="checkbox" name="type" value="hardware" <#if TYPES.hardware>checked</#if>>Hardware</label></td>
                    <td><label><input type="checkbox" name="type" value="software" <#if TYPES.software>checked</#if>>Software</label></td>
                    <td><label><input type="checkbox" name="type" value="ovladac" <#if TYPES.driver>checked</#if>>Ovladaèe</label></td>
                    <td><label><input type="checkbox" name="type" value="pojem" <#if TYPES.dictionary>checked</#if>>Pojmy</label></td>
                    <td><button type="button" onclick="toggle(this, stav)">V¹e/nic</button></td>
                   </tr>
                  </table>
              <#else>
                  <a href="/Search?advancedMode=true<#if PARAMS.query?exists>&amp;query=${PARAMS.query?url</#if>}">Roz¹íøené hledání</a>
              </#if>
            </td>
            <td align="right" valign="middle">
                <a href="http://lucene.apache.org/java/docs/" title="Knihovna pro fulltextové hledání"><img
                src="/images/site2/lucene.gif" width="150" height="23" alt="Lucene" border="0"></a>
                <script src="/data/site/search.js" type="text/javascript"></script>
                <br><a href="/doc/napoveda/hledani">Nápovìda k hledání</a>
            </td>
        </tr>
    </table>
  <#if PARAMS.parent?exists><input type="hidden" name="parent" value="${PARAMS.parent}"></#if>


<#if RESULT?exists>
    <p align="right">
        Nalezeno ${RESULT.total} objektù, zobrazuji ${RESULT.thisPage.row}-${RESULT.thisPage.row+RESULT.thisPage.size}.
    </p>

    <#list RESULT.data as doc>
        <p>
            <!--m--><a href="${doc.url}">${doc.title?default(doc.url)}</a><!--n--> (${doc.typ})
            <#if doc.fragments?exists>
                <br>${doc.fragments}
            </#if>
        </p>
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
