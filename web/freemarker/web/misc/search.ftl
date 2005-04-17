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

<#if QUESTION_OK?exists>
 <p>Nyn� si pros�m projd�te nalezen� dokumenty, zda neobsahuj�
 odpov�� na va�i ot�zku.</p>
</#if>
<#if QUESTION_OK?exists || QUESTION_KO?exists>
 <#assign formURL=URL.make("/EditDiscussion")>
</#if>

<form action="${formURL?default("/Search")}" method="get">
  <p><input type="text" name="query" value="${QUERY?if_exists?html}" size="50" tabindex="1">
  <input type="submit" value="Hledej" tabindex="2"></p>

  <#if ERRORS.query?exists><div class="error">${ERRORS.query}</div></#if>

  <p><b>Kl��ov� slova:</b> AND + OR NOT - ( ) "fr�ze z v�ce slov"</p>

  <table>
   <tr>
    <td><label><input type="checkbox" name="type" value="clanek" <#if TYPES.article>checked</#if>>�l�nky</label></td>
    <td><label><input type="checkbox" name="type" value="diskuse" <#if TYPES.discussion>checked</#if>>Diskuse</label></td>
    <td><label><input type="checkbox" name="type" value="zpravicka" <#if TYPES.news>checked</#if>>Zpr�vi�ky</label></td>
    <td><label><input type="checkbox" name="type" value="sekce" <#if TYPES.section>checked</#if>>Sekce</label></td>
    <td><button type="button" onclick="toggle(this)">V�e/nic</button></td>
   </tr>
   <tr>
    <td><label><input type="checkbox" name="type" value="hardware" <#if TYPES.hardware>checked</#if>>Hardware</label></td>
    <td><label><input type="checkbox" name="type" value="software" <#if TYPES.software>checked</#if>>Software</label></td>
    <td><label><input type="checkbox" name="type" value="ovladac" <#if TYPES.driver>checked</#if>>Ovlada�e</label></td>
    <td><label><input type="checkbox" name="type" value="dictionary" <#if TYPES.dictionary>checked</#if>>Pojmy</label></td>
    <td><label><input type="checkbox" name="type" value="blog" <#if TYPES.blog>checked</#if>>Blogy</label></td>
   </tr>
  </table>

  <#if QUESTION_OK?exists || QUESTION_KO?exists>
   <input type="hidden" name="rid" value="${PARAMS.rid?if_exists}">
   <input type="hidden" name="action" value="addQuez2">
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

<#if QUESTION_OK?exists>
 <p>Pokud jste pe�liv� pro�li nalezen� dokumenty a p�esto jste nena�li odpov��,
 <a href="${URL.make("/EditDiscussion?action=addQuez3&amp;rid="+PARAMS.rid)}">zde</a>
 m��ete polo�it ot�zku do zvolen�ho diskusn�ho fora.</p>
</#if>

<#include "../footer.ftl">
