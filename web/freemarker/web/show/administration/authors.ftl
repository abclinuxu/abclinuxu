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
<table class="siroka list">
	<thead>
	<tr>
		<th>Jméno</th>
		<th>Příjmení</th>
		<th>Smlouva</th>
		<th>Aktivní</th>
		<th>Článků</th>
		<th>Poslední</th>
		<th></th>
	</tr>
	</thead>
	<tbody>
	<tr>	
	<form action="${URL.noPrefix("/sprava/redakce/autori")}" method="POST">
		<td><input type="text" name="filterAuthorsByName" value="${FILTER.value("filterAuthorsByName")}" size="8"/></td>
		<td><input type="text" name="filterAuthorsBySurname" value="${FILTER.value("filterAuthorsBySurname")}" size="8"/></td>
		<td><select name="filterAuthorsByContract">
			<@lib.showOption5 "", "", FILTER.checked("filterAuthorsByContract", "")/>
			<@lib.showOption5 "old", "stará", FILTER.checked("filterAuthorsByContract", "old")/>
			<@lib.showOption5 "current", "aktualní", FILTER.checked("filterAuthorsByContract", "current")/>
			<@lib.showOption5 "none", "žádná", FILTER.checked("filterAuthorsByContract", "none")/>
			</select></td>
		<td><select name="filterAuthorsByActive">
			<@lib.showOption5 "", "", FILTER.checked("filterAuthorsByActive", "")/>
			<@lib.showOption5 "1", "ano", FILTER.checked("filterAuthorsByActive", "1")/>
			<@lib.showOption5 "0", "ne", FILTER.checked("filterAuthorsByActive", "0")/>
			</select></td>
		<td><select name="filterAuthorsByArticles">
			<@lib.showOption5 "", "", FILTER.checked("filterAuthorsByArticles", "")/>
			<@lib.showOption5 "0", "žádný", FILTER.checked("filterAuthorsByArticles", "0")/>
			<@lib.showOption5 "1-4", "1 až 4", FILTER.checked("filterAuthorsByArticles", "1-4")/>
			<@lib.showOption5 "5-9", "5 až 9", FILTER.checked("filterAuthorsByArticles", "5-9")/>
			<@lib.showOption5 "10-49", "10 až 49", FILTER.checked("filterAuthorsByArticles", "10-49")/>
			<@lib.showOption5 "50-99", "50 až 99", FILTER.checked("filterAuthorsByArticles", "50-99")/>
			<@lib.showOption5 "101", "100 a více", FILTER.checked("filterAuthorsByArticles", "101")/>
			</select></td>
		<td><select name="filterAuthorsByRecent">
			<@lib.showOption5 "", "", FILTER.checked("filterAuthorsByRecent", "")/>
			<@lib.showOption5 "25", "starší než dva roky", FILTER.checked("filterAuthorsByRecent", "25")/>
			<@lib.showOption5 "24", "nejvíce dva roky", FILTER.checked("filterAuthorsByRecent", "24")/>
			<@lib.showOption5 "12", "nejvíce rok", FILTER.checked("filterAuthorsByRecent", "12")/>
			<@lib.showOption5 "6", "nejvíce půl roku", FILTER.checked("filterAuthorsByRecent", "6")/>
			<@lib.showOption5 "2", "minulý měsíc", FILTER.checked("filterAuthorsByRecent", "2")/>
			<@lib.showOption5 "1", "poslední měsíc", FILTER.checked("filterAuthorsByRecent", "1")/>
			</select></td>			
		<td><input type="submit" value="Filtruj" /></td>
		</form>
	</tr>
	</tbody>
</table>
<table class="siroka list">
	<thead>
	<tr>
		<th>Jméno a příjmení </th>		
		<th>Smlouva</th>
		<th>Aktivní</th>
		<th>Článků</th>
		<th>Poslední</th>
		<th>Akce</th>
	</tr>
	</thead>
	<tbody>	
	<#list FOUND.data as author>
		<tr>
			<td style="text-align: left"><a href="${URL.make("/redakce/autori/show/?aId=${author.id}&amp;action=show")}">${(author.title)!?html}</a></td>			
			<td></td>
			<td>${(author.active)!?string("ano","ne")}</td>
			<td>${(author.articleCount)!}</td>
			<#assign date=""/>
			<#if author.lastArticleDate??>
				<#assign date=DATE.show(author.lastArticleDate, "CZ_DMY") />
			</#if>
			<td>${date}</td>
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