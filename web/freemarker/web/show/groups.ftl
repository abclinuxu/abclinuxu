<#include "../header.ftl">

<@lib.showSignPost "Akce">
<ul>
    <li><a href="${URL.noPrefix("/Group?action=add")}">Vytvořit skupinu</a></li>
</ul>
</@lib.showSignPost>

<@lib.showMessages/>

<h2>Administrátorské skupiny</h2>

<p>Skupina je logický celek, ke kterému je možno jednotně přistupovat na základě společné charakteristiky jejích členů. Mohou existovat například skupiny admini, kam patří lidé mající některou administrátorskou roli, nebo skupina autoři, kam patří lidé píšící články. Členové těchto skupin pak mohou mít například větší práva než ostatní uživatelé.</p>

<dl>
<#list GROUPS as group>
  <dt>${group.title}</dt>
  <dd><p>${TOOL.xpath(group.data,"/data/desc")}</p>

   <p><a href="${URL.noPrefix("/Group?action=edit&gid="+group.id)}">Úprava</a>,
   <a href="${URL.noPrefix("/Group?action=members&gid="+group.id)}">Členové</a></p>
  </dd>
</#list>
</dl>

<#include "../footer.ftl">
