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

<#list QUESTIONS.entrySet() as forums>
    <#assign FORUM=TOOL.analyzeDiscussions(forums.value)>
    <#assign rid=forums.key>
    <#if forums.value.size() gt 0>
      <table class="ds" id="forum_table_${forums.key}">
        <thead>
          <tr>
            <td class="td-nazev">
              <#if rid gt 0>
                  <#assign relation=TOOL.createRelation(forums.key)>
                  <span class="st_nadpis"><a href="${relation.url}" title="${TOOL.childName(relation)}">${TOOL.childName(relation)}</a></span>
              <#elseif rid==0>
                  <span class="st_nadpis"><a href="/poradna" title="Poradna">Poradna</a></span>
              <#elseif rid==-1>
                  <span class="st_nadpis"><a href="/skupiny" title="Poradny ze skupin">Poradny ze skupin</a></span>
              </#if>
            </td>
            <td class="td-meta">Reakcí</td>
            <td class="td-datum">
                Poslední
            </td>
          </tr>
        </thead>
        <tbody id="forum_tbody_${forums.key}">
         <#list FORUM as diz>
          <tr>
            <td><a href="${diz.url}">${diz.title}</a></td>
            <td class="td-meta">${diz.responseCount}</td>
            <td class="td-datum">${DATE.show(diz.updated,"SMART")}</td>
          </tr>
         </#list>
        </tbody>
      </table>
      </#if>
</#list>

<h2>Nabídky zaměstnání</h2>

<p>Nové IT a linuxové nabídky zaměstnání ze serveru <a href="http://wwww.abcprace.cz">www.abcprace.cz</a>.</p>

<#list JOBS! as job>
<p><a href="http://www.abcprace.cz/www/detail.php?id=${job.id}">${job.title}</a><br />
   Region: ${job.region}, Kategorie: ${job.category}<#if job.itJob>, IT</#if><#if job.linuxJob>, Linux</#if></p>
</#list>