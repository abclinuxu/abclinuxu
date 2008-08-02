<#include "../header.ftl">

<@lib.showMessages/>

<h1>Nastavení vašeho účtu</h1>

<p>Pro vaši ochranu nejdříve zadejte vaše heslo.</p>

<form action="${URL.noPrefix("/EditUser")}" method="POST">
 <table class="siroka">
  <tr>
   <td class="required">Heslo</td>
   <td>
    <input type="password" name="PASSWORD" size="16" tabindex="1">
    <div class="error">${ERRORS.PASSWORD?if_exists}</div>
   </td>
  </tr>

  <tr>
   <td class="required">Doba platnosti přihlašovací cookie</td>
   <td>
    <select name="cookieValid" tabindex="2">
     <#assign cookieValid=PARAMS.cookieValid?default("16070400")>
     <option value="0"<#if cookieValid=="0">SELECTED</#if>>nevytvářet</option>
     <option value="-1" <#if cookieValid=="-1">SELECTED</#if>>tato session</option>
     <option value="3600"<#if cookieValid=="3600">SELECTED</#if>>hodina</option>
     <option value="86400"<#if cookieValid=="86400">SELECTED</#if>>den</option>
     <option value="604800"<#if cookieValid=="604800">SELECTED</#if>>týden</option>
     <option value="2678400"<#if cookieValid=="2678400">SELECTED</#if>>měsíc</option>
     <option value="8035200"<#if cookieValid=="8035200">SELECTED</#if>>čtvrt roku</option>
     <option value="16070400"<#if cookieValid=="16070400">SELECTED</#if>>půl roku</option>
     <option value="32140800"<#if cookieValid=="32140800">SELECTED</#if>>rok</option>
     <option value="3214080000"<#if cookieValid=="3214080000">SELECTED</#if>>sto let</option>
    </select>
   </td>
  </tr>
  <tr>
   <td colspan="2">
    <p>Toto nastavení ovlivňuje vytváření cookie při přihlášení. Standardně se vytvoří cookie
    s platností půl roku, která vás dokáže automaticky přihlásit bez nutnosti zadávat vaše heslo.
    Pokud však počítač sdílíte s více lidmi, například ve škole či internetové kavárně, může být toto chování
    pro vás nepraktické.</p>

    <p>První volba je nevytvářet tuto cookie vůbec, takže příště se budete muset přihlásit ručně.
    Druhá omezí platnost této cookie jen do vypnutí prohlížeče (session), ostatní omezí její délku
    podle popisu.</p>
   </td>
  </tr>

  <tr>
   <td class="required">Vlastní CSS</td>
   <td>
    <input type="text" name="css" size="40" value="${PARAMS.css?if_exists}" tabindex="3">
    <div class="error">${ERRORS.css?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td colspan="2">Zadejte URL souboru obsahující CSS definici vzhledu portálu. Bude použita místo
   standardního vzhledu. Pro bílé písmo na černém podkladu vložte
   <code>/styles-dark.css</code>. <a href="/doc/napoveda/alternativni-design">Nápověda</a>.
   </td>
  </tr>

  <tr>
   <td class="required">Grafické emotikony</td>
   <td>
    <select name="emoticons" tabindex="4">
     <#assign emoticons=PARAMS.emoticons?default("yes")>
     <option value="yes" <#if emoticons=="yes">SELECTED</#if>>ano</option>
     <option value="no"<#if emoticons=="no">SELECTED</#if>>ne</option>
    </select>
   </td>
  </tr>
  <tr>
   <td colspan="2">Určuje, zda má systém při zobrazování textu nahrazovat emotikony
   obrázky. Vypnutím získáte zanedbatelný nárůst rychlosti.
   </td>
  </tr>

  <tr>
   <td class="required">Zobrazovat patičku</td>
   <td>
    <select name="signatures" tabindex="5">
     <#assign emoticons=PARAMS.signatures?default("yes")>
     <option value="yes" <#if emoticons=="yes">SELECTED</#if>>ano</option>
     <option value="no"<#if emoticons=="no">SELECTED</#if>>ne</option>
    </select>
   </td>
  </tr>
  <tr>
  <tr>
   <td colspan="2">Určuje, zda má systém při zobrazování diskusních příspěvků
   zobrazovat signatury autorů příspěvků.
   </td>
  </tr>
  <td class="required">Zobrazovat avatary</td>
   <td>
    <select name="avatars" tabindex="6">
     <#assign avatars=PARAMS.avatars?default("yes")>
     <option value="yes" <#if avatars=="yes">SELECTED</#if>>ano</option>
     <option value="no"<#if avatars=="no">SELECTED</#if>>ne</option>
    </select>
   </td>
  </tr>
  <tr>
  <tr>
   <td colspan="2">Určuje, zda má systém při zobrazování diskusních příspěvků
   zobrazovat avatary autorů příspěvků.
   </td>
  </tr>

  <tr>
   <td class="required">Počet diskusí na úvodní stránce</td>
   <td>
   <#assign forums=TOOL.getUserForums(USER)>
   <table>
    <#list forums.entrySet() as forum>
        <tr><td>${TOOL.childName(TOOL.createRelation(forum.key))}</td><td><input type="text" name="discussions_${forum.key}" value="${forum.value}" size="3" tabindex="7"></td></tr>
    </#list>
   </table>
   <!--
    <select name="discussions" tabindex="7">
     <#assign discussions=PARAMS.discussions?default("20")>
     <option value="-2"<#if discussions=="-2">SELECTED</#if>>default</option>
     <option value="0" <#if discussions=="0">SELECTED</#if>>žádné</option>
     <option value="5"<#if discussions=="5">SELECTED</#if>>5</option>
     <option value="10"<#if discussions=="10">SELECTED</#if>>10</option>
     <option value="15"<#if discussions=="15">SELECTED</#if>>15</option>
     <option value="20"<#if discussions=="20">SELECTED</#if>>20</option>
     <option value="25"<#if discussions=="25">SELECTED</#if>>25</option>
     <option value="30"<#if discussions=="30">SELECTED</#if>>30</option>
     <option value="40"<#if discussions=="40">SELECTED</#if>>40</option>
     <option value="50"<#if discussions=="50">SELECTED</#if>>50</option>
    </select>
    -->
   </td>
  </tr>
  <tr>
   <td colspan="2">Zde máte možnost ovlivnit počet zobrazených diskusí
   na úvodní stránce. Nula znamená, že se dané fórum na úvodní stránce
   zobrazovat nebude.
   </td>
  </tr>

  <tr>
   <td class="required">Počet zpráviček</td>
   <td>
    <input type="text" name="news" value="${PARAMS.news?if_exists}" size="3" tabindex="8">
    <div class="error">${ERRORS.news?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td colspan="2">Podobně můžete také určit počet zpráviček, které se zobrazují. Tento počet
   je standardně nastaven na ${DEFAULT_NEWS} a můžete jej zde předefinovat.
   </td>
  </tr>

  <tr>
   <td class="required">Počet zápisků</td>
   <td>
    <input type="text" name="stories" value="${PARAMS.stories?if_exists}" size="3" tabindex="9">
    <div class="error">${ERRORS.stories?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td colspan="2">Zde můžete specifikovat počet zápisků z blogů, které se zobrazují na úvodní stránce.
   Standardní počet je nastaven na ${DEFAULT_STORIES} a můžete jej zde předefinovat. Nastavením na nulu
   zrušíte jejich zobrazování.
   </td>
  </tr>

  <tr>
   <td class="required">Zobrazovat všechny zápisky</td>
   <td>
    <select name="bannedStories" tabindex="10">
     <#assign bannedStories=PARAMS.bannedStories?default("no")>
     <option value="yes" <#if bannedStories=="yes">SELECTED</#if>>ano</option>
     <option value="no" <#if bannedStories=="no">SELECTED</#if>>ne</option>
    </select>
   </td>
  </tr>
  <tr>
   <td colspan="2">
    Administrátoři mohou zakázat zobrazování některého zápisku z blogu na titulní stránce. Typicky se
    to děje u provokací, urážek nebo tapetování. Toto nastavení způsobí, že na titulní stránce uvidíte
    všechny zápisky.
   </td>
  </tr>

  <tr>
   <td class="required">Počet desktopů</td>
   <td>
    <input type="text" name="screenshots" value="${PARAMS.screenshots?if_exists}" size="3" tabindex="11">
    <div class="error">${ERRORS.screenshots?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td colspan="2">Zde můžete ovlivnit počet zobrazovaných uživatelských desktopů na titulní stránce. Tento počet
   je standardně nastaven na ${DEFAULT_SCREENSHOTS}. Můžete jej přenastavit na hodnotu v rozmezí 0&nbsp;-&nbsp;3,
   přičemž 0 odstraní boxík se screenshoty z hlavní stránky úplně.
   </td>
  </tr>

  <tr>
   <td class="required">Velikost stránky při hledání</td>
   <td>
    <input type="text" name="search" value="${PARAMS.search?if_exists}" size="3" tabindex="12">
    <div class="error">${ERRORS.search?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td colspan="2">Počet nalezených dokumentů na jedné stránce.</td>
  </tr>

  <tr>
   <td class="required">Velikost stránky diskusního fóra</td>
   <td>
    <input type="text" name="forum" value="${PARAMS.forum?if_exists}" size="3" tabindex="13">
    <div class="error">${ERRORS.forum?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td colspan="2">Počet diskusí na stránce diskusního fóra.</td>
  </tr>

  <tr>
   <td class="required">Zobrazovat rozcestník</td>
   <td>
    <select name="guidepost" tabindex="14">
     <#assign guidepost=PARAMS.guidepost?default("yes")>
     <option value="yes" <#if guidepost=="yes">SELECTED</#if>>ano</option>
     <option value="no" <#if guidepost=="no">SELECTED</#if>>ne</option>
    </select>
   </td>
  </tr>
  <tr>
   <td colspan="2">Určuje, zda se má zobrazovat rozcestník.</td>
  </tr>

  <tr>
    <td class="required">Zobrazovat servery</td>
    <td>
        <#list SERVERS as server>
            <label>
                <#assign feedParam = "feed"+server.id>
                <input type="checkbox" name="${feedParam}"<#if PARAMS[feedParam]?exists> checked</#if>> ${server.name}<br>
            </label>
        </#list>
    </td>
  </tr>
  <tr>
   <td colspan="2">Pokud jste nevypnuli rozcestník úplně, tak zde si můžete vybrat servery,
   které se v něm mají zobrazovat.</td>
  </tr>

  <tr>
   <td class="required">Počet odkazů</td>
   <td>
       <input type="text" name="indexFeedSize" value="${PARAMS.indexFeedSize?if_exists}" size="3">
       <span class="error">${ERRORS.indexFeedSize?if_exists}</span>
       na hlavní stránce
       <input type="text" name="feedSize" value="${PARAMS.feedSize?if_exists}" size="3">
       <span class="error">${ERRORS.feedSize?if_exists}</span>
       mimo hlavní stránku
   </td>
  </tr>
  <tr>
   <td colspan="2">
    Počet odkazů pro jeden server v rozcestníku na hlavní stránce a mimo ni.
    Standardní počet je nastaven na ${DEFAULT_TEMPLATE_LINKS} a ${DEFAULT_LINKS}.
   </td>
  </tr>

  <tr>
   <td>&nbsp;</td>
   <td><input type="submit" value="Dokonči"></td>
  </tr>
 </table>
 <input type="hidden" name="action" value="editSettings2">
 <input type="hidden" name="uid" value="${MANAGED.id}">
</form>


<#include "../footer.ftl">
