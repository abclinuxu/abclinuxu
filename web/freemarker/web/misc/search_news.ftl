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

<h1 class="st_nadpis">Hledání</h1>

<form action="/Search" method="POST">
  <p><input type="text" name="query" value="${QUERY?if_exists?html}" size="50" tabindex="1">
  <input type="submit" value="Hledej" tabindex="2"></p>

  <p><b>Klíèová slova:</b> AND + OR NOT - ( ) "fráze z více slov"</p>
  <#if ERRORS.query?exists><div class="error">${ERRORS.query}</div></#if>

  <table>
   <#list CATEGORIES as category>
    <#if category_index%3==0><tr></#if>
     <td>
      <label><input type="checkbox" name="category" value="${category.key}" <#if category.set>checked</#if>>${category.name}</label>
     </td>
    <#if category_index%3==2></tr></#if>
   </#list>
   <tr><td colspan="3"><button type="button" onclick="toggle(this)">V¹e/nic</button></td></tr>
  </table>
 <input type="hidden" name="parent" value="42932">
 <input type="hidden" name="type" value="zpravicka">

<#if RESULT?exists>

 <h2>Stránkování výsledkù</h2>

 <p>Nalezeno ${RESULT.total} objektù</p>

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
