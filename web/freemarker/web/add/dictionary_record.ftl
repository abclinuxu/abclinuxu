<#include "../header.ftl">

<@lib.showMessages/>

<h1 class="st_nadpis">�vod</h1>

<p>C�lem t�to slu�by je vytvo�it rozs�hl� v�kladov� slovn�k
nejr�zn�j��ch pojm� t�kaj�c�ch se Linuxu �i Unixu.  ��m v�ce
pojm� bude kvalitn� pokr�vat, t�m sn�ze se nov��ci zorientuj�
v Linuxu a zv��� se �ance, �e nebudou kl�st ot�zky vypl�vaj�c�
z nepochopen� z�kladn�ch princip� tohoto opera�n�ho syst�mu.
</p>

<h1 class="st_nadpis">Nov� pojem</h1>

<p>Ka�d� pojem ve slovn�ku se skl�d� z n�zvu a popisu. N�zev
odpov�d� pojmu v prvn�m p�d� jednotn�ho ��sla. Nap��klad
souborov� syst�m, kernel, speci�ln� za��zen� �i symbolick� odkaz.
Popis pak obsahuje vysv�tlen� tohoto pojmu. Pokud v popisu nepou�ijete
formatovac� znaky nov� ��dek a odstavec, popis bude ulo�en v takzvan�m
zjednodu�en�m form�tu, kdy pr�zdn� ��dek bude nahrazen zna�kou
pro nov� odstavec.
</p>

<#if PARAMS.preview?exists && PARAMS.desc?exists>
 <h1 class="st_nadpis">N�hled</h1>
  <p class="slovnik">
   ${TOOL.render(PARAMS.desc,USER?if_exists)}
  </p>
 <br><br>
</#if>

<form action="${URL.make("/edit")}" method="POST">
 <table cellpadding="0" border="0" width="100%">
  <tr>
   <td colspan="2" class="required">Popis</td>
  </tr>
  <tr>
   <td colspan="2">
    <textarea name="desc" cols="70" rows="20" tabindex="1">${PARAMS.desc?if_exists?html}</textarea>
    <div class="error">${ERRORS.desc?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td colspan="2">
    <input type="submit" name="preview" value="N�hled">
    <#if PARAMS.preview?exists><input type="submit" name="submit" value="Dokon�i"></#if>
   </td>
  </tr>
 </table>
 <input type="hidden" name="action" value="addRecord2">
 <input type="hidden" name="rid" value="${RELATION.id}">
</form>

<#include "../footer.ftl">
