<h2>Články</h2>

<#list ARTICLES! as clanek>
 <b class="st_nadpis"><a href="${clanek.url}">${clanek.title}</a></b>
 <p>${clanek.perex}</p>
 <p class="meta-vypis">
    ${DATE.show(clanek.published, "CZ_FULL", false)} |
    <#list clanek.authors as author>
        <a href="${author.url}">${TOOL.childName(author)}</a><#if author_has_next>, </#if>
    </#list> | Přečteno: ${clanek.reads}x | Komentářů: ${clanek.comments}
 </p>
</#list>

<h2>Zprávičky</h2>

<#list NEWS! as news>
 <b class="st_nadpis"><a href="${news.url}">${news.title}</a></b>
 <p>${news.content}</p>
 <p class="meta-vypis">
   ${DATE.show(news.published, "CZ_FULL", false)} | <a href="/Profile/${news.authorId}">${news.author}</a> |
   <a href="${news.url}">Komentářů: ${news.comments}</a>
 </p>
 <#if news_has_next><hr /></#if>
</#list>

<h2>Otázky v diskusi</h2>

<table class="ds">
<thead>
  <tr>
    <td class="td-nazev">Dotaz</td>
    <td class="td-meta">Reakcí</td>
    <td class="td-datum">Položen</td>
  </tr>
</thead>
<tbody>
<#list QUESTIONS! as question>
  <tr>
    <td><a href="${question.url?default("/forum/show/"+question.relationId)}">${question.title}</a></td>
    <td class="td-meta">${question.responseCount}</td>
    <td class="td-datum">${DATE.show(question.created, "CZ_FULL", false)}</td>
  </tr>
</#list>
</tbody>
</table>

<h2>Nabídky zaměstnání</h2>

<p>Nové IT a linuxové nabídky zaměstnání ze serveru <a href="http://wwww.abcprace.cz">www.abcprace.cz</a>.</p>

<#list JOBS! as job>
<p><a href="http://www.abcprace.cz/www/detail.php?id=${job.id}">${job.title}</a><br />
   Region: ${job.region}, Kategorie: ${job.category}<#if job.itJob>, IT</#if><#if job.linuxJob>, Linux</#if></p>
</#list>