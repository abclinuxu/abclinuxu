<#include "../header.ftl">

<@lib.showMessages/>

<p>Skupina je logick� celek, ke kter�mu je mo�no jednotn� p�istupovat
na z�klad� spole�n� charakteristiky jejich �len�. Mohou existovat
nap��klad skupiny admini, kam pat�� lid� maj�c� n�kterou administr�torskou
roli, nebo skupina auto�i, kam pat�� lid� p��c� �l�nky. �lenov� t�chto
skupin pak mohou m�t nap��klad v�t�� pr�va, ne� ostatn� u�ivatel�.
</p>

<p>
<a href="${URL.noPrefix("/Group?action=add")}">Vytvo�en� nov� skupiny</a>
</p>

<#list GROUPS as group>
 <dl>
  <dt>${TOOL.xpath(group.data,"/data/name")}</dt>
  <dd>${TOOL.xpath(group.data,"/data/desc")}</dd>
  <dd>
   <a href="${URL.noPrefix("/Group?action=edit&gid="+group.id)}">�prava</a>
   <a href="${URL.noPrefix("/Group?action=members&gid="+group.id)}">�lenov�</a>
  </dd>
 </dl>
</#list>

<#include "../footer.ftl">
