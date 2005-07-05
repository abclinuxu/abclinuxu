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

<h1 class="st_nadpis">Hled�n�</h1>

<form action="/Search" method="get">
  <p>
      <input type="text" name="query" value="${QUERY?if_exists?html}" size="50" tabindex="1">
      <input type="submit" value="Hledej" tabindex="2">
  </p>

  <#if ERRORS.query?exists><div class="error">${ERRORS.query}</div></#if>

  <p><b>Kl��ov� slova:</b> AND + OR NOT - ( ) "fr�ze z v�ce slov"</p>

  <#if PARAMS.advancedMode?default("false")=="true">
      <table>
       <tr>
        <td><label><input type="checkbox" name="type" value="clanek" <#if TYPES.article>checked</#if>>�l�nky</label></td>
        <td><label><input type="checkbox" name="type" value="diskuse" <#if TYPES.discussion>checked</#if>>Diskuse</label></td>
        <td><label><input type="checkbox" name="type" value="zpravicka" <#if TYPES.news>checked</#if>>Zpr�vi�ky</label></td>
        <td><label><input type="checkbox" name="type" value="sekce" <#if TYPES.section>checked</#if>>Sekce</label></td>
        <td><label><input type="checkbox" name="type" value="blog" <#if TYPES.blog>checked</#if>>Blogy</label></td>
       </tr>
       <tr>
        <td><label><input type="checkbox" name="type" value="hardware" <#if TYPES.hardware>checked</#if>>Hardware</label></td>
        <td><label><input type="checkbox" name="type" value="software" <#if TYPES.software>checked</#if>>Software</label></td>
        <td><label><input type="checkbox" name="type" value="ovladac" <#if TYPES.driver>checked</#if>>Ovlada�e</label></td>
        <td><label><input type="checkbox" name="type" value="pojem" <#if TYPES.dictionary>checked</#if>>Pojmy</label></td>
        <td><button type="button" onclick="toggle(this)">V�e/nic</button></td>
       </tr>
      </table>
      <input type="hidden" name="advancedMode" value="true">
  <#else>
      <a href="/Search?advancedMode=true&amp;query=${QUERY?url}">Roz���en� hled�n�</a>
  </#if>

  <#if PARAMS.parent?exists><input type="hidden" name="parent" value="${PARAMS.parent}"></#if>

<script src="/data/site/search.js" type="text/javascript"></script>

<#if RESULT?exists>

 <h2>Str�nkov�n� v�sledk�</h2>

 <p>Nalezeno ${RESULT.total} objekt�.
 <a href="/Search?query=${PARAMS.query?html}">Odkaz</a>
 </p>

 <#if RESULT.prevPage?exists>
  <input type="submit" name="from_0" value="0">
  <input type="submit" name="from_${RESULT.prevPage.row}" value="&lt;&lt;">
 <#else>
  <button value="" disabled="disabled">0</button>
  <button value="" disabled="disabled">&lt;&lt;</button>
 </#if>

 <button value="" disabled="disabled">${RESULT.thisPage.row}-${RESULT.thisPage.row+RESULT.thisPage.size}</button>

 <#if RESULT.nextPage?exists>
  <input type="submit" name="from_${RESULT.nextPage.row?string["#"]}" value="&gt;&gt;">
  <input type="submit" name="from_${(RESULT.total-RESULT.pageSize)?string["#"]}" value="${RESULT.total}">
 <#else>
  <button value="" disabled="disabled">&gt;&gt;</button>
  <button value="" disabled="disabled">${RESULT.total}</button>
 </#if>

 <br>

 <table border="0">
  <#list RESULT.data as doc>
   <tr>
    <td width="20px">${RESULT.currentPage.row + doc_index + 1}.</td>
    <td width="60px">${doc.typ}</td>
    <td>
     <!--m--><a href="${doc.url}">${doc.title?default(doc.url)?html}</a><!--n-->
    </td>
    <td align="right">${doc.score}</td>
   </tr>
  </#list>
 </table>

 <#if (RESULT.thisPage.size>20) >
  <br>
  <#if RESULT.prevPage?exists>
   <input type="submit" name="from_0" value="0">
   <input type="submit" name="from_${RESULT.prevPage.row}" value="&lt;&lt;">
  <#else>
   <button value="" disabled="disabled">0</button>
   <button value="" disabled="disabled">&lt;&lt;</button>
  </#if>

  <button value="" disabled="disabled">${RESULT.thisPage.row}-${RESULT.thisPage.row+RESULT.thisPage.size}</button>

  <#if RESULT.nextPage?exists>
   <input type="submit" name="from_${RESULT.nextPage.row?string["#"]}" value="&gt;&gt;">
   <input type="submit" name="from_${(RESULT.total-RESULT.pageSize)?string["#"]}" value="${RESULT.total}">
  <#else>
   <button value="" disabled="disabled">&gt;&gt;</button>
   <button value="" disabled="disabled">${RESULT.total}</button>
  </#if>
 </#if>

</#if>

</form>

<#include "../footer.ftl">
