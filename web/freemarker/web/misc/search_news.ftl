<#include "../header.ftl">

<@lib.showMessages/>

<script language="javascript1.2" type="text/javascript">
    stav = true;
</script>

<h1 class="st_nadpis">Hled�n�</h1>

<form action="/Search" method="GET">
  <p><input type="text" name="query" value="${QUERY?if_exists?html}" size="50" tabindex="1">
  <input type="submit" value="Hledej" tabindex="2"></p>

  <p><b>Kl��ov� slova:</b> AND + OR NOT - ( ) "fr�ze z v�ce slov"</p>
  <#if ERRORS.query?exists><div class="error">${ERRORS.query}</div></#if>

  <table>
   <#list CATEGORIES as category>
    <#if category_index%3==0><tr></#if>
     <td>
      <label><input type="checkbox" name="category" value="${category.key}" <#if category.set>checked</#if>>${category.name}</label>
     </td>
    <#if category_index%3==2></tr></#if>
   </#list>
   <tr><td colspan="3"><button type="button" onclick="toggle(this, stav)">V�e/nic</button></td></tr>
  </table>
 <input type="hidden" name="parent" value="42932">
 <input type="hidden" name="type" value="zpravicka">

<#if RESULT?exists>

    <p align="right">
        Nalezeno ${RESULT.total} objekt�, zobrazuji ${RESULT.thisPage.row}-${RESULT.thisPage.row+RESULT.thisPage.size}.
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
