<#include "../header.ftl">

<h1>Pøehled mých aktivit</h1>

<@lib.showMessages/>

<#if COUNTS?exists>
    <p>Na portále jsem publikoval:</p>

    <ol>
      <li><a href="${URL.noPrefix("/History?type=articles&amp;uid="+PROFILE.id)}">èlánky</a>
      (${COUNTS.article})</li>
      <li><a href="${URL.noPrefix("/History?type=news&amp;uid="+PROFILE.id)}">zprávièky</a>
      (${COUNTS.news})</li>
      <li><a href="${URL.noPrefix("/History?type=questions&amp;uid="+PROFILE.id)}">otázky ve fóru</a>
      (${COUNTS.question})</li>
      <li><a href="${URL.noPrefix("/History?type=comments&amp;uid="+PROFILE.id)}">komentovaných diskusí</a>
      (${COUNTS.comment})</li>
      <li><a href="${URL.noPrefix("/History?type=hardware&amp;uid="+PROFILE.id)}">hardwarové záznamy</a>
      (${COUNTS.hardware})</li>
      <li><a href="${URL.noPrefix("/History?type=software&amp;uid="+PROFILE.id)}">softwarové záznamy</a>
      (${COUNTS.software})</li>
      <li><a href="${URL.noPrefix("/History?type=dictionary&amp;uid="+PROFILE.id)}">pojmy ve slovníku</a>
      (${COUNTS.dictionary})</li>
    </ol>
</#if>

<#include "../footer.ftl">
