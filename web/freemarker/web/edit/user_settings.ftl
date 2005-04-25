<#include "../header.ftl">

<@lib.showMessages/>

<h1 class="st_nadpis">Nastavení va¹eho úètu</h1>

<p>Pro va¹i ochranu nejdøíve zadejte va¹e heslo.</p>

<form action="${URL.noPrefix("/EditUser")}" method="POST">
 <table width="100%" border=0 cellpadding=5>
  <tr>
   <td class="required">Heslo</td>
   <td>
    <input type="password" name="PASSWORD" size="16" tabindex="1">
    <div class="error">${ERRORS.PASSWORD?if_exists}</div>
   </td>
  </tr>

  <tr>
   <td class="required">Doba platnosti pøihla¹ovací cookie</td>
   <td>
    <select name="cookieValid" tabindex="2">
     <#assign cookieValid=PARAMS.cookieValid?default("16070400")>
     <option value="0"<#if cookieValid=="0">SELECTED</#if>>nevytváøet</option>
     <option value="-1" <#if cookieValid=="-1">SELECTED</#if>>tato session</option>
     <option value="3600"<#if cookieValid=="3600">SELECTED</#if>>hodina</option>
     <option value="86400"<#if cookieValid=="86400">SELECTED</#if>>den</option>
     <option value="604800"<#if cookieValid=="604800">SELECTED</#if>>týden</option>
     <option value="2678400"<#if cookieValid=="2678400">SELECTED</#if>>mìsíc</option>
     <option value="8035200"<#if cookieValid=="8035200">SELECTED</#if>>ètvrt roku</option>
     <option value="16070400"<#if cookieValid=="16070400">SELECTED</#if>>pùl roku</option>
     <option value="32140800"<#if cookieValid=="32140800">SELECTED</#if>>rok</option>
     <option value="3214080000"<#if cookieValid=="3214080000">SELECTED</#if>>sto let</option>
    </select>
   </td>
  </tr>
  <tr>
   <td colspan="2">
    <p>Toto nastavení ovlivòuje vytváøení cookie pøi pøihlá¹ení. Standardnì se vytvoøí cookie
    s platností pùl roku, která vás doká¾e automaticky pøihlásit bez nutnosti zadávat va¹e heslo.
    Pokud v¹ak poèítaè sdílíte s více lidmi, napøíklad ve ¹kole èi internetové kavárnì, mù¾e být toto chování
    pro vás nepraktické.</p>

    <p>První volba je nevytváøet tuto cookie vùbec, tak¾e pøí¹tì se budete muset pøihlásit ruènì.
    Druhá omezí platnost této cookie jen do vypnutí prohlí¾eèe (session), ostatní omezí její délku
    podle popisu.</p>
   </td>
  </tr>

  <tr>
   <td class="required">Vlastní CSS</td>
   <td>
    <input type="text" name="css" size="40" value="${PARAMS.css?if_exists}" tabindex="3">
    <div class="error">${ERRORS.url?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td colspan="2">Zadejte URL souboru obsahující CSS definici vzhledu portálu. Bude pou¾ita místo
   standardního vzhledu. Nemáte-li rádi experimenty, ponechte prázdné.
   </td>
  </tr>

  <tr>
   <td class="required">Nahrazovat emotikony</td>
   <td>
    <select name="emoticons" tabindex="4">
     <#assign emoticons=PARAMS.emoticons?default("yes")>
     <option value="yes" <#if emoticons=="yes">SELECTED</#if>>ano</option>
     <option value="no"<#if emoticons=="no">SELECTED</#if>>ne</option>
    </select>
   </td>
  </tr>
  <tr>
   <td colspan="2">Urèuje, zda má systém pøi zobrazování textu nahrazovat emotikony
   obrázky. Vypnutím získáte zanedbatelný nárùst rychlosti.
   </td>
  </tr>

  <tr>
   <td class="required">Zobrazovat signatury</td>
   <td>
    <select name="signatures" tabindex="5">
     <#assign emoticons=PARAMS.signatures?default("yes")>
     <option value="yes" <#if emoticons=="yes">SELECTED</#if>>ano</option>
     <option value="no"<#if emoticons=="no">SELECTED</#if>>ne</option>
    </select>
   </td>
  </tr>
  <tr>
   <td colspan="2">Urèuje, zda má systém pøi zobrazování diskusních pøíspìvkù
   zobrazovat signatury autorù pøíspìvkù.
   </td>
  </tr>

  <tr>
   <td class="required">Poèet diskusí na úvodní stránce</td>
   <td>
    <select name="discussions" tabindex="6">
     <#assign discussions=PARAMS.discussions?default("20")>
     <option value="-2"<#if discussions=="-2">SELECTED</#if>>default</option>
     <option value="0" <#if discussions=="0">SELECTED</#if>>¾ádné</option>
     <option value="5"<#if discussions=="5">SELECTED</#if>>5</option>
     <option value="10"<#if discussions=="10">SELECTED</#if>>10</option>
     <option value="15"<#if discussions=="15">SELECTED</#if>>15</option>
     <option value="20"<#if discussions=="20">SELECTED</#if>>20</option>
     <option value="25"<#if discussions=="25">SELECTED</#if>>25</option>
     <option value="30"<#if discussions=="30">SELECTED</#if>>30</option>
     <option value="40"<#if discussions=="40">SELECTED</#if>>40</option>
     <option value="50"<#if discussions=="50">SELECTED</#if>>50</option>
    </select>
   </td>
  </tr>
  <tr>
   <td colspan="2">Zde máte mo¾nost ovlivnit poèet zobrazených diskusí
   na úvodní stránce. Automaticky se zobrazí jen ${DEFAULT_DISCUSSIONS}
   nejèerstvìj¹ích diskusí, na této stránce máte mo¾nost zvolit si vlastní poèet.
   </td>
  </tr>

  <tr>
   <td class="required">Poèet zprávièek</td>
   <td>
    <input type="text" name="news" value="${PARAMS.news?if_exists}" size="3" tabindex="7">
    <div class="error">${ERRORS.news?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td colspan="2">Podobnì mù¾ete také urèit poèet zprávièek, které se zobrazují. Tento poèet
   je standardnì nastaven na ${DEFAULT_NEWS} a mù¾ete jej zde pøedefinovat.
   </td>
  </tr>

  <tr>
   <td class="required">Velikost stránky pøi hledání</td>
   <td>
    <input type="text" name="search" value="${PARAMS.search?if_exists}" size="3" tabindex="8">
    <div class="error">${ERRORS.search?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td colspan="2">Mù¾ete si také zvolit vlastní velikost stránky s nalezenými dokumenty.</td>
  </tr>

  <tr>
   <td class="required">Velikost stránky diskusního fóra</td>
   <td>
    <input type="text" name="forum" value="${PARAMS.forum?if_exists}" size="3" tabindex="9">
    <div class="error">${ERRORS.forum?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td colspan="2">A poèet diskusí na stránce jednotlivých diskusních fór.</td>
  </tr>

  <tr>
   <td width="200">&nbsp;</td>
   <td><input type="submit" value="Dokonèi" tabindex="10"></td>
  </tr>
 </table>
 <input type="hidden" name="action" value="editSettings2">
 <input type="hidden" name="uid" value="${MANAGED.id}">
</form>


<#include "../footer.ftl">
