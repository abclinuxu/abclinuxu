<#include "../header.ftl">

<@lib.showMessages/>

<p>Chyst�te se p�esunout vl�kno na jinou pozici ve strom�.
Toto je velmi neobvykl� krok a d�v� smysl jen tehdy, kdy�
se chyst�te diskusi rozd�lit. Zadejte pros�m ��slo nov�ho rodi�e
zvolen�ho vl�kna. Pokud chcete vl�kno p�esunout na nejvy���
�rove�, m�sto ��sla p�edka zadejte nulu. D�vejte pozor, aby
ve strom� diskuse nevznikla smy�ka - pokud koment�� p�esunete
do n�kter�ho jeho potomka, vznikne velk� probl�m!</p>

<form action="${URL.make("/EditDiscussion")}" method="POST">
 <table cellpadding="5">
  <tr>
   <td class="required">P�edek</td>
   <td>
     <input type="text" name="parentId" size="4" value="${PARAMS.parentId?if_exists}">
   </td>
  </tr>
  <tr>
   <td>&nbsp;</td>
   <td>
    <input type="submit" name="finish" value="Dokon�i">
   </td>
  </tr>
 </table>
 <input type="hidden" name="action" value="move2">
 <input type="hidden" name="rid" value="${RELATION.id}">
 <input type="hidden" name="dizId" value="${PARAMS.dizId}">
 <input type="hidden" name="threadId" value="${PARAMS.threadId}">
</form>

<#assign diz = TOOL.createDiscussionTree(DISCUSSION,"no",RELATION.id,false)>
<#list diz.threads as thread>
 <@showThread thread, 0 />
</#list>

<#macro showComment(comment) >
 <p class="diz_header">
  ��slo koment��e: ${comment.id}<br>
  ${DATE.show(comment.created,"CZ_FULL")}
  <#if comment.author?exists>
   <#local who=TOOL.createUser(comment.author)>
   <a href="/Profile/${who.id}">${who.nick?default(who.name)}</a><br>
  <#else>
   ${comment.anonymName?if_exists}<br>
  </#if>
  ${comment.title?if_exists}<br>
 </p>
  <div>${TOOL.render(TOOL.element(comment.data,"text"),USER?if_exists)}</div>
</#macro>

<#macro showThread(diz level)>
 <#local space=level*15>
 <div style="padding-left: ${space}pt">
  <@showComment diz />
 </div>
 <#if diz.children?exists>
  <#local level2=level+1>
  <#list diz.children as child>
   <@showThread child, level2 />
  </#list>
 </#if>
</#macro>

<#include "../footer.ftl">
