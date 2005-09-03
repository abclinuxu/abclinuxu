<#include "../header.ftl">
<@lib.showMessages/>

<h1 class="st_nadpis">Administrativní po¾adavky</h1>

<p>Tato stránka slou¾í pro zadávání <b>administrativních</b> po¾adavkù
na správce portálu www.abclinuxu.cz. Pokud potøebujete zalo¾it novou sekci,
zapomnìli jste pøihla¹ovací údaje a podobnì, pou¾ijte tento formuláø.
Pokud máte námìt na vylep¹ení, nebo jste na¹li chybu, pi¹te do
<a href="http://bugzilla.abclinuxu.cz">bugzilly</a>,
u¹etøíte nám tak práci a budete mít pøehled o vyøízení va¹i ¾ádosti.</p>

<p>Potøebujete-li poradit s Linuxem, zkuste si nejdøíve
<a href="/Search">najít</a> odpovìï sami a nenajdete-li øe¹ení,
po¾ádejte o pomoc v <a href="/diskuse.jsp">diskusním fóru</a>.
Tento formuláø v¹ak pro tyto úèely neslou¾í a proto bez odpovìdi
<u>sma¾eme</u> jakékoliv po¾adavky, které nesouvisí s chodem portálu.</p>

<#if CHILDREN?exists && CHILDREN?size gt 0>

<h2>Nevyøízené po¾adavky</h2>

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
        <a href="${URL.make("/EditRequest?action=deliver&requestId="+relation.id)}">Vyøízeno</a>,
        <a href="${URL.make("/EditRequest?action=delete&requestId="+relation.id)}">Smazat</a>,
        <a href="${URL.make("/EditRequest?action=todo&requestId="+relation.id)}">Pøesunout do TODO</a>
    </#if>
  </p><hr />
</#list>

</#if>

<#if PARAMS.preview?exists>
    <fieldset>
        <legend>Náhled</legend>
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
   <td class="required">Va¹e jméno</td>
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
    <td class="required">Vá¹ email</td>
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
    <td>Typ po¾adavku</td>
    <td>
        <#if PARAMS.categoryPosition?exists>
            <#assign defaultCategory=CATEGORIES[PARAMS.categoryPosition?eval]>
        <#else>
            <#assign defaultCategory="Hlá¹ení chyby">
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
    <span class="required">Po¾adavek</span>
    <div class="error">${ERRORS.text?if_exists}</div>
    <textarea name="text" cols="60" rows="15" tabindex="3">${PARAMS.text?if_exists?html}</textarea>
  </td>
  </tr>
  <tr>
   <td colspan="2">
       <input type="submit" value="Odeslat" tabindex="5">
       <input type="submit" name="preview" value="Náhled" tabindex="4">
   </td>
  </tr>
 </table>
 <input type="hidden" name="action" value="add">
</form>

<#include "../footer.ftl">
