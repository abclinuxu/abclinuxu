<#include "../header.ftl">

<h1>Přehled mých aktivit</h1>

<@lib.showMessages/>

<#if COUNTS?exists>
    <p>Moje:</p>

    <ul>
      <li><a href="${URL.noPrefix("/History?type=news&amp;uid="+PROFILE.id)}">zprávičky</a>
      (${COUNTS.news})</li>
      <li><a href="${URL.noPrefix("/History?type=questions&amp;uid="+PROFILE.id)}">otázky</a>
      (${COUNTS.question})</li>
      <li><a href="${URL.noPrefix("/History?type=comments&amp;uid="+PROFILE.id)}">komentáře</a>
      (${COUNTS.comment})</li>
    </ul>
</#if>

<p>
    Tento seznam obsahuje jen ty objekty, které nemají sdílené vlastnictví (wiki). Proto se zde nezobrazují
    například záznamy o <a href="/hardware">hardwaru</a> a <a href="/software">softwaru</a>,
    <a href="/ovladace">ovladače</a>, <a href="/faq">FAQ</a>, <a href="/slovnik">slovníku</a> či
    <a href="/ucebnice">učebnici</a>.
</p>

<#include "../footer.ftl">
