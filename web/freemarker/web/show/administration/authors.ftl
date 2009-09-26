<#include "../../header.ftl">

<@lib.showSignPost "Rozcestník">
<ul>
   <li><a href="${URL.make("/redakce/autori/edit?action=add")}">Přidat autora</a></li>
</ul>
</@lib.showSignPost>

<@lib.showMessages/>

<h3>Správa autorů</h3>

<p>Na této stránce můžete prohlížet a spravovat autory. Autory lze filtrovat podle následujících kritérií:</p>

<ul style="list-style: none">
	<li>Jméno a příjmení - stačí zadat začátek jména nebo příjmení</li>
	<li>Smlouva - souhlas autora s autorskou smlouvou a její aktuálnost</li>
	<li>Aktivní - indikace, zda autor stále tvoří obsah abclinuxu.cz</li>
	<li>Článků - počet článku napsaný autorem, inkluzivní interval</li>
	<li>Poslední - stáří posledního článku<li>
</ul>

<form action="${URL.noPrefix("/sprava/redakce/autori")}" method="POST">
    <table class="siroka list">
	    <thead>
            <tr>
                <th>Jméno a Příjmení</th>
                <th>Smlouva</th>
                <th>Aktivní</th>
                <th>Článků</th>
                <th>Poslední</th>
                <th>&nbsp;</th>
            </tr>
        </thead>
	    <tbody>
            <tr>
                <td>
                    <@lib.filterInput filter=FILTER name="filterAuthorsByName" size="8" />
                    <@lib.filterInput filter=FILTER name="filterAuthorsBySurname" size="8" />
                </td>
                <td>
                    <select name="filterAuthorsByContract">
                        <@lib.filterOption filter=FILTER name="filterAuthorsByContract" value=""></@lib.filterOption>
                        <@lib.filterOption filter=FILTER name="filterAuthorsByContract" value="old">stará</@lib.filterOption>
                        <@lib.filterOption filter=FILTER name="filterAuthorsByContract" value="current">aktuální</@lib.filterOption>
                        <@lib.filterOption filter=FILTER name="filterAuthorsByContract" value="none">žádná</@lib.filterOption>
                    </select>
                </td>
                <td>
                    <select name="filterAuthorsByActive">
                    	<@lib.filterOption filter=FILTER name="filterAuthorsByActive" value=""></@lib.filterOption>
                    	<@lib.filterOption filter=FILTER name="filterAuthorsByActive" value="1">ano</@lib.filterOption>
                    	<@lib.filterOption filter=FILTER name="filterAuthorsByActive" value="0">ne</@lib.filterOption>
                    </select>
                </td>
                <td>
                    <select name="filterAuthorsByArticles">
                    	<@lib.filterOption filter=FILTER name="filterAuthorsByArticles" value=""></@lib.filterOption>
                    	<@lib.filterOption filter=FILTER name="filterAuthorsByArticles" value="0">žádný</@lib.filterOption>
                    	<@lib.filterOption filter=FILTER name="filterAuthorsByArticles" value="1-4">1 až 4</@lib.filterOption>
                    	<@lib.filterOption filter=FILTER name="filterAuthorsByArticles" value="5-9">5 až 9</@lib.filterOption>
                    	<@lib.filterOption filter=FILTER name="filterAuthorsByArticles" value="10-49">10 až 49</@lib.filterOption>
                    	<@lib.filterOption filter=FILTER name="filterAuthorsByArticles" value="50-99">50 až 99</@lib.filterOption>
                    	<@lib.filterOption filter=FILTER name="filterAuthorsByArticles" value="100">100 a více</@lib.filterOption>
                    </select>
                </td>
                <td>
                    <select name="filterAuthorsByRecent">
                    	<@lib.filterOption filter=FILTER name="filterAuthorsByRecent" value=""></@lib.filterOption>
                    	<@lib.filterOption filter=FILTER name="filterAuthorsByRecent" value="25">starší než dva roky</@lib.filterOption>
                    	<@lib.filterOption filter=FILTER name="filterAuthorsByRecent" value="24">nejvíce dva roky</@lib.filterOption>
                    	<@lib.filterOption filter=FILTER name="filterAuthorsByRecent" value="12">nejvíce rok</@lib.filterOption>
                    	<@lib.filterOption filter=FILTER name="filterAuthorsByRecent" value="6">nejvíce půl roku</@lib.filterOption>
                    	<@lib.filterOption filter=FILTER name="filterAuthorsByRecent" value="2">minulý měsíc</@lib.filterOption>
                    	<@lib.filterOption filter=FILTER name="filterAuthorsByRecent" value="1">poslední měsíc</@lib.filterOption>
                    </select>
                </td>
                <td><input type="submit" value="Filtruj" /></td>
            </tr>
            <#list FOUND.data as author>
                <tr>
                    <td style="text-align: left">
                        <a href="${URL.make("/redakce/autori/show/?aId=${author.id}&amp;action=show")}">${(author.title)!?html}</a>
                    </td>
                    <td>&nbsp;</td>
                    <td>${(author.active)!?string("ano","ne")}</td>
                    <td>${(author.articleCount)!}</td>
                    <td>
                        <#if author.lastArticleDate??>
                            ${DATE.show(author.lastArticleDate, "CZ_DMY")}
                        </#if>
                    </td>
                    <td style="white-space: nowrap">
                        <#if author.email??><a href="mailto:${(author.email)!?html}" title="Poslat email">@</a>&nbsp;</#if>
                        <a href="${URL.make("/redakce/autori/edit/${author.id}?action=edit")}" title="Upravit autora">U</a>&nbsp;
                        <a href="${URL.make("/autori/namety")}" title="Náměty">N</a>&nbsp;
                        <a href="${URL.make("/autori/clanky")}" title="Články">Č</a>&nbsp;
                        <a href="${URL.make("/autori/honorare")}" title="Honoráře">H</a>&nbsp;
                        <a href="${URL.make("/autori/smlouvy")}" title="Smlouvy">S</a>&nbsp;
                        <a href="${URL.make("/redakce/autori/edit/${author.id}?action=rm")}" title="Smazat autora">X</a>
                    </td>
                </tr>
            </#list>
	    </tbody>
    </table>
</form>

<div>
<#if FOUND.prevPage??>
    <a href="${URL_BEFORE_FROM}0${URL_AFTER_FROM}">0</a>
    &nbsp;<a href="${URL_BEFORE_FROM}${FOUND.prevPage.row}${URL_AFTER_FROM}">&lt;&lt;</a>
<#else>
    0 &lt;&lt;
</#if>
${FOUND.thisPage.row} - ${FOUND.thisPage.row+FOUND.thisPage.size}&nbsp;
<#if FOUND.nextPage??>
    <a href="${URL_BEFORE_FROM}${FOUND.nextPage.row?string["#"]}${URL_AFTER_FROM}">&gt;&gt;</a>
    &nbsp;<a href="${URL_BEFORE_FROM}${(FOUND.total - FOUND.pageSize)?string["#"]}${URL_AFTER_FROM}">${FOUND.total}</a>
<#else>
    &gt;&gt; ${FOUND.total}
</#if>
</div>

<#include "../../footer.ftl">