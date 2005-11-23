<#assign plovouci_sloupec>
  <div class="s_nad_h1"><div class="s_nad_pod_h1">
    <a class="info" href="#">?<span class="tooltip">Vlastn� blog si po p�ihl�en� m��ete zalo�it v nastaven� sv�ho profilu.</span></a>
    <h1><a href="/blog">Seznam v�ech blog�</a></h1>
  </div></div>

    <div class="s_sekce">
        <p>Chcete-li tak� ps�t sv�j blog, p�ihla�te se a v nastaven�
        si jej m��ete zalo�it.</p>
        <p>V�ce o t�to nov� funkci se dozv�te z <a href="/blog/leos/2005/1/2/72133">ozn�men�</a>.</p>
        <ul>
          <li><a href="/blog">aktu�ln� z�pisy</a></li>
	  <li><a href="/blog/souhrn">aktu�ln� z�pisy (stru�n�j�� souhrn)</a></li>
          <li><a href="/auto/blog.rss">RSS kan�l</a></li>
        </ul>
    </div>
</#assign>

<#include "../header.ftl">

<#list BLOGS as info>
    <#assign title=TOOL.xpath(info.blog,"//custom/title")?default("blog")>
    <p><b><a href="/blog/${info.blog.subType}">${title}</a></b> | 
    <a href="/Profile/${info.author.id}">${info.author.nickname?default(info.author.name)}</a></p>
    <p class="cl_inforadek"> &nbsp; Zalo�eno: ${DATE.show(info.blog.created,"CZ_FULL_TXT")} | P��sp�vk�: ${info.stories}</p>
</#list>

<#include "../footer.ftl">
