<#include "../header.ftl">
<@lib.showMessages/>

<h1>Ovlada�e</h1>

<p>V t�to ��sti si m��ete prohl��et nejaktu�ln�j�� verze r�zn�ch ovlada��.
Ovlada�e jsou set��zeny podle �asu vytvo�en� �i posledn� �pravy, nejnov�j��mi
za��naje.</p>

<p>Polo�ka ovlada�e slou�� pro shroma��ov�n� informac� ohledn� ovlada��,
kter� nejsou standardn� sou��st� j�dra. Typicky jde bu� o Open Source
projekty, kdy se nad�enci sna�� vytvo�it podporu pro dan� hardware
(typicky ovlada�e scanner�) nebo v�robce odm�t� uvolnit specifikaci
sv�ch produkt� komunit� a m�sto toho vyr�b� vlastn� ovlada�e (nap��klad
nVidia).
</p>

<p>Pokud chcete p�idat nov� ovlada�, zkuste jej nejd��ve naj�t
v t�to ��sti. Pokud budete �sp�n�, otev�ete jej a zvolte odkaz
<i>Vlo� novou verzi</i>. Pokud zde v� ovlada� nen� uveden,
<a href="${URL.make("/edit?action=add")}">zde
jej m��ete vytvo�it</a>.<p>

<#if USER?exists && USER.hasRole("category admin")>
 <a href="${URL.make("/EditCategory?action=edit&rid="+RELATION.id+"&categoryId="+CATEGORY.id)}">
 <img src="/images/actions/pencil.png" class="ikona22" ALT="Uprav sekci"></a>
</#if>

<ol>
<#list SORT.byDate(CHILDREN,"DESCENDING") as relation>
 <li>
  <a href="${URL.make("/show/"+relation.id)}">
  ${TOOL.xpath(relation.child,"data/name")}, verze ${TOOL.xpath(relation.child,"data/version")}</a>
 </li>
</#list>
</ol>

<#include "../footer.ftl">
