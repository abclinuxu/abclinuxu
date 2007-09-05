<#include "../header.ftl">

<h1>Přehled mých aktivit</h1>

<@lib.showMessages/>

<#if COUNTS?exists>
    <p>Moje:</p>

    <ul>
        <#if AUTHOR?exists>
            <li>
                <a href="${AUTHOR.url}">články</a>
                (${COUNTS.article})
            </li>
        </#if>
        <li>
            <a href="${URL.noPrefix("/History?type=news&amp;uid="+PROFILE.id)}">zprávičky</a>
            (${COUNTS.news})
        </li>
        <li>
            <a href="${URL.noPrefix("/History?type=wiki&amp;uid="+PROFILE.id)}">wiki záznamy</a>
            (${COUNTS.wiki})
        </li>
        <li>
            <a href="${URL.noPrefix("/History?type=questions&amp;uid="+PROFILE.id)}">dotazy v poradně</a>
            (${COUNTS.question})
        </li>
        <li>
            <a href="${URL.noPrefix("/History?type=comments&amp;uid="+PROFILE.id)}">komentáře</a>
            (${COUNTS.comment})
        </li>
        <#if BLOG?exists>
            <li>
                <a href="/blog/${BLOG.subType}">zápisky v blogu</a>
                (${COUNTS.story})
            </li>
        </#if>
    </ul>
</#if>

<#include "../footer.ftl">
