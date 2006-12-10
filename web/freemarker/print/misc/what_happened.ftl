<h2>Èlánky</h2>

<#list ARTICLES?if_exists as clanek>
 <h2><a href="${clanek.url}">${clanek.title}</a></h2>
 <p>
    ${DATE.show(clanek.published, "CZ_FULL", false)} |
    <#list clanek.authors as author>
        <a href="${author.url}">${TOOL.childName(author)}</a><#if author_has_next>, </#if>
    </#list>
    <br>
    ${clanek.perex}<br>
    Pøeèteno: ${clanek.reads}x | Komentáøù: ${clanek.comments}
 </p>
</#list>

<h2>Zprávièky</h2>

<#list NEWS?if_exists as news>
 <p>${DATE.show(news.published, "CZ_FULL", false)} | <a href="/Profile/${news.authorId}">${news.author}</a><br>
 ${news.content}<br>
 Komentáøù: ${news.comments} | <a href="${news.url}">Zobrazit</a>
 </p>
 <#if news_has_next><hr></#if>
</#list>

<h2>Otázky v diskusi</h2>

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

<h2>Nabídky zamìstnání</h2>

<p>
    Nové IT a linuxové nabídky zamìstnání ze serveru <a href="http://wwww.praceabc.cz">www.praceabc.cz</a>.
</p>

<#list JOBS?if_exists as job>
    <p>
        <a href="http://www.praceabc.cz/www/detail.php?id=${job.id}">${job.title}</a><br>
        Region: ${job.region}, Kategorie: ${job.category}<#if job.itJob>, IT</#if><#if job.linuxJob>, Linux</#if>
    </p>
</#list>