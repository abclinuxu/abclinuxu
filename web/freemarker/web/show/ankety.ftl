<#include "../header.ftl">

<@lib.showMessages/>

<#assign ankety=SORT.byDate(CHILDREN, "DESCENDING"), from=TOOL.parseInt(PARAMS.get("from")?default("0"))>
<#assign count=15, until=from+count>
<#if (until>=ankety?size)><#assign until=ankety?size-1></#if>

<!--<h1 class="st_nadpis"><a href="/clanky/show/78675">Anketa o nejpopulárnìj¹í distribuci</a></h1>

<p>Pøipojte se k hlasování v na¹í ka¾doroèní <a href="/clanky/show/78675">anketì 
o nejpopulárnìj¹í distribuci</a>. Kromì zvolení distribuce mù¾ete vyplnit i dotazník,
jeho¾ výsledky nám v¹em pomohou udìlat si obrázek o tom, kdo jsou u¾ivatelé GNU/Linuxu, 
proè jim vyhovuje, a jaký hardware k jeho provozování pou¾ívají.</p>-->

<h1 class="st_nadpis">Archiv anket</h1>
<#if from==0 && USER?exists && USER.hasRole("poll admin")>
 <p>
  <a href="${URL.make("/EditPoll?action=add&amp;rid="+RELATION.id)}">
  <img src="/images/actions/attach.png" ALT="Vytvoø anketu" class="ikona22"></a>
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
  <a href="${URL.make("/dir/"+RELATION.id+"?from="+from2)}">Novìj¹í ankety</a>
 </#if>
 <#if ((until+count) < ankety?size)>
  <a href="${URL.make("/dir/"+RELATION.id+"?from="+until)}">Star¹í ankety</a>
 </#if>
</p>

<#include "../footer.ftl">


