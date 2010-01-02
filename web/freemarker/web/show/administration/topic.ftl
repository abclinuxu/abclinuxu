<#include "../../header.ftl">

<#if LAYOUT != "print">
    <@lib.showSignPost "Rozcestník">
    <ul>
       <#if ! EDITOR?? && ! TOPIC.author??>
            <li>
                <a href="${URL.make("/sprava/redakce/namety/edit/" + TOPIC.relationId + "?action=accept"+TOOL.ticket(USER, false))}">
                    Přijmout námět
                </a>
            </li>
       </#if>
        <#if EDITOR??>
            <li>
                <a href="${URL.make("/sprava/redakce/namety/?action=notify&amp;trid="+TOPIC.relationId)}">Zaslat notifikaci</a>
            </li>
            <li>
                <a href="${URL.make("/sprava/redakce/namety/edit/"+TOPIC.relationId)+"?action=edit"}">Upravit</a>
            </li>
            <li>
                <a href="${URL.make("/sprava/redakce/namety/edit/"+TOPIC.relationId)+"?action=rm"}">Smazat</a>
            </li>
        </#if>
    </ul>
    </@lib.showSignPost>
</#if>

<@lib.showMessages/>

<h1>${TOPIC.title}</h1>

<table>
    <tr>
        <td>Termín odevzdání</td>
        <td>
            <#if (TOPIC.deadline)??>
                <#assign deadlineStyle="">
                <#if TOPIC.delayed && (TOPIC.articleState == "NONE" || TOPIC.articleState == "DRAFT")>
                    <#assign deadlineStyle=" style=\"color: red\"">
                </#if>
                <span${deadlineStyle!}>${DATE.show(TOPIC.deadline, "CZ_DMY")}</span>
            </#if>            
        </td>
    </tr>
    <tr>
        <td>Honorář</td>
        <td>${(TOPIC.royalty)!"běžný"}</td>
    </tr>
    <tr>
        <td>Autor</td>
        <td>
            <#if (TOPIC.author)?? >
                <@lib.showAuthor TOPIC.author />
            <#else>
                námět je volný
            </#if>
        </td>
    </tr>
    <tr>
        <td>Asociovaný článek</td>
        <td>
            <#if TOPIC.article??>
                <a href="${URL.getRelationUrl(TOPIC.article)}">${TOOL.childName(TOPIC.article)}</a>
            <#else>
                žádný
            </#if>
        </td>
    </tr>
    <tr>
        <td>Popis</td>
        <td>${TOPIC.description}</td>
    </tr>
</table>

<#include "../../footer.ftl">