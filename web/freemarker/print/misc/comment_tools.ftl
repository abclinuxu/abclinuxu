<#include "../header.ftl">

<@lib.showMessages/>
<#assign relId = RELATION.id, dizId = RELATION.child.id>

<h1>Administrace komentáøù</h1>

<p>
    Jste na stránce urèené pro øe¹ení chyb a problémù týkajících se
    diskusí a komentáøù. Mù¾ete zde reportovat na¹im administrátorùm
    ¹patnì zaøazenou èi duplicitní diskusi, vulgární èi osoèující pøíspìvek
    a podobnì. Dìkujeme vám za va¹i pomoc, více oèí více vidí, spoleènì
    mù¾eme udr¾et vysokou kvalitu abclinuxu.
</p>

<fieldset>
    <legend>Pøíspìvek</legend>
    <@lib.showThread COMMENT, 0, TOOL.createEmptyDiscussion(), false />
</fieldset>

<#if USER?exists && USER.hasRole("discussion admin")>
    <fieldset>
        <legend>Nástroje pro adminy</legend>
        <a href="${URL.make("/EditDiscussion/"+relId+"?action=edit&dizId="+dizId+"&threadId="+COMMENT.id)}">Upravit</a>
        <#if (COMMENT.id>0)>
            <a href="${URL.make("/EditDiscussion/"+relId+"?action=rm&amp;dizId="+dizId+"&amp;threadId="+COMMENT.id)}">Smazat</a>
            <a href="${URL.make("/EditDiscussion/"+relId+"?action=censore&amp;dizId="+dizId+"&amp;threadId="+COMMENT.id)}">Cenzura</a>
            <a href="${URL.make("/EditDiscussion/"+relId+"?action=move&amp;dizId="+dizId+"&amp;threadId="+COMMENT.id)}">Pøesunout</a>
            <#if (COMMENT.parent?exists)>
                <a href="${URL.make("/EditDiscussion/"+relId+"?action=moveUp&amp;dizId="+dizId+"&amp;threadId="+COMMENT.id)}">Pøesunout vý¹e</a>
            </#if>
            <a href="${URL.make("/EditDiscussion/"+relId+"?action=toQuestion&amp;dizId="+dizId+"&amp;threadId="+COMMENT.id)}">Osamostatnit</a>
        </#if>
        <#assign author_ip = TOOL.xpath(COMMENT.data, "//author_ip")?default("UNDEFINED")>
        <#if author_ip!="UNDEFINED">${author_ip}</#if>
    </fieldset>
</#if>

<#if TOOL.xpath(RELATION.child,"data/title")?exists>
    <p>
        Tato otázka je v diskusním fóru <a href="/forum/dir/${RELATION.upper}">${TOOL.childName(RELATION.upper)}</a>.
        Pokud si myslíte, ¾e spí¹e patøí do jiného fóra,
        <a href="${URL.noPrefix("/clanky/EditRequest?action=chooseRightForum&amp;rid="+RELATION.id)}">vyberte</a>
        jej a informujte adminy, kteøí diskusi pøesunou.
    </p>
</#if>

<p>
    V tomto formuláøi mù¾ete formulovat svou stí¾nost ohlednì pøíspìvku. Nejprve vyberte typ akce, kterou
    navrhujete provést s diskusí èi pøíspìvkem. Potom do textového pole napi¹te dùvody, proè by mìli
    admini provést va¹i ¾ádost, problém nemusí být patrný na první pohled. Odkaz na pøíspìvek bude
    pøidán automaticky.
</p>

<ul>
    <li>Offtopic diskuse pou¾ijte pro diskusi mimo zábìr na¹eho portálu (vèelaøství, windows),
    která by mìla být smazána.</li>
    <li>Duplicitní diskuse je urèena pro pøípad, kdy u¾ivatel odeslal svùj dotaz nìkolikrát.</li>
    <li>Návrh na cenzuru pou¾ijte, pokud komentáø obsahuje urá¾ky, vulgarismy nebo poru¹uje zákony.</li>
    <li>Návrh na smazání komentáøe je velmi neobvyklá akce pou¾ívaná obvykle pøi nepovolené reklamì
    èi spamu, výkøicích psychicky nemocných jedincù (je¹tírci) nebo pro smazání duplikátních komentáøù.</li>
    <li>Oprava formátování je ¾ádost pro úpravu formy pøíspìvku, napøíklad pokud nìkdo pí¹e
    bez odstavcù nebo pou¾ije PRE pro celý komentáø. ®ádosti o zmìnu obsahu budou zamítnuty.</li>
</ul>

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
        <select name="category">
            <#list ["Offtopic diskuse","Duplicitní diskuse","Návrh na cenzuru","Návrh na smazání komentáøe","Oprava formátování"] as choice>
                <option<#if PARAMS.category?if_exists==choice> selected</#if>>${choice}</option>
            </#list>
        </select>
    </td>
  </tr>
  <tr>
   <td colspan="2">
    <span class="required">Slovní popis</span><br>
    <textarea name="text" cols="60" rows="15" tabindex="3">${PARAMS.text?if_exists?html}</textarea>
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
 <input type="hidden" name="action" value="complaint">
 <input type="hidden" name="rid" value="${RELATION.id}">
 <input type="hidden" name="threadId" value="${COMMENT.id}">
</form>


<#include "../footer.ftl">
