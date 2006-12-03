<#include "../header.ftl">

<h1>Pøehled mých aktivit</h1>

<@lib.showMessages/>

<#if COUNTS?exists>
    <p>Moje:</p>

    <ul>
      <li><a href="${URL.noPrefix("/History?type=news&amp;uid="+PROFILE.id)}">zprávièky</a>
      (${COUNTS.news})</li>
      <li><a href="${URL.noPrefix("/History?type=questions&amp;uid="+PROFILE.id)}">otázky</a>
      (${COUNTS.question})</li>
      <li><a href="${URL.noPrefix("/History?type=comments&amp;uid="+PROFILE.id)}">komentáøe</a>
      (${COUNTS.comment})</li>
    </ul>
</#if>

<p>
    Tento seznam obsahuje jen ty objekty, které nemají sdílené vlastnictví (wiki). Proto se zde nezobrazují
    napøíklad záznamy o <a href="/hardware">hardwaru</a> a <a href="/software">softwaru</a>,
    <a href="/ovladace">ovladaèe</a>, <a href="/faq">FAQ</a>, <a href="/slovnik">slovníku</a> èi
    <a href="/ucebnice">uèebnici</a>.
</p>

<#include "../footer.ftl">
