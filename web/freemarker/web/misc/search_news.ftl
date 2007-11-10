<#include "../header.ftl">

<@lib.showMessages/>

<script language="javascript1.2" type="text/javascript">
    stav = true;
    function toggleCheckBoxes(sender) {
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
   <tr><td colspan="3"><button type="button" onclick="toggleCheckBoxes(this)">Vše/nic</button></td></tr>
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
    </from>

    <#if RESULT.prevPage?exists>
        <a href="${CURRENT_URL}&amp;from=0">0</a>
        <a href="${CURRENT_URL}&amp;from=${RESULT.prevPage.row}">&lt;&lt;</a>
    <#else>
        0 &lt;&lt;
    </#if>

    ${RESULT.thisPage.row} - ${RESULT.thisPage.row + RESULT.thisPage.size}

    <#if RESULT.nextPage?exists>
        <a href="${CURRENT_URL}&amp;from=${RESULT.nextPage.row?string["#"]}">&gt;&gt;</a>
        <a href="${CURRENT_URL}&amp;from=${(RESULT.total-RESULT.pageSize)?string["#"]}">${RESULT.total}</a>
    <#else>
        &gt;&gt; ${RESULT.total}
    </#if>

</#if>

<#include "../footer.ftl">
