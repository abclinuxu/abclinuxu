<#include "../header.ftl">

<h1>Seznam blokovaných uživatelů</h1>

<p>
Může se stát, že mezi čtenáři se objeví troll, který
jen uráží a provokuje. Pokud jej zařadíte na tento seznam,
texty jeho komentářů i případné reakce budou schovány.
Možnost si je přečíst zůstane zachována.
</p>

<@lib.showMessages/>

<#assign blacklist=TOOL.getUsersBlacklist(MANAGED)>
<#if (blacklist?size > 0)>
    <p>Na vašem seznamu jsou tito uživatelé:</p>
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
    <p>Váš seznam je prázdný.</p>
</#if>


<#include "../footer.ftl">
