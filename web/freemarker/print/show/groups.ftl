<#include "../header.ftl">

<@lib.showMessages/>

<p>Skupina je logický celek, ke kterému je mo¾no jednotnì pøistupovat
na základì spoleèné charakteristiky jejich èlenù. Mohou existovat
napøíklad skupiny admini, kam patøí lidé mající nìkterou administrátorskou
roli, nebo skupina autoøi, kam patøí lidé pí¹ící èlánky. Èlenové tìchto
skupin pak mohou mít napøíklad vìt¹í práva, ne¾ ostatní u¾ivatelé.
</p>

<p>
<a href="${URL.noPrefix("/Group?action=add")}">Vytvoøení nové skupiny</a>
</p>

<#list GROUPS as group>
 <dl>
  <dt>${TOOL.xpath(group.data,"/data/name")}</dt>
  <dd>${TOOL.xpath(group.data,"/data/desc")}</dd>
  <dd>
   <a href="${URL.noPrefix("/Group?action=edit&gid="+group.id)}">Úprava</a>
   <a href="${URL.noPrefix("/Group?action=members&gid="+group.id)}">Èlenové</a>
  </dd>
 </dl>
</#list>

<#include "../footer.ftl">
