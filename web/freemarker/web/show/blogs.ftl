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

<table>
  <thead>
    <tr>
      <th>Blog</th>
      <th>Autor</th>
      <th>Založeno</th>
      <th>Příspěvků</th>
    </tr>
  </thead>
  <tbody>
  <#list BLOGS.data as info>
    <#assign title=TOOL.xpath(info.blog,"//custom/title")?default("blog")>
      <tr>
        <td><a href="/blog/${info.blog.subType}">${title}</a></td>
        <td><@lib.showUser info.author /></td>
        <td align="right">${DATE.show(info.blog.created,"SMART_DMY_TXT")}</td>
        <td align="center">${info.stories}</td>
      </tr>
   </#list>
  </tbody>
</table>

<p>
<#if (BLOGS.currentPage.row > 0) >
    <#assign start=BLOGS.currentPage.row-BLOGS.pageSize><#if (start<0)><#assign start=0></#if>
    <a href="/blogy?from=${start}&amp;count=${BLOGS.pageSize}">Novější blogy</a> &#8226;
</#if>
<#assign start=BLOGS.currentPage.row + BLOGS.pageSize>
<#if (start < BLOGS.total) >
    <a href="/blogy?from=${start}&amp;count=${BLOGS.pageSize}">Starší blogy</a>
</#if>
</p>

<#include "../footer.ftl">
