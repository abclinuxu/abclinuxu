<#include "../header.ftl">
<@lib.showMessages/>

<h1 class="st_nadpis">Administrativní po¾adavky</h1>

<p>Tato stránka slou¾í pro zadávání <b>administrativních</b> po¾adavkù
na správce portálu www.abclinuxu.cz. Pokud jste na¹li chybu,
potøebujete zalo¾it novou sekci, zapomnìli jste pøihla¹ovací údaje
nebo máte námìt na zlep¹ení portálu, pou¾ijte tento formuláø.</p>

</p>Potøebujete-li poradit s Linuxem, zkuste si nejdøíve
<a href="/Search">najít</a> odpovìï sami a nenajdete-li øe¹ení,
po¾ádejte o pomoc v <a href="/diskuse.jsp">diskusním fóru</a>.
Tento formuláø v¹ak pro tyto úèely neslou¾í a proto bez odpovìdi
<u>sma¾eme</u> jakékoliv po¾adavky, které nesouvisí s chodem portálu.
</p>

<#if CHILDREN?exists && CHILDREN?size gt 0>
 <table border=0 cellpadding=5>
  <tr>
   <th>Nevyøízené po¾adavky</th>
  </tr>
  <#list CHILDREN as relation>
   <tr>
    <td>
    <b>
        ${DATE.show(relation.child.created,"CZ_FULL")}
        ${TOOL.xpath(relation.child,"/data/category")},
        ${TOOL.xpath(relation.child,"data/author")}
        <#if USER?exists && USER.hasRole("root")>${TOOL.xpath(relation.child,"data/email")}</#if>
    </b>
    <br>
    ${TOOL.render(TOOL.element(relation.child.data,"data/text"),USER?if_exists)}
    <#if USER?exists && USER.hasRole("requests admin")>
        <br>
        <a href="${URL.make("/EditRequest?action=email&requestId="+relation.id)}">Poslat email</a>,
        <a href="${URL.make("/EditRequest?action=deliver&requestId="+relation.id)}">Vyøízeno</a>,
        <a href="${URL.make("/EditRequest?action=delete&requestId="+relation.id)}">Smazat</a>,
        <a href="${URL.make("/EditRequest?action=todo&requestId="+relation.id)}">Pøesunout do TODO</a>
    </#if>
    </td>
   </tr>
  </#list>
 </table>
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
    <span class="required">Po¾adavek</span><br>
    <textarea name="text" cols="60" rows="15" tabindex="3">${PARAMS.text?if_exists?html}</textarea>
    <span class="error">${ERRORS.text?if_exists}</span>
  </td>
  </tr>
  <tr>
   <td></td>
   <td>
       <input type="submit" value="OK" tabindex="4">
       <input tabindex="5" type="submit" name="preview" value="Náhled">
   </td>
  </tr>
 </table>
 <input type="hidden" name="action" value="add">
</form>

<#include "../footer.ftl">
