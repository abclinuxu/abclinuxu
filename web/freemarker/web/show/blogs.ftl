<#assign plovouci_sloupec>
  <div class="s_nadpis">
    <a class="info" href="#">?<span class="tooltip">Vlastní blog si po přihlášení můžete založit v nastavení svého profilu.</span></a>
    <a href="/blog">Seznam všech blogů</a>
  </div>

    <div class="s_sekce">
        <p>Chcete-li také psát svůj blog, přihlašte se a v nastavení
        si jej můžete založit.</p>
        <p>Více o této nové funkci se dozvíte z <a href="/blog/leos/2005/1/2/72133">oznámení</a>.</p>
        <ul>
          <li><a href="/blog">aktuální zápisy</a></li>
	  <li><a href="/blog/souhrn">aktuální zápisy (stručnější souhrn)</a></li>
          <li><a href="/auto/blog.rss">RSS kanál</a></li>
        </ul>
    </div>
</#assign>

<#include "../header.ftl">

<#list BLOGS as info>
    <#assign title=TOOL.xpath(info.blog,"//custom/title")?default("blog")>
    <p><b class="st_nadpis"><a href="/blog/${info.blog.subType}">${title}</a></b> | 
    <a href="/Profile/${info.author.id}">${info.author.nickname?default(info.author.name)}</a></p>
    <p class="cl_inforadek"> &nbsp; Založeno: ${DATE.show(info.blog.created,"CZ_FULL_TXT")} | Příspěvků: ${info.stories}</p>
</#list>

<#include "../footer.ftl">
