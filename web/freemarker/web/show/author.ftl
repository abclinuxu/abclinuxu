<#include "../header.ftl">

<@lib.showMessages/>

<h1>Autor ${TOOL.childName(RELATION)}</h1>

<#if USER?exists && USER.hasRole("article admin")>
    <a href="${URL.noPrefix("/autori/edit?rid="+RELATION.id+"&action=edit")}">Upravit</a>
    <table border="0">
        <#if TOOL.xpath(AUTHOR,"/data/birthNumber")?exists>
            <tr>
                <td>Rodné èíslo:</td><td>${TOOL.xpath(AUTHOR,"/data/birthNumber")}</td>
            </tr>
        </#if>
        <#if TOOL.xpath(AUTHOR,"/data/accountNumber")?exists>
            <tr>
                 <td>Èíslo úètu:</td><td>${TOOL.xpath(AUTHOR,"/data/accountNumber")}</td>
            </tr>
        </#if>
        <#if TOOL.xpath(AUTHOR,"/data/email")?exists>
            <tr>
                 <td>Email:</td><td>${TOOL.xpath(AUTHOR,"/data/email")}</td>
            </tr>
        </#if>
        <#if TOOL.xpath(AUTHOR,"/data/phone")?exists>
            <tr>
                 <td>Telefon:</td><td>${TOOL.xpath(AUTHOR,"/data/phone")}</td>
            </tr>
        </#if>
        <#if TOOL.xpath(AUTHOR,"/data/address")?exists>
            <tr>
                 <td>Adresa:</td><td>${TOOL.xpath(AUTHOR,"/data/address")}</td>
            </tr>
        </#if>
    </table>
</#if>

<#list ARTICLES.data as article>
    <@lib.showArticle article, "SMART" />
    <#if article_has_next><hr /></#if>
</#list>

<#if (ARTICLES.currentPage.row > 0) >
<#assign start=ARTICLES.currentPage.row-ARTICLES.pageSize><#if (start<0)><#assign start=0></#if>
    <a href="${RELATION.url}?from=${start}&amp;count=${ARTICLES.pageSize}">Novìj¹í èlánky</a>
</#if>
<#assign start=ARTICLES.currentPage.row + ARTICLES.pageSize>
<#if (start < ARTICLES.total) >
    <a href="${RELATION.url}?from=${start}&amp;count=${ARTICLES.pageSize}">Star¹í èlánky</a>
</#if>

<#include "../footer.ftl">
