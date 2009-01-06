<#include "../header.ftl">

<#assign byCreated = FOUND.isQualifierSet("SORT_BY_CREATED")>

<h1>${PAGE_TITLE}</h1>

<table class="ds">
 <thead>
   <tr>
                <td class="td-nazev">Titulek</td>
                <td class="td-meta">Stav</td>
                <td class="td-meta">Reakcí</td>
                <td class="td-datum">Poslední</td>
   </tr>
 </thead>
 <tbody>
  <#list TOOL.analyzeDiscussions(FOUND.data) as diz>
   <tr>
    <td><a href="${diz.url?default("/forum/show/"+diz.relationId)}">${TOOL.limit(diz.title,100," ..")}</a></td>
    <td class="td-meta">
       <@lib.markNewCommentsQuestion diz/>
       <#if TOOL.xpath(diz.discussion,"/data/frozen")??>
         <img src="/images/site2/zamceno.gif" alt="Z" title="Diskuse byla administrátory uzamčena">
       </#if>
       <#if TOOL.isQuestionSolved(diz.discussion.data)>
         <img src="/images/site2/vyreseno.gif" alt="V" title="Diskuse byla podle čtenářů vyřešena">
       </#if>
       <#if USER?? && TOOL.xpath(diz.discussion,"//monitor/id[text()='"+USER.id+"']")??>
         <img src="/images/site2/sledovano.gif" alt="S" title="Tuto diskusi sledujete monitorem">
       </#if>
    </td>
    <td class="td-meta">${diz.responseCount}</td>
    <td class="td-datum">
      <#if byCreated>${DATE.show(diz.created,"CZ_FULL")}<#else>${DATE.show(diz.updated,"CZ_FULL")}</#if>
    </td>
   </tr>
  </#list>
 </tbody>
</table>


<form action="/History">
    <table border="0">
    <tr>
        <th>Pozice</th>
        <th>Počet</th>
        <th>Řadit podle</th>
        <th>Směr</th>
        <td></td>
    </tr>
    <tr>
        <td><input type="text" size="4" value="${FOUND.thisPage.row}" name="from" tabindex="1"></td>
        <td><input type="text" size="3" value="${FOUND.pageSize}" name="count" tabindex="2"></td>
        <td>
            <select name="orderBy" tabindex="3">
                <#if PARAMS.filter??>
                    <option value="date">data prohlédnutí</option>
                    <option value="update">data posledního komentáře</option>
                <#else>
                    <option value="update">data <#if PARAMS.uid??>mého </#if>posledního komentáře</option>
                </#if>
            </select>
        </td>
        <td>
            <select name="orderDir" tabindex="4">
                <option value="desc">sestupně</option>
                <option value="asc">vzestupně</option>
            </select>
        </td>
        <td><input type="submit" value="Zobrazit"></td>
        </tr>
    </table>

    <input type="hidden" name="type" value="${PARAMS.type}">
    <#if PARAMS.uid??><input type="hidden" name="uid" value="${PARAMS.uid}"></#if>
    <#if PARAMS.filter??><input type="hidden" name="filter" value="${PARAMS.filter}"></#if>
</form>

<#if FOUND.prevPage??>
 <a href="${URL_BEFORE_FROM}0${URL_AFTER_FROM}">0</a>
 <a href="${URL_BEFORE_FROM}${FOUND.prevPage.row}${URL_AFTER_FROM}">&lt;&lt;</a>
<#else>0 &lt;&lt;
</#if>
${FOUND.thisPage.row}-${FOUND.thisPage.row+FOUND.thisPage.size}
<#if FOUND.nextPage??>
 <a href="${URL_BEFORE_FROM}${FOUND.nextPage.row?string["#"]}${URL_AFTER_FROM}">&gt;&gt;</a>
 <a href="${URL_BEFORE_FROM}${(FOUND.total - FOUND.pageSize)?string["#"]}${URL_AFTER_FROM}">${FOUND.total}</a>
<#else>&gt;&gt; ${FOUND.total}
</#if>

<#include "../footer.ftl">
