<#include "../header.ftl">

<@lib.showMessages/>

<h1 class="st_nadpis">Nastaven� va�eho ��tu</h1>

<p>Pro va�i ochranu nejd��ve zadejte va�e heslo.</p>

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
   <td class="required">Doba platnosti p�ihla�ovac� cookie</td>
   <td>
    <select name="cookieValid" tabindex="2">
     <#assign cookieValid=PARAMS.cookieValid?default("16070400")>
     <option value="0"<#if cookieValid=="0">SELECTED</#if>>nevytv��et</option>
     <option value="-1" <#if cookieValid=="-1">SELECTED</#if>>tato session</option>
     <option value="3600"<#if cookieValid=="3600">SELECTED</#if>>hodina</option>
     <option value="86400"<#if cookieValid=="86400">SELECTED</#if>>den</option>
     <option value="604800"<#if cookieValid=="604800">SELECTED</#if>>t�den</option>
     <option value="2678400"<#if cookieValid=="2678400">SELECTED</#if>>m�s�c</option>
     <option value="8035200"<#if cookieValid=="8035200">SELECTED</#if>>�tvrt roku</option>
     <option value="16070400"<#if cookieValid=="16070400">SELECTED</#if>>p�l roku</option>
     <option value="32140800"<#if cookieValid=="32140800">SELECTED</#if>>rok</option>
     <option value="3214080000"<#if cookieValid=="3214080000">SELECTED</#if>>sto let</option>
    </select>
   </td>
  </tr>
  <tr>
   <td colspan="2">
    <p>Toto nastaven� ovliv�uje vytv��en� cookie p�i p�ihl�en�. Standardn� se vytvo�� cookie
    s platnost� p�l roku, kter� v�s dok�e automaticky p�ihl�sit bez nutnosti zad�vat va�e heslo.
    Pokud v�ak po��ta� sd�l�te s v�ce lidmi, nap��klad ve �kole �i internetov� kav�rn�, m��e b�t toto chov�n�
    pro v�s nepraktick�.</p>

    <p>Prvn� volba je nevytv��et tuto cookie v�bec, tak�e p��t� se budete muset p�ihl�sit ru�n�.
    Druh� omez� platnost t�to cookie jen do vypnut� prohl�e�e (session), ostatn� omez� jej� d�lku
    podle popisu.</p>
   </td>
  </tr>

  <tr>
   <td class="required">Vlastn� CSS</td>
   <td>
    <input type="text" name="css" size="40" value="${PARAMS.css?if_exists}" tabindex="3">
    <div class="error">${ERRORS.url?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td colspan="2">Zadejte URL souboru obsahuj�c� CSS definici vzhledu port�lu. Bude pou�ita m�sto
   standardn�ho vzhledu. Nem�te-li r�di experimenty, ponechte pr�zdn�.
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
   <td colspan="2">Ur�uje, zda m� syst�m p�i zobrazov�n� textu nahrazovat emotikony
   obr�zky. Vypnut�m z�sk�te zanedbateln� n�r�st rychlosti.
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
   <td colspan="2">Ur�uje, zda m� syst�m p�i zobrazov�n� diskusn�ch p��sp�vk�
   zobrazovat signatury autor� p��sp�vk�.
   </td>
  </tr>

  <tr>
   <td class="required">Po�et diskus� na �vodn� str�nce</td>
   <td>
    <select name="discussions" tabindex="6">
     <#assign discussions=PARAMS.discussions?default("20")>
     <option value="-2"<#if discussions=="-2">SELECTED</#if>>default</option>
     <option value="0" <#if discussions=="0">SELECTED</#if>>��dn�</option>
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
   <td colspan="2">Zde m�te mo�nost ovlivnit po�et zobrazen�ch diskus�
   na �vodn� str�nce. Automaticky se zobraz� jen ${DEFAULT_DISCUSSIONS}
   nej�erstv�j��ch diskus�, na t�to str�nce m�te mo�nost zvolit si vlastn� po�et.
   </td>
  </tr>

  <tr>
   <td class="required">Po�et zpr�vi�ek</td>
   <td>
    <input type="text" name="news" value="${PARAMS.news?if_exists}" size="3" tabindex="7">
    <div class="error">${ERRORS.news?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td colspan="2">Podobn� m��ete tak� ur�it po�et zpr�vi�ek, kter� se zobrazuj�. Tento po�et
   je standardn� nastaven na ${DEFAULT_NEWS} a m��ete jej zde p�edefinovat.
   </td>
  </tr>

  <tr>
   <td class="required">Velikost str�nky p�i hled�n�</td>
   <td>
    <input type="text" name="search" value="${PARAMS.search?if_exists}" size="3" tabindex="8">
    <div class="error">${ERRORS.search?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td colspan="2">M��ete si tak� zvolit vlastn� velikost str�nky s nalezen�mi dokumenty.</td>
  </tr>

  <tr>
   <td class="required">Velikost str�nky diskusn�ho f�ra</td>
   <td>
    <input type="text" name="forum" value="${PARAMS.forum?if_exists}" size="3" tabindex="9">
    <div class="error">${ERRORS.forum?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td colspan="2">A po�et diskus� na str�nce jednotliv�ch diskusn�ch f�r.</td>
  </tr>

  <tr>
   <td width="200">&nbsp;</td>
   <td><input type="submit" value="Dokon�i" tabindex="10"></td>
  </tr>
 </table>
 <input type="hidden" name="action" value="editSettings2">
 <input type="hidden" name="uid" value="${MANAGED.id}">
</form>


<#include "../footer.ftl">
