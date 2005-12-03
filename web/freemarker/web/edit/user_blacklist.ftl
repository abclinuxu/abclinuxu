<#include "../header.ftl">

<h1>Seznam blokovaných u¾ivatelù</h1>

<p>
Mù¾e se stát, ¾e mezi ètenáøi se objeví troll, který
jen urá¾í a provokuje. Pokud jej zaøadíte na tento seznam,
texty jeho komentáøù i pøípadné reakce budou schovány.
Mo¾nost si je pøeèíst zùstane zachována.
</p>

<@lib.showMessages/>

<#assign blacklist=TOOL.getUsersBlacklist(MANAGED)>
<#if (blacklist?size > 0)>
    <p>Na va¹em seznamu jsou tito u¾ivatelé:</p>
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
    <p>Vá¹ seznam je prázdný.</p>
</#if>


<#include "../footer.ftl">
