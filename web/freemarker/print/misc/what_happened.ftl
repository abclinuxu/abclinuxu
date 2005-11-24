<h1>Èlánky</h1>

<#list ARTICLES?if_exists as clanek>
 <h2><a href="${clanek.url}">${clanek.title}</a></h2>
 <p>${DATE.show(clanek.published, "CZ_FULL", false)} | <a href="/Profile/${clanek.authorId}">${clanek.author}</a><br>
 ${clanek.perex}<br>
 Pøeèteno: ${clanek.reads}x | Komentáøù: ${clanek.comments}
 </p>
</#list>

<h1>Zprávièky</h1>

<#list NEWS?if_exists as news>
 <p>${DATE.show(news.published, "CZ_FULL", false)} | <a href="/Profile/${news.authorId}">${news.author}</a><br>
 ${news.content}<br>
 Komentáøù: ${news.comments} | <a href="${news.url}">Zobrazit</a>
 </p>
 <#if news_has_next><hr></#if>
</#list>

<h1>Otázky v diskusi</h1>

<table>
<tr>
<th>Dotaz</th><th>Reakcí</th><th>Polo¾en</th>
</tr>
<#list QUESTIONS?if_exists as question>
 <tr>
  <td><a href="${question.url?default("/forum/show/"+question.relationId)}">${question.title}</a></td>
  <td>${question.responseCount}</td>
  <td>${DATE.show(question.created, "CZ_FULL", false)}</td>
 </tr>
</#list>
</table>

