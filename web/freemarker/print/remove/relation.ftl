<#include "/include/macros.ftl">
<#include "../header.ftl">

<#call showMessages>

<p>
Chystáte se smazat objekt ${TOOL.childName(CURRENT)}.
</p>

<p>V¹echny relace pro tento objekt v systému:</p>

<ul>
 <#list PARENTS as relation>
  <li>
    <a href="${URL.make("/ViewRelation?rid="+relation.id,PARAMS.prefix)}">${TOOL.childName(relation)}</a>
   </li>
 </#list>
</ul>

<#if PARENTS?size gt 1>
 <p>Jeliko¾ je tento objekt ulo¾en v systému vícekrát, pokud
 budete pokraèovat, zru¹í se jen tato jeho vazba.</p>
<#else>
 <p>Pro tento objekt neexistuje ¾ádná dal¹í
 vazba v systému. Pokud budete pokraèovat, objekt
 bude nenávratnì ztracen.</p>
</#if>

<form action="${URL.noPrefix("/EditRelation")}" method="POST">
 <input type="submit" VALUE="Dokonèi" tabindex="1">
 <input type="hidden" name="action" value="remove2">
 <input type="hidden" name="prefix" value="${PARAMS.prefix}">
 <input type="hidden" name="rid" value="${PARAMS.rid}">
</form>
