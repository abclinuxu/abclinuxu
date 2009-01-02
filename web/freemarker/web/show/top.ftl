<#include "../header.ftl">

<#macro displayTable data comment>
    <table border="1" style="border-collapse:collapse; font-size:small; width:100%;" class="reverse_anchor">
        <#list data.entrySet() as topitem>
            <tr>
                <td style="overflow:hidden; width:auto">
                    <a href="${topitem.key.url!}" style="display:block">${TOOL.childName(topitem.key)}</a>
                </td>
                <td style="width:20%" align="right">
                    ${topitem.value}${comment}
                </td>
            </tr>
        </#list>
    </table>
</#macro>

<@lib.showMessages/>

<h1>Nej portálu</h1>

<p>
    Na této stránce najdete tabulky, které ukazují nejčtenější a nejkomentovanější obsah tohoto portálu.
    Údaje jsou aktualizovány každou noc.
</p>

<h2>Články</h2>

<table border="0" style="font-size:small; width:99%;">
    <tr>
        <td style="width:45%; vertical-align:top;">
            <#if VARS.mostReadArticles!?has_content>
                <h3>Nejčtenější články</h3>
                <@displayTable VARS.mostReadArticles, "&times;"/>
            </#if>
        </td>
        <td style="width:45%; padding-left:1em;">
            <#if VARS.mostCommentedArticles!?has_content>
                <h3>Nejkomentovanější články</h3>
                <@displayTable VARS.mostCommentedArticles, ""/>
            </#if>
        </td>
    </tr>
</table>

<table border="0" style="font-size:small; width:99%;">
    <tr>
        <td style="width:45%; vertical-align:top;">
            <#if VARS.recentMostReadArticles!?has_content>
                <h3>Nejčtenější články posledního měsíce</h3>
                <@displayTable VARS.recentMostReadArticles, "&times;"/>
            </#if>
        </td>
        <td style="width:45%; padding-left:1em;">
            <#if VARS.recentMostCommentedArticles!?has_content>
                <h3>Nejkomentovanější články posledního měsíce</h3>
                <@displayTable VARS.recentMostCommentedArticles, ""/>
            </#if>
        </td>
    </tr>
</table>

<br /><hr />

<h2>Blogy</h2>

<table border="0" style="font-size:small; width:99%;">
    <tr>
        <td style="width:45%; vertical-align:top;">
            <#if VARS.mostReadStories!?has_content>
                <h3>Nejčtenější blogové zápisky</h3>
                <@displayTable VARS.mostReadStories, "&times;"/>
            </#if>
        </td>
        <td style="width:45%; padding-left:1em;">
            <#if VARS.mostCommentedStories!?has_content>
                <h3>Nejkomentovanější blogové zápisky</h3>
                <@displayTable VARS.mostCommentedStories, ""/>
            </#if>
        </td>
    </tr>
</table>

<table border="0" style="font-size:small; width:99%;">
    <tr>
        <td style="width:45%; vertical-align:top;">
            <#if VARS.recentMostReadStories!?has_content>
                <h3>Nejčtenější blogové zápisky posledního měsíce</h3>
                <@displayTable VARS.recentMostReadStories, "&times;"/>
            </#if>
        </td>
        <td style="width:45%; padding-left:1em;">
            <#if VARS.recentMostCommentedStories!?has_content>
                <h3>Nejkomentovanější blogové zápisky posledního měsíce</h3>
                <@displayTable VARS.recentMostCommentedStories, ""/>
            </#if>
        </td>
    </tr>
</table>

<br /><hr />

<h2>Zprávičky</h2>

<table border="0" style="font-size:small; width:99%;">
    <tr>
        <td style="width:45%; vertical-align:top;">
            <#if VARS.mostCommentedNews!?has_content>
                <h3>Nejkomentovanější zprávičky</h3>
                <@displayTable VARS.mostCommentedNews, ""/>
            </#if>
        </td>
        <td style="width:45%; padding-left:1em;">
            <#if VARS.recentMostCommentedNews!?has_content>
                <h3>Nejkomentovanější zprávičky posledního měsíce</h3>
                <@displayTable VARS.recentMostCommentedNews, ""/>
            </#if>
        </td>
    </tr>
</table>

<br /><hr />

<h2>Ankety</h2>

<table border="0" style="font-size:small; width:99%;">
    <tr>
        <td style="width:45%; vertical-align:top;">
            <#if VARS.mostVotedOnPolls!?has_content>
                <h3>Ankety s nejvíce hlasy</h3>
                <@displayTable VARS.mostVotedOnPolls, ""/>
            </#if>
        </td>
        <td style="width:45%; padding-left:1em;">
            <#if VARS.mostCommentedPolls!?has_content>
                <h3>Nejkomentovanější ankety</h3>
                <@displayTable VARS.mostCommentedPolls, ""/>
            </#if>
        </td>
    </tr>
</table>

<br /><hr />

<#if VARS.highestScoreUsers!?has_content>
    <h2>Skóre uživatelů</h2>

    <table border="1" style="border-collapse:collapse; font-size:small; width:100%;" class="reverse_anchor">

        <#list VARS.highestScoreUsers.entrySet() as topitem>
        <tr>
            <td style="overflow:hidden; width:auto"><a href="/lide/${topitem.key.login}" style="display:block">${topitem.key.nick!topitem.key.name}</a></td>
            <td style="width:20%" align="right">${topitem.value}</td>
        </tr>
        </#list>

    </table>
</#if>

<#include "../footer.ftl">
