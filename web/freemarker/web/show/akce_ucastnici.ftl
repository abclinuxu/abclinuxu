<#include "../header.ftl">

<h1>Účastníci akce ${TOOL.childName(ITEM)}</h1>

<#assign regs=ITEM.data.selectNodes("/data/registrations/registration")>

<#if regs?exists && regs?size gt 0>
    <p>
    Následující uživatelé zaregistrovali svou účast na <a href="${RELATION.url?default("/akce/"+RELATION.id)}">této akci</a>:
    </p>

    <#if USER?exists>
        <#assign myreg=ITEM.data.selectSingleNode("/data/registrations/registration[@uid="+USER.id+"]")?default("UNDEF")>
    <#else>
        <#assign myreg="UNDEF">
    </#if>

    <#if DATE.now().compareTo(ITEM.created) lt 0>
        <form action="/akce/edit" method="post">
            <input type="hidden" name="rid" value="${RELATION.id}">
            <#if myreg=="UNDEF">
                <input type="submit" value="Registrovat svou účast">
                <input type="hidden" name="action" value="register">
            <#else>
                <input type="submit" value="Odvolat svou účast">
                <input type="hidden" name="action" value="deregister2">
            </#if>
        </form>
    </#if>

    <ul>
        <#list regs as reg>
            <#assign user=reg.attributeValue("uid")?default("UNDEF"), name=reg.attributeValue("name")>
            <#if user!="UNDEF"><#assign user=TOOL.createUser(user)></#if>

            <li>
                <#if user!="UNDEF">
                    <a href="/lide/${user.login}">${name}</a>
                <#else>
                    ${name}
                </#if>
            </li>
        </#list>
    </ul>
<#else>
    <p>Zatím se nikdo bohužel nezaregistroval.</p>
</#if>

<#include "../footer.ftl">
