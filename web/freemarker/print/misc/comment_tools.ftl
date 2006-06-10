<#include "../header.ftl">

<@lib.showMessages/>
<#assign relId = RELATION.id, dizId = RELATION.child.id>

<h1>Administrace koment���</h1>

<p>
    Jste na str�nce ur�en� pro �e�en� chyb a probl�m� t�kaj�c�ch se
    diskus� a koment���. M��ete zde reportovat na�im administr�tor�m
    �patn� za�azenou �i duplicitn� diskusi, vulg�rn� �i oso�uj�c� p��sp�vek
    a podobn�. D�kujeme v�m za va�i pomoc, v�ce o�� v�ce vid�, spole�n�
    m��eme udr�et vysokou kvalitu abclinuxu.
</p>

<fieldset>
    <legend>P��sp�vek</legend>
    <@lib.showThread COMMENT, 0, TOOL.createEmptyDiscussion(), false />
</fieldset>

<#if USER?exists && USER.hasRole("discussion admin")>
    <fieldset>
        <legend>N�stroje pro adminy</legend>
        <a href="${URL.make("/EditDiscussion/"+relId+"?action=edit&dizId="+dizId+"&threadId="+COMMENT.id)}">Upravit</a>
        <#if (COMMENT.id>0)>
            <a href="${URL.make("/EditDiscussion/"+relId+"?action=rm&amp;dizId="+dizId+"&amp;threadId="+COMMENT.id)}">Smazat</a>
            <a href="${URL.make("/EditDiscussion/"+relId+"?action=censore&amp;dizId="+dizId+"&amp;threadId="+COMMENT.id)}">Cenzura</a>
            <a href="${URL.make("/EditDiscussion/"+relId+"?action=move&amp;dizId="+dizId+"&amp;threadId="+COMMENT.id)}">P�esunout</a>
            <#if (COMMENT.parent?exists)>
                <a href="${URL.make("/EditDiscussion/"+relId+"?action=moveUp&amp;dizId="+dizId+"&amp;threadId="+COMMENT.id)}">P�esunout v��e</a>
            </#if>
            <a href="${URL.make("/EditDiscussion/"+relId+"?action=toQuestion&amp;dizId="+dizId+"&amp;threadId="+COMMENT.id)}">Osamostatnit</a>
        </#if>
        <#assign author_ip = TOOL.xpath(COMMENT.data, "//author_ip")?default("UNDEFINED")>
        <#if author_ip!="UNDEFINED">${author_ip}</#if>
    </fieldset>
</#if>

<#if TOOL.xpath(RELATION.child,"data/title")?exists>
    <p>
        Tato ot�zka je v diskusn�m f�ru <a href="/forum/dir/${RELATION.upper}">${TOOL.childName(RELATION.upper)}</a>.
        Pokud si mysl�te, �e sp�e pat�� do jin�ho f�ra,
        <a href="${URL.noPrefix("/clanky/EditRequest?action=chooseRightForum&amp;rid="+RELATION.id)}">vyberte</a>
        jej a informujte adminy, kte�� diskusi p�esunou.
    </p>
</#if>

<p>
    V tomto formul��i m��ete formulovat svou st�nost ohledn� p��sp�vku. Nejprve vyberte typ akce, kterou
    navrhujete prov�st s diskus� �i p��sp�vkem. Potom do textov�ho pole napi�te d�vody, pro� by m�li
    admini prov�st va�i ��dost, probl�m nemus� b�t patrn� na prvn� pohled. Odkaz na p��sp�vek bude
    p�id�n automaticky.
</p>

<ul>
    <li>Offtopic diskuse pou�ijte pro diskusi mimo z�b�r na�eho port�lu (v�ela�stv�, windows),
    kter� by m�la b�t smaz�na.</li>
    <li>Duplicitn� diskuse je ur�ena pro p��pad, kdy u�ivatel odeslal sv�j dotaz n�kolikr�t.</li>
    <li>N�vrh na cenzuru pou�ijte, pokud koment�� obsahuje ur�ky, vulgarismy nebo poru�uje z�kony.</li>
    <li>N�vrh na smaz�n� koment��e je velmi neobvykl� akce pou��van� obvykle p�i nepovolen� reklam�
    �i spamu, v�k�ic�ch psychicky nemocn�ch jedinc� (je�t�rci) nebo pro smaz�n� duplik�tn�ch koment���.</li>
    <li>Oprava form�tov�n� je ��dost pro �pravu formy p��sp�vku, nap��klad pokud n�kdo p�e
    bez odstavc� nebo pou�ije PRE pro cel� koment��. ��dosti o zm�nu obsahu budou zam�tnuty.</li>
</ul>

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
        <select name="category">
            <#list ["Offtopic diskuse","Duplicitn� diskuse","N�vrh na cenzuru","N�vrh na smaz�n� koment��e","Oprava form�tov�n�"] as choice>
                <option<#if PARAMS.category?if_exists==choice> selected</#if>>${choice}</option>
            </#list>
        </select>
    </td>
  </tr>
  <tr>
   <td colspan="2">
    <span class="required">Slovn� popis</span><br>
    <textarea name="text" cols="60" rows="15" tabindex="3">${PARAMS.text?if_exists?html}</textarea>
    <span class="error">${ERRORS.text?if_exists}</span>
  </td>
  </tr>
  <tr>
   <td colspan="2" align="center">
       <input type="submit" value="OK" tabindex="4">
       <input type="submit" name="preview" value="N�hled" tabindex="5">
   </td>
  </tr>
 </table>
 <input type="hidden" name="action" value="complaint">
 <input type="hidden" name="rid" value="${RELATION.id}">
 <input type="hidden" name="threadId" value="${COMMENT.id}">
</form>


<#include "../footer.ftl">
