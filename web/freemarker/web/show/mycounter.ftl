<#include "../header.ftl">

<h1>P�ehled m�ch aktivit</h1>

<@lib.showMessages/>

<#if COUNTS?exists>
    <p>Moje:</p>

    <ul>
      <li><a href="${URL.noPrefix("/History?type=articles&amp;uid="+PROFILE.id)}">�l�nky</a>
      (${COUNTS.article})</li>
      <li><a href="${URL.noPrefix("/History?type=news&amp;uid="+PROFILE.id)}">zpr�vi�ky</a>
      (${COUNTS.news})</li>
      <li><a href="${URL.noPrefix("/History?type=questions&amp;uid="+PROFILE.id)}">ot�zky</a>
      (${COUNTS.question})</li>
      <li><a href="${URL.noPrefix("/History?type=comments&amp;uid="+PROFILE.id)}">koment��e</a>
      (${COUNTS.comment})</li>
    </ul>
</#if>

<p>Tento seznam obsahuje jen ty objekty, kter� nemaj� sd�len� vlastnictv� (wiki).
Proto se zde nezobrazuj� nap��klad z�znamy o <a href="/hardware">hardwaru</a> a <a href="/software">softwaru</a>, <a href="/ovladace">ovlada�e</a>, <a href="/faq">FAQ</a>, <a href="/slovnik">slovn�k</a> �i <a href="/ucebnice">u�ebnice</a>.</p>

<#include "../footer.ftl">
