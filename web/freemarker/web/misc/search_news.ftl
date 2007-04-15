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

<h1>Hledání</h1>

<form action="/zpravicky/hledani" method="GET">
  <p>
      <input type="text" name="dotaz" value="${QUERY?if_exists?html}" size="50" tabindex="1">
      <input type="submit" value="Hledej" tabindex="2">
  </p>

  <p><b>Klíčová slova:</b> AND + OR NOT - ( ) "fráze z více slov"</p>
  <#if ERRORS.dotaz?exists><div class="error">${ERRORS.dotaz}</div></#if>

  <table>
   <#list CATEGORIES as category>
    <#if category_index%3==0><tr></#if>
     <td>
      <label><input type="checkbox" name="category" value="${category.key}" <#if category.set>checked</#if>>${category.name}</label>
     </td>
    <#if category_index%3==2></tr></#if>
   </#list>
   <tr><td colspan="3"><button type="button" onclick="toggle(this)">Vše/nic</button></td></tr>
  </table>
 <input type="hidden" name="parent" value="42932">
 <input type="hidden" name="type" value="zpravicka">
</form>

<#if RESULT?exists>

    <p align="right">
        Nalezeno ${RESULT.total} objektů, zobrazuji ${RESULT.thisPage.row} - ${RESULT.thisPage.row+RESULT.thisPage.size}.
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
                Zprávička,
                vytvořena: ${DATE.show(doc.created,"SMART_DMY")},
                ${doc.velikost_obsahu} znaků
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

<#include "../footer.ftl">
