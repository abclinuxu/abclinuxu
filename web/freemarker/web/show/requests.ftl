<#include "../header.ftl">
<@lib.showMessages/>

<h1>Administrativní požadavky</h1>

<p>Tato stránka slouží pro zadávání <b>administrativních</b> požadavků
na správce portálu www.abclinuxu.cz. Pokud potřebujete založit novou sekci,
zapomněli jste přihlašovací údaje a podobně, použijte tento formulář.
Pokud máte námět na vylepšení, nebo jste našli chybu, pište do
<a href="http://bugzilla.abclinuxu.cz">bugzilly</a>,
ušetříte nám tak práci a budete mít přehled o vyřízení vaši žádosti.</p>

<p>Jedná-li se o námět na novou anketu, považujte jeho smazání za jeho vyřízení.
Neznamená to, že bychom ho ignorovali, ale že jsme ho zařadili do seznamu
dalších námětů. Ne všechny náměty se stanou anketami, protože námětů je prostě
moc. Z vašich návrhů vybíráme ty nejzajímavější.</p>

<p>Potřebujete-li poradit s Linuxem, zkuste si nejdříve
<a href="/hledani">najít</a> odpověď sami a nenajdete-li řešení,
požádejte o pomoc v <a href="/poradna">poradně</a>.
Tento formulář však pro tyto účely neslouží, a proto bez odpovědi
<u>smažeme</u> jakékoliv požadavky, které nesouvisí s chodem portálu.</p>

<#if CHILDREN?exists && CHILDREN?size gt 0>

<h2>Nevyřízené požadavky</h2>

<#list SORT.byDate(CHILDREN) as relation>
    <#assign item = relation.child>
    <p>
        <b>
            <span id="${relation.id}">${DATE.show(item.created,"SMART")}</span>
            ${TOOL.xpath(item,"/data/category")},
            ${TOOL.xpath(item,"data/author")}
            <#if USER?exists && USER.hasRole("root")>${TOOL.xpath(item,"data/email")}</#if>
        </b>
        <br />

        <#assign url = TOOL.xpath(item, "data/url")?default("UNDEFINED")>
        <#if url != "UNDEFINED">
            <a href="${url}">${url}</a>
        </#if>
        ${TOOL.render(TOOL.element(item.data,"data/text"),USER?if_exists)}

        <#if USER?exists && TOOL.permissionsFor(USER, RELATION).canModify()>
            <br />
            <a href="${URL.make("/EditRequest?action=email&requestId="+relation.id)}">Poslat email</a>,
            <a href="${URL.make("/EditRequest?action=deliver&requestId="+relation.id+TOOL.ticket(USER, false))}">Vyřízeno</a>,
            <a href="${URL.make("/EditRequest?action=delete&requestId="+relation.id+TOOL.ticket(USER, false))}">Smazat</a>
        </#if>
    </p>
    <hr />
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

<p>Chyby prosím hlaste do <a href="http://bugzilla.abclinuxu.cz">bugzilly</a>.</p>

<a name="form"></a>
<form action="${URL.make("/EditRequest")}" method="POST">
 <table border=0 cellpadding=5 style="padding-top: 10px">
  <tr>
   <td class="required">Vaše jméno</td>
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
    <td class="required">Váš email</td>
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
    <td>Typ požadavku</td>
    <td>
        <#if PARAMS.categoryPosition?exists>
            <#assign defaultCategory=CATEGORIES[PARAMS.categoryPosition?eval]>
        <#else>
            <#assign defaultCategory="Hlášení chyby">
        </#if>
        <select name="category">
            <#list CATEGORIES as category>
                <option<#if PARAMS.category?default(defaultCategory)==category> selected</#if>>${category}</option>
            </#list>
        </select>
    </td>
  </tr>
    <#if ! (USER?exists || USER_VERIFIED?if_exists)>
        <tr>
            <td class="required">Aktuální rok</td>
            <td>
                <input type="text" size="4" name="antispam" value="${PARAMS.antispam?if_exists?html}">
                <a class="info" href="#">?<span class="tooltip">Vložte aktuální rok. Jedná se o ochranu před spamboty.
                Po úspěšném ověření se uloží cookie (včetně vašeho jména) a tato kontrola přestane být prováděna.</span></a>
                <span class="error">${ERRORS.antispam?if_exists}</span>
            </td>
        </tr>
    </#if>
  <tr>
   <td colspan="2">
    <span class="required">Požadavek</span>
    <div class="error">${ERRORS.text?if_exists}</div>
    <textarea name="text" cols="60" rows="15">${PARAMS.text?if_exists?html}</textarea>
  </td>
  </tr>
  <tr>
   <td colspan="2">
       <input type="submit" name="preview" value="Náhled">
       <input type="submit" value="Odeslat">
   </td>
  </tr>
 </table>
 <input type="hidden" name="action" value="add">
 <#if PARAMS.url?exists>
    <input type="hidden" name="url" value="${PARAMS.url}">
 </#if>
</form>

<#include "../footer.ftl">
