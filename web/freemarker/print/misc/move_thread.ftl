<#include "../header.ftl">

<@lib.showMessages/>

<p>Chystáte se pøesunout vlákno na jinou pozici ve stromì.
Toto je velmi neobvyklý krok a dává smysl jen tehdy, kdy¾
se chystáte diskusi rozdìlit. Zadejte prosím èíslo nového rodièe
zvoleného vlákna. Pokud chcete vlákno pøesunout na nejvy¹¹í
úroveò, místo èísla pøedka zadejte nulu. Dávejte pozor, aby
ve stromì diskuse nevznikla smyèka - pokud komentáø pøesunete
do nìkterého jeho potomka, vznikne velký problém!</p>

<form action="${URL.make("/EditDiscussion")}" method="POST">
 <table cellpadding="5">
  <tr>
   <td class="required">Pøedek</td>
   <td>
     <input type="text" name="parentId" size="4" value="${PARAMS.parentId?if_exists}">
   </td>
  </tr>
  <tr>
   <td>&nbsp;</td>
   <td>
    <input type="submit" name="finish" value="Dokonèi">
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
  Èíslo komentáøe: ${comment.id}<br>
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
