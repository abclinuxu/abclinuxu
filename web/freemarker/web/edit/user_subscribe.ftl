<#include "../header.ftl">

<@lib.showMessages/>

<p>Ná¹ portál pro vás pøipravil dvì atraktivní
slu¾by. První z nich je Týdenní souhrn a je
urèen tìm, kteøí nemají èas dennì nás nav¹tìvovat,
ale nechtìjí pøijít o ¾ádné na¹e èlánky èi zprávièky.
Pokud si jej pøihlásíte, ka¾dý víkend vám za¹leme emailem seznam
èlánkù a v¹echny zprávièky, které jsme daný týden vydali.
</p>

<p>Dal¹í slu¾bou je Zpravodaj portálu AbcLinuxu.cz.
Pokud si jej pøihlásíte, zaèátkem ka¾dého mìsíce
obdr¾íte email se spoustou zajímavostí ze svìta
Linuxu i z na¹eho portálu.
</p>

<p>Novinkou v testovacím re¾imu je emailové rozhraní
k diskusnímu fóru. Pro ka¾dý nový pøíspìvek diskuse
umístìné v nìkterém fóru se ode¹le v¹em pøihlá¹eným
u¾ivatelùm email s jeho obsahem a adresou, na které je
mo¾né odpovìdìt.
</p>

<p>Pro va¹i ochranu nejdøíve zadejte souèasné heslo.
</p>

<form action="${URL.noPrefix("/EditUser")}" method="POST">
 <table width="100%" border=0 cellpadding=5>
  <tr>
   <td class="required" width="60">Heslo</td>
   <td>
    <input type="password" name="PASSWORD" size="16" tabindex="1" class="pole">
    <div class="error">${ERRORS.PASSWORD?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="60">Týdenní souhrn</td>
   <td>
    <select name="weekly" tabindex="2">
     <#assign weekly=PARAMS.weekly?if_exists>
     <option value="yes" <#if weekly=="yes">SELECTED</#if>>ano</option>
     <option value="no"<#if weekly=="no">SELECTED</#if>>ne</option>
    </select>
   </td>
  </tr>
  <tr>
   <td width="60">Mìsíèní zpravodaj</td>
   <td>
    <select name="monthly" tabindex="3">
     <#assign monthly=PARAMS.monthly?if_exists>
     <option value="yes" <#if monthly=="yes">SELECTED</#if>>ano</option>
     <option value="no"<#if monthly=="no">SELECTED</#if>>ne</option>
    </select>
   </td>
  </tr>
  <tr>
   <td width="60">Diskusní fórum</td>
   <td>
    <select name="forum" tabindex="4">
     <#assign forum=PARAMS.forum?if_exists>
     <option value="yes" <#if forum=="yes">SELECTED</#if>>ano</option>
     <option value="no"<#if forum=="no">SELECTED</#if>>ne</option>
    </select>
   </td>
  </tr>
  <tr>
   <td width="60">&nbsp;</td>
   <td><input type="submit" value="Dokonèi" tabindex="5" class="buton"></td>
  </tr>
 </table>
 <input type="hidden" name="action" value="subscribe2">
 <input type="hidden" name="uid" value="${MANAGED.id}">
</form>


<#include "../footer.ftl">
