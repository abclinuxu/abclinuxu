<#include "../header.ftl">
<@lib.showMessages/>

<h1 class="st_nadpis">Administrativn� po�adavky</h1>

<p>Tato str�nka slou�� pro zad�v�n� <b>administrativn�ch</b> po�adavk�
na spr�vce port�lu www.abclinuxu.cz. Pokud pot�ebujete zalo�it novou sekci,
zapomn�li jste p�ihla�ovac� �daje a podobn�, pou�ijte tento formul��.
Pokud m�te n�m�t na vylep�en�, nebo jste na�li chybu, pi�te do
<a href="http://bugzilla.abclinuxu.cz">bugzilly</a>,
u�et��te n�m tak pr�ci a budete m�t p�ehled o vy��zen� va�i ��dosti.</p>

<p>Pot�ebujete-li poradit s Linuxem, zkuste si nejd��ve
<a href="/Search">naj�t</a> odpov�� sami a nenajdete-li �e�en�,
po��dejte o pomoc v <a href="/diskuse.jsp">diskusn�m f�ru</a>.
Tento formul�� v�ak pro tyto ��ely neslou�� a proto bez odpov�di
<u>sma�eme</u> jak�koliv po�adavky, kter� nesouvis� s chodem port�lu.</p>

<#if CHILDREN?exists && CHILDREN?size gt 0>

<h2>Nevy��zen� po�adavky</h2>

<#list CHILDREN as relation>

  <p><b>
    ${DATE.show(relation.child.created,"CZ_FULL")}
    ${TOOL.xpath(relation.child,"/data/category")},
    ${TOOL.xpath(relation.child,"data/author")}
    <#if USER?exists && USER.hasRole("root")>${TOOL.xpath(relation.child,"data/email")}</#if>
   </b><br />
    ${TOOL.render(TOOL.element(relation.child.data,"data/text"),USER?if_exists)}
    <#if USER?exists && USER.hasRole("requests admin")>
        <br />
        <a href="${URL.make("/EditRequest?action=email&requestId="+relation.id)}">Poslat email</a>,
        <a href="${URL.make("/EditRequest?action=deliver&requestId="+relation.id)}">Vy��zeno</a>,
        <a href="${URL.make("/EditRequest?action=delete&requestId="+relation.id)}">Smazat</a>,
        <a href="${URL.make("/EditRequest?action=todo&requestId="+relation.id)}">P�esunout do TODO</a>
    </#if>
  </p><hr />
</#list>

</#if>

<#if PARAMS.preview?exists>
    <fieldset>
        <legend>N�hled</legend>
        <b>
            ${PARAMS.category}
            ${PARAMS.author}
        </b>
        <br>
        ${TOOL.render(PARAMS.text,USER?if_exists)}
    </fieldset>
</#if>

<form action="${URL.make("/EditRequest")}" method="POST">
 <table border=0 cellpadding=5 style="padding-top: 10px">
  <tr>
   <td class="required">Va�e jm�no</td>
   <#if PARAMS.author?exists>
    <#assign author=PARAMS.author>
   <#elseif USER?exists>
    <#assign author=USER.name>
   </#if>
   <td align="left">
    <input type="text" name="author" value="${author?if_exists}" size="20" tabindex="1">
    <span class="error">${ERRORS.author?if_exists}</span>
   </td>
  </tr>
  <tr>
    <td class="required">V� email</td>
   <#if PARAMS.email?exists>
    <#assign email=PARAMS.email>
   <#elseif USER?exists>
    <#assign email=USER.email>
   </#if>
   <td align="left">
    <input type="text" name="email" value="${email?if_exists}" size="20" tabindex="2">
    <span class="error">${ERRORS.email?if_exists}</span>
   </td>
  </tr>
  <tr>
    <td>Typ po�adavku</td>
    <td>
        <#if PARAMS.categoryPosition?exists>
            <#assign defaultCategory=CATEGORIES[PARAMS.categoryPosition?eval]>
        <#else>
            <#assign defaultCategory="Hl�en� chyby">
        </#if>
        <select name="category">
            <#list CATEGORIES as category>
                <option<#if PARAMS.category?default(defaultCategory)==category> selected</#if>>${category}</option>
            </#list>
        </select>
    </td>
  </tr>
  <tr>
   <td colspan="2">
    <span class="required">Po�adavek</span>
    <div class="error">${ERRORS.text?if_exists}</div>
    <textarea name="text" cols="60" rows="15" tabindex="3">${PARAMS.text?if_exists?html}</textarea>
  </td>
  </tr>
  <tr>
   <td colspan="2">
       <input type="submit" value="Odeslat" tabindex="5">
       <input type="submit" name="preview" value="N�hled" tabindex="4">
   </td>
  </tr>
 </table>
 <input type="hidden" name="action" value="add">
</form>

<#include "../footer.ftl">
