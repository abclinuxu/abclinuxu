<#include "../header.ftl">

<h1>Seznam blokovan�ch u�ivatel�</h1>

<p>
M��e se st�t, �e mezi �ten��i se objev� troll, kter�
jen ur�� a provokuje. Pokud jej za�ad�te na tento seznam,
texty jeho koment��� i p��padn� reakce budou schov�ny.
Mo�nost si je p�e��st z�stane zachov�na.
</p>

<@lib.showMessages/>

<#assign blacklist=TOOL.getUsersBlacklist(MANAGED)>
<#if (blacklist?size > 0)>
    <p>Na va�em seznamu jsou tito u�ivatel�:</p>
     <form action="${URL.noPrefix("/EditUser"+MANAGED.id+"?action=fromBlacklist")}" method="POST">
      <#list blacklist as who_>
       <#assign who = TOOL.createUser(who_.id)>
       <div>
        <input type="checkbox" name="bUid" value="${who.id}">
        <a href="/Profile/${who.id}">${who.nick?default(who.name)}</a>
       </div>
      </#list>

      <p>
          <input name="submit" type="submit" value="Odstranit ze seznamu">
      </p>
     </form>
<#else>
    <p>V� seznam je pr�zdn�.</p>
</#if>


<#include "../footer.ftl">
