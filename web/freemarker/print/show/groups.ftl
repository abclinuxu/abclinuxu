<#include "../header.ftl">

<@lib.showMessages/>

<p>Skupina je logický celek, ke kterému je možno jednotně přistupovat
na základě společné charakteristiky jejich členů. Mohou existovat
například skupiny admini, kam patří lidé mající některou administrátorskou
roli, nebo skupina autoři, kam patří lidé píšící články. Členové těchto
skupin pak mohou mít například větší práva, než ostatní uživatelé.
</p>

<p>
<a href="${URL.noPrefix("/Group?action=add")}">Vytvoření nové skupiny</a>
</p>

<#list GROUPS as group>
 <dl>
  <dt>${TOOL.xpath(group.data,"/data/name")}</dt>
  <dd>${TOOL.xpath(group.data,"/data/desc")}</dd>
  <dd>
   <a href="${URL.noPrefix("/Group?action=edit&gid="+group.id)}">Úprava</a>
   <a href="${URL.noPrefix("/Group?action=members&gid="+group.id)}">Členové</a>
  </dd>
 </dl>
</#list>

<#include "../footer.ftl">
