<#include "../header.ftl">
<#setting number_format="#,##0">

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

<p>Na této stránce najdete tabulky a žebříčky, které ukazují ten nej obsah tohoto portálu. Většina údajů je k dispozici buď za celou dobu, nebo jen dokumenty vytvořené za poslední měsíc. Údaje jsou aktualizovány každou noc.</p>


<h2>Články</h2>

<table border="0" style="font-size:small; width:99%;">
    <tr>
        <td style="width:45%; vertical-align:top;">
            <#if VARS.mostReadArticles!?has_content>
                <h3>Nejčtenější články</h3>
                <@displayTable VARS.mostReadArticles, "&times;"/>
            </#if>
        </td>
        <td style="width:45%; vertical-align:top; padding-left:1em;">
            <#if VARS.recentMostReadArticles!?has_content>
                <h3>Nejčtenější nové články</h3>
                <@displayTable VARS.recentMostReadArticles, "&times;"/>
            </#if>
        </td>
    </tr>
</table>

<table border="0" style="font-size:small; width:99%;">
    <tr>
        <td style="width:45%; vertical-align:top;">
            <#if VARS.mostCommentedArticles!?has_content>
                <h3>Nejkomentovanější články</h3>
                <@displayTable VARS.mostCommentedArticles, ""/>
            </#if>
        </td>
        <td style="width:45%; vertical-align:top; padding-left:1em;">
            <#if VARS.recentMostCommentedArticles!?has_content>
                <h3>Nejkomentovanější nové články</h3>
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
                <h3>Nejčtenější zápisky</h3>
                <@displayTable VARS.mostReadStories, "&times;"/>
            </#if>
        </td>
        <td style="width:45%; vertical-align:top; padding-left:1em;">
            <#if VARS.recentMostReadStories!?has_content>
                <h3>Nejčtenější nové zápisky</h3>
                <@displayTable VARS.recentMostReadStories, "&times;"/>
            </#if>
        </td>
    </tr>
</table>

<table border="0" style="font-size:small; width:99%;">
    <tr>
        <td style="width:45%; vertical-align:top;">
            <#if VARS.mostCommentedStories!?has_content>
                <h3>Nejkomentovanější zápisky</h3>
                <@displayTable VARS.mostCommentedStories, ""/>
            </#if>
        </td>
        <td style="width:45%; vertical-align:top; padding-left:1em;">
            <#if VARS.recentMostCommentedStories!?has_content>
                <h3>Nejkomentovanější nové zápisky</h3>
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
        <td style="width:45%; vertical-align:top; padding-left:1em;">
            <#if VARS.recentMostCommentedNews!?has_content>
                <h3>Nejkomentovanější nové zprávičky</h3>
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
        <td style="width:45%; vertical-align:top; padding-left:1em;">
            <#if VARS.mostCommentedPolls!?has_content>
                <h3>Nejkomentovanější ankety</h3>
                <@displayTable VARS.mostCommentedPolls, ""/>
            </#if>
        </td>
    </tr>
</table>

<br /><hr />

<h2>Desktopy</h2>

<table border="0" style="font-size:small; width:99%;">
    <tr>
        <td style="width:45%; vertical-align:top;">
            <#if VARS.mostSeenDesktops!?has_content>
                <h3>Nejpopulárnější desktopy</h3>
                <@displayTable VARS.mostPopularDesktops, "&times;"/>
            </#if>
        </td>
        <td style="width:45%; vertical-align:top; padding-left:1em;">
            <#if VARS.recentMostSeenDesktops!?has_content>
                <h3>Nejpopulárnější nové desktopy</h3>
                <@displayTable VARS.recentMostPopularDesktops, "&times;"/>
            </#if>
        </td>
    </tr>
</table>

<table border="0" style="font-size:small; width:99%;">
    <tr>
        <td style="width:45%; vertical-align:top;">
            <#if VARS.mostSeenDesktops!?has_content>
                <h3>Nejprohlíženější desktopy</h3>
                <@displayTable VARS.mostSeenDesktops, "&times;"/>
            </#if>
        </td>
        <td style="width:45%; vertical-align:top; padding-left:1em;">
            <#if VARS.recentMostSeenDesktops!?has_content>
                <h3>Nejprohlíženější nové desktopy</h3>
                <@displayTable VARS.recentMostSeenDesktops, "&times;"/>
            </#if>
        </td>
    </tr>
</table>

<table border="0" style="font-size:small; width:99%;">
    <tr>
        <td style="width:45%; vertical-align:top;">
            <#if VARS.mostCommentedDesktops!?has_content>
                <h3>Nejkomentovanější desktopy</h3>
                <@displayTable VARS.mostCommentedDesktops, ""/>
            </#if>
        </td>
        <td style="width:45%; vertical-align:top; padding-left:1em;">
            <#if VARS.recentMostCommentedDesktops!?has_content>
                <h3>Nejkomentovanější nové desktopy</h3>
                <@displayTable VARS.recentMostCommentedDesktops, ""/>
            </#if>
        </td>
    </tr>
</table>

<br /><hr />

<h2>Software</h2>

<table border="0" style="font-size:small; width:99%;">
    <tr>
        <td style="width:45%; vertical-align:top;">
            <#if VARS.mostPopularSoftware!?has_content>
                <h3>Nejpopulárnější aplikace</h3>
                <@displayTable VARS.mostPopularSoftware, "&times;"/>
            </#if>
        </td>
        <td style="width:45%; vertical-align:top; padding-left:1em;">
            <#if VARS.recentMostPopularSoftware!?has_content>
                <h3>Nejpopulárnější nové aplikace</h3>
                <@displayTable VARS.recentMostPopularSoftware, "&times;"/>
            </#if>
        </td>
    </tr>
</table>

<table border="0" style="font-size:small; width:99%;">
    <tr>
        <td style="width:45%; vertical-align:top;">
            <#if VARS.mostVisitedSoftware!?has_content>
                <h3>Nejnavštěvovanější aplikace</h3>
                <@displayTable VARS.mostVisitedSoftware, "&times;"/>
            </#if>
        </td>
        <td style="width:45%; vertical-align:top; padding-left:1em;">
            <#if VARS.recentMostVisitedSoftware!?has_content>
                <h3>Nejnavštěvovanější nové aplikace</h3>
                <@displayTable VARS.recentMostVisitedSoftware, "&times;"/>
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
