<#include "../header.ftl">

<@lib.showMessages/>

<#assign ankety=SORT.byDate(CHILDREN, "DESCENDING"), from=TOOL.parseInt(PARAMS.get("from")?default("0"))>
<#assign count=15, until=from+count>
<#if (until>=ankety?size)><#assign until=ankety?size-1></#if>

<!--<h1 class="st_nadpis"><a href="/clanky/show/78675">Anketa o nejpopul�rn�j�� distribuci</a></h1>

<p>P�ipojte se k hlasov�n� v na�� ka�doro�n� <a href="/clanky/show/78675">anket� 
o nejpopul�rn�j�� distribuci</a>. Krom� zvolen� distribuce m��ete vyplnit i dotazn�k,
jeho� v�sledky n�m v�em pomohou ud�lat si obr�zek o tom, kdo jsou u�ivatel� GNU/Linuxu, 
pro� jim vyhovuje, a jak� hardware k jeho provozov�n� pou��vaj�.</p>-->

<h1 class="st_nadpis">Archiv anket</h1>
<#if from==0 && USER?exists && USER.hasRole("poll admin")>
 <p>
  <a href="${URL.make("/EditPoll?action=add&amp;rid="+RELATION.id)}">
  <img src="/images/actions/attach.png" ALT="Vytvo� anketu" class="ikona22"></a>
 </p>
</#if>

<table>
<#list ankety[from..(until-1)] as relation>
<tbody>
 <tr>
  <td align="right" width="120px"><a href="${URL.make("/show/"+relation.id)}">
    ${DATE.show(relation.child.created, "CZ_DMY")}</a></td>
  <td>${relation.child.text}</td>
 </tr>
</tbody>
</#list>
</table>

<p>
 <#if (from>0)>
  <#assign from2=from-count><#if (from2 < 0)><#assign from2=0></#if>
  <a href="${URL.make("/dir/"+RELATION.id+"?from="+from2)}">Nov�j�� ankety</a>
 </#if>
 <#if ((until+count) < ankety?size)>
  <a href="${URL.make("/dir/"+RELATION.id+"?from="+until)}">Star�� ankety</a>
 </#if>
</p>

<#include "../footer.ftl">


