<#include "/include/macros.ftl">
<#include "../header.ftl">

<#call showMessages>

<p>
Chyst�te se smazat objekt ${TOOL.childName(CURRENT)}.
</p>

<p>V�echny relace pro tento objekt v syst�mu:</p>

<ul>
 <#list PARENTS as relation>
  <li>
    <a href="${URL.make("/ViewRelation?rid="+relation.id,PARAMS.prefix)}">${TOOL.childName(relation)}</a>
   </li>
 </#list>
</ul>

<#if PARENTS?size gt 1>
 <p>Jeliko� je tento objekt ulo�en v syst�mu v�cekr�t, pokud
 budete pokra�ovat, zru�� se jen tato jeho vazba.</p>
<#else>
 <p>Pro tento objekt neexistuje ��dn� dal��
 vazba v syst�mu. Pokud budete pokra�ovat, objekt
 bude nen�vratn� ztracen.</p>
</#if>

<form action="${URL.noPrefix("/EditRelation")}" method="POST">
 <input type="submit" VALUE="Dokon�i" tabindex="1">
 <input type="hidden" name="action" value="remove2">
 <input type="hidden" name="prefix" value="${PARAMS.prefix}">
 <input type="hidden" name="rid" value="${PARAMS.rid}">
</form>
