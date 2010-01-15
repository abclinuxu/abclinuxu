<#include "../header.ftl">

<@lib.showMessages/>
<#assign relId = RELATION.id, dizId = RELATION.child.id>

<h1>Administrace komentářů</h1>

<p>
    Jste na stránce určené pro řešení chyb a problémů týkajících se
    diskusí a komentářů. Můžete zde našim administrátorům reportovat
    špatně zařazenou či duplicitní diskusi, vulgární či osočující příspěvek
    a podobně. Děkujeme vám za vaši pomoc, více očí více vidí, společně
    můžeme udržet vysokou kvalitu AbcLinuxu.cz.
</p>

<#if USER?? && USER.hasRole("discussion admin")>
    <fieldset>
        <legend>Nástroje pro adminy</legend>
        <#assign author_ip = TOOL.xpath(COMMENT.data, "//author_ip")!"UNDEFINED">
        <#if author_ip!="UNDEFINED">${author_ip}</#if>
        <a href="${URL.make("/EditDiscussion/"+relId+"?action=edit&dizId="+dizId+"&threadId="+COMMENT.id)}">Upravit</a>
        <#if (COMMENT.id>0)>
            <a href="${URL.make("/EditDiscussion/"+relId+"?action=rm&amp;dizId="+dizId+"&amp;threadId="+COMMENT.id)}">Smazat</a>
            <a href="${URL.make("/EditDiscussion/"+relId+"?action=censore&amp;dizId="+dizId+"&amp;threadId="+COMMENT.id)}">Cenzura</a>
            <a href="${URL.make("/EditDiscussion/"+relId+"?action=move&amp;dizId="+dizId+"&amp;threadId="+COMMENT.id)}">Přesunout</a>
            <#if (COMMENT.parent??)>
                <a href="${URL.make("/EditDiscussion/"+relId+"?action=moveUp&amp;dizId="+dizId+"&amp;threadId="+COMMENT.id+TOOL.ticket(USER, false))}">Přesunout výše</a>
            </#if>
            <a href="${URL.make("/EditDiscussion/"+relId+"?action=toQuestion&amp;dizId="+dizId+"&amp;threadId="+COMMENT.id)}">Osamostatnit</a>
            <a href="${URL.make("/inset/"+relId+"?action=manage&amp;threadId="+COMMENT.id)}">Správa příloh</a>
        </#if>
    </fieldset>
</#if>

<fieldset>
    <legend>Příspěvek</legend>
    <@lib.showThread COMMENT, 0, TOOL.createEmptyDiscussionWithAttachments(RELATION.child), false />
</fieldset>

<#if TOOL.isQuestion(RELATION.child)>
    <p>
        Tato otázka je v diskusním fóru <a href="/forum/dir/${RELATION.upper}">${TOOL.childName(RELATION.upper)}</a>.
        Pokud si myslíte, že spíše patří do jiného fóra,
        <a href="${URL.noPrefix("/clanky/EditRequest?action=chooseRightForum&amp;rid="+RELATION.id)}">vyberte</a>
        jej a informujte adminy, kteří diskusi přesunou.
    </p>
</#if>

<p>
    V tomto formuláři můžete formulovat svou stížnost ohledně příspěvku. Nejprve vyberte typ akce, kterou
    navrhujete provést s diskusí či příspěvkem. Potom do textového pole napište důvody, proč by měli
    admini provést vaši žádost, problém nemusí být patrný na první pohled. Odkaz na příspěvek bude
    přidán automaticky.
</p>

<ul>
    <li>Offtopic diskuse použijte pro diskusi mimo záběr našeho portálu (včelařství, windows),
    která by měla být smazána.</li>
    <li>Duplicitní diskuse je určena pro případ, kdy uživatel odeslal svůj dotaz několikrát.</li>
    <li>Návrh na cenzuru použijte, pokud komentář obsahuje urážky, vulgarismy nebo porušuje zákony.</li>
    <li>Návrh na smazání komentáře je velmi neobvyklá akce používaná obvykle při nepovolené reklamě
    či spamu, výkřicích psychicky nemocných jedinců (ještírci) nebo pro smazání duplikátních komentářů.</li>
    <li>Oprava formátování je žádost pro úpravu formy příspěvku, například pokud někdo píše
    bez odstavců nebo použije PRE pro celý komentář. Žádosti o změnu obsahu budou zamítnuty.</li>
</ul>

<#if PARAMS.preview??>
    <fieldset>
        <legend>Náhled</legend>
        <b>
            ${PARAMS.category}
            ${PARAMS.author}
        </b>
        <br>
        ${TOOL.render(PARAMS.text,USER!)}
    </fieldset>
</#if>

<form action="${URL.make("/EditRequest")}" method="POST">
    <table border=0 cellpadding=5 style="padding-top: 10px">
        <tr>
            <td class="required">Vaše jméno</td>
            <#if PARAMS.author??>
                <#assign author=PARAMS.author>
            <#elseif USER??>
                <#assign author=USER.name>
            </#if>
            <td align="left">
                <input type="text" name="author" value="${author!}" size="20">
                <span class="error">${ERRORS.author!}</span>
            </td>
        </tr>
        <tr>
            <td class="required">Váš email</td>
            <#if PARAMS.email??>
                <#assign email=PARAMS.email>
            <#elseif USER??>
                <#assign email=USER.email>
            </#if>
            <td align="left">
                <input type="text" name="email" value="${email!}" size="20">
                <span class="error">${ERRORS.email!}</span>
            </td>
        </tr>
        <tr>
            <td>Typ požadavku</td>
            <td>
                <select name="category">
                    <#list ["Offtopic diskuse","Duplicitní diskuse","Návrh na cenzuru","Návrh na smazání komentáře","Oprava formátování"] as choice>
                        <option<#if PARAMS.category! == choice> selected</#if>>${choice}</option>
                    </#list>
                </select>
            </td>
        </tr>
        <#if ! (USER?? || USER_VERIFIED!false)>
            <tr>
                <td class="required">Aktuální rok</td>
                <td>
                    <input type="text" size="4" name="antispam" value="${PARAMS.antispam!?html}">
                    <a class="info" href="#">?<span class="tooltip">Vložte aktuální rok. Jedná se o ochranu před spamboty.
                    Po úspěšném ověření se uloží cookie (včetně vašeho jména) a tato kontrola přestane být prováděna.</span></a>
                    <span class="error">${ERRORS.antispam!}</span>
                </td>
            </tr>
        </#if>
        <tr>
            <td colspan="2">
                Slovní popis<br>
                <div class="error">${ERRORS.text!}</div>
                <textarea name="text" cols="60" rows="15">${PARAMS.text!?html}</textarea>
            </td>
        </tr>
        <tr>
            <td colspan="2">
                <input type="submit" name="preview" value="Náhled">
                <input type="submit" value="Odeslat">
            </td>
        </tr>
    </table>
    <input type="hidden" name="action" value="submitComplaint">
    <input type="hidden" name="rid" value="${RELATION.id}">
    <input type="hidden" name="threadId" value="${COMMENT.id}">
</form>

<#include "../footer.ftl">
