<#include "../header.ftl">

<@lib.showMessages/>

<h1>N�pov�da</h1>

<p>Vypl�te �daje z�znamu. Sna�te se p�itom zapsat co nejv�ce podrobnost�,
ale d�vejte si pozor, aby v� p��sp�vek nebyl p��li�
zam��en na pou�it� n�stroj� va�� distribuce. Linux je toti� jeden, tak�e
pokud vypln�te podstatn� �daje (nap��klad n�zev jadern�ho modulu),
v� p��sp�vek pom��e i u�ivatel�m ostatn�ch distribuc�.</p>

<h1>Form�tov�n�</h1>

<p>Sm�te pou��vat z�kladn� HTML zna�ky. Pokud je nepou�ijete,
pr�zdn� ��dky budou nahrazeny nov�m odstavcem.
</p>

<form action="${URL.make("/edit")}" method="POST">

 <table width=100 border=0 cellpadding=5>
  <tr>
   <td class="required">Ovlada� je dod�v�n </td>
   <td>
    <select name="driver" tabindex=8>
     <#assign driver=PARAMS.driver?if_exists>
     <option value="kernel"<#if driver=="kernel"> SELECTED</#if>>v j�d�e</option>
     <option value="xfree"<#if driver=="xfree"> SELECTED</#if>>v XFree86</option>
     <option value="maker"<#if driver=="maker"> SELECTED</#if>>v�robcem</option>
     <option value="other"<#if driver=="other"> SELECTED</#if>>n�k�m jin�m</option>
     <option value="none"<#if driver=="none"> SELECTED</#if>>neexistuje</option>
     <option value="unknown"<#if driver=="unknown"> SELECTED</#if>>netu��m</option>
    </select>
   </td>
  </tr>

  <tr>
   <td class="required">Cena v�robku je </td>
   <td>
    <select name="price" tabindex=8>
     <#assign driver=PARAMS.price?if_exists>
     <option value="verylow"<#if driver=="verylow"> SELECTED</#if>>velmi n�zk�</option>
     <option value="low"<#if driver=="low"> SELECTED</#if>>n�zk�</option>
     <option value="good"<#if driver=="good"> SELECTED</#if>>p�im��en�</option>
     <option value="high"<#if driver=="high"> SELECTED</#if>>vysok�</option>
     <option value="toohigh"<#if driver=="toohigh"> SELECTED</#if>>p�emr�t�n�</option>
     <option value="unknown"<#if driver=="unknown"> SELECTED</#if>>nehodnot�m</option>
    </select>
   </td>
  </tr>

  <tr>
   <td>Identifikace pod Linuxem</td>
   <td>
    <div>
     Zadejte, jak toto za��zen� identifikuje Linux. K tomu v�m pomohou p��kazy
     <code>lspci</code>, <code>lsusb</code> �i <code>dmesg</code>, tyto informace
     b�vaj� tak� k dispozici v adres��i <code>/proc</code>. Nicm�n� bu�te stru�n�
     a vkl�dejte jen skute�n� zaj�mav� �daje t�kaj�c� se tohoto za��zen�, nap��klad
     <code>lsusb</code> v�m vr�t� des�tky kilobajt� text� u�ite�n�ch jen v�voj���m
     j�dra, z nich� pro na�e ��ely se hod� jen dv� t�i ��dky.
    </div>
    <textarea name="identification" cols="50" rows="4">${PARAMS.identification?if_exists?html}</textarea>
    <div class="error">${ERRORS.identification?if_exists}</div>
   </td>
  </tr>

  <tr>
   <td>Technick� parametry</td>
   <td>
    <textarea name="params" cols="50" rows="4">${PARAMS.params?if_exists?html}</textarea>
    <div class="error">${ERRORS.params?if_exists}</div>
   </td>
  </tr>

  <tr>
   <td class="required">Postup zprovozn�n�</td>
   <td>
    <textarea name="setup" cols="50" rows="10">${PARAMS.setup?if_exists?html}</textarea>
    <div class="error">${ERRORS.setup?if_exists}</div>
   </td>
  </tr>

  <tr>
   <td class="required">Pozn�mka</td>
   <td>
    <textarea name="note" cols="50" rows="10">${PARAMS.note?if_exists?html}</textarea>
    <div class="error">${ERRORS.note?if_exists}</div>
   </td>
  </tr>

  <tr>
   <td width="120">&nbsp;</td>
   <td><input type="submit" value="Dokon�i" tabindex="4"></td>
  </tr>

 </table>

 <#assign action=PARAMS.action?if_exists>
 <#if action.startsWith("addItem")>
  <input type="hidden" name="action" value="addItem3">
  <input type="hidden" name="name" value="${PARAMS.name?if_exists}">
  <input type="hidden" name="icon" value="${PARAMS.icon?if_exists}">
 <#else>
  <input type="hidden" name="action" value="${action}">
 </#if>

 <input type="hidden" name="rid" value="${PARAMS.rid}">
 <#if PARAMS.recordId?exists>
  <input type="hidden" name="recordId" value="${PARAMS.recordId}">
 </#if>
</form>


<#include "../footer.ftl">
