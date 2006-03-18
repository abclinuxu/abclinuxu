<#include "../header.ftl">

<h1>P�ehled m�ch aktivit</h1>

<@lib.showMessages/>

<#if COUNTS?exists>
    <p>Na port�le jsem publikoval(a):</p>

    <ul>
      <li><a href="${URL.noPrefix("/History?type=articles&amp;uid="+PROFILE.id)}">�l�nky</a>
      (${COUNTS.article})</li>
      <li><a href="${URL.noPrefix("/History?type=news&amp;uid="+PROFILE.id)}">zpr�vi�ky</a>
      (${COUNTS.news})</li>
      <li><a href="${URL.noPrefix("/History?type=questions&amp;uid="+PROFILE.id)}">ot�zky ve f�ru</a>
      (${COUNTS.question})</li>
      <li><a href="${URL.noPrefix("/History?type=comments&amp;uid="+PROFILE.id)}">komentovan�ch diskus�</a>
      (${COUNTS.comment})</li>
      <li><a href="${URL.noPrefix("/History?type=dictionary&amp;uid="+PROFILE.id)}">pojmy ve slovn�ku</a>
      (${COUNTS.dictionary})</li>
    </ul>
</#if>

<#include "../footer.ftl">
