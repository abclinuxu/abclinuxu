<#assign plovouci_sloupec>
  <div class="s_nad_h1"><div class="s_nad_pod_h1">
    <a class="info" href="#">?<span class="tooltip">Vlastní blog si po pøihlá¹ení mù¾ete zalo¾it v nastavení svého profilu.</span></a>
    <h1><a href="/blog">Seznam v¹ech blogù</a></h1>
  </div></div>

    <div class="s_sekce">
        <p>Chcete-li také psát svùj blog, pøihla¹te se a v nastavení
        si jej mù¾ete zalo¾it.</p>
        <p>Více o této nové funkci se dozvíte z <a href="/blog/leos/2005/1/2/72133">oznámení</a>.</p>
        <ul>
          <li><a href="/blog">aktuální zápisy</a></li>
	  <li><a href="/blog/souhrn">aktuální zápisy (struènìj¹í souhrn)</a></li>
          <li><a href="/auto/blog.rss">RSS kanál</a></li>
        </ul>
    </div>
</#assign>

<#include "../header.ftl">

<#list BLOGS as info>
    <#assign title=TOOL.xpath(info.blog,"//custom/title")?default("blog")>
    <p><b><a href="/blog/${info.blog.subType}">${title}</a></b> | 
    <a href="/Profile/${info.author.id}">${info.author.nickname?default(info.author.name)}</a></p>
    <p class="cl_inforadek"> &nbsp; Zalo¾eno: ${DATE.show(info.blog.created,"CZ_FULL_TXT")} | Pøíspìvkù: ${info.stories}</p>
</#list>

<#include "../footer.ftl">
