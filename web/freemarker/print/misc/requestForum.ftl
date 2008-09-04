<#include "../header.ftl">

<@lib.showMessages/>

<p>Nyní prosím vyberte vhodnější fórum pro tuto diskusi,
Pak zadejte do formuláře své jméno, emailovou adresu
a případný vzkaz.
</p>

<form action="${URL.make("/EditRequest")}" method="POST">
 <table border=0 cellpadding=5>
  <#list FORUMS?keys as name>
   <#assign FORUM=FORUMS[name]>
   <tr><td colspan="4" align="center">${name}</td></tr>
   <#list FORUM as i>
    <#if i_index%4==0><tr></#if>
    <td>
     <input type="radio" name="forumId" value="${i.id}"<#if PARAMS.forumId?default("0")==i.id?string> checked</#if>>
     <a href="/forum/dir/${i.id}">${TOOL.childName(i)}</a>
    </td>
    <#if i_index%4==3></tr></#if>
   </#list>
  </#list>
 </table>

<#if PARAMS.preview?exists>
    <fieldset>
        <legend>Náhled</legend>
        <b>
            ${PARAMS.category}
            ${PARAMS.author}
        </b>
        <br>
        ${TOOL.render(PARAMS.text?if_exists,USER?if_exists)}
    </fieldset>
</#if>


 <table border=0 cellpadding=5>
  <tr>
   <td class="required">Vaše jméno</td>
   <#if PARAMS.author?exists><#assign author=PARAMS.author><#elseif USER?exists><#assign author=USER.name></#if>
   <td>
    <input type="text" name="author" value="${author?if_exists}" size="20" tabindex="1">
    <span class="error">${ERRORS.author?if_exists}</span>
   </td>
  </tr>
  <tr>
    <td class="required">Váš email</td>
   <#if PARAMS.email?exists><#assign email=PARAMS.email><#elseif USER?exists><#assign email=USER.email></#if>
   <td>
    <input type="text" name="email" value="${email?if_exists}" size="20" tabindex="2">
    <span class="error">${ERRORS.email?if_exists}</span>
   </td>
  </tr>
  <tr>
   <td>Vzkaz</td>
   <td>
    <textarea name="text" cols="60" rows="5" tabindex="3">${PARAMS.text?if_exists?html}</textarea>
    <span class="error">${ERRORS.text?if_exists}</span>
   </td>
  </tr>
  <tr>
   <td colspan="2" align="center">
       <input type="submit" value="OK" tabindex="4">
       <input type="submit" name="preview" value="Náhled" tabindex="5">
   </td>
  </tr>
 </table>
 <input type="hidden" name="rid" value="${PARAMS.rid}">
 <input type="hidden" name="action" value="rightForum">
</form>

<#include "../footer.ftl">
