<#include "../../header.ftl">

<@lib.showSignPost "Rozcestník">
<ul>
   <li><a href="${URL.make("/redakce/autori/edit?action=add")}">Přidat autora</a></li>
</ul>
</@lib.showSignPost>

<@lib.showMessages/>
<h3>Správa autorů</h3>
<p>Na této stránce můžete prohlížet a spravovat autory.</p>
<p>Authory lze filtrovat pomocí sloupců <b>Jméno</b> a <b>Příjmení</b>, kde stačí zadat začátek jména.
Dále podle toho, zda uživatel souhlasil s autorskou smlouvou a zda odsouhlasená smlouva není stará.
Sloupec <b>Aktivní</b> poté ukazuje, zda autor autor nadále tvoří obsah abclinuxu.cz. 
Datum posledního článku je zobrazen ve sloupci <b>Poslední</b> a toto stáří lze zde filtrovat. 
</p> 
<br/>

<table class="siroka list">
	<thead>
	<tr>
		<th>Jméno</th>
		<th>Příjmení</th>
		<th>Smlouva</th>
		<th>Aktivní</th>
		<th>Článků</th>
		<th>Poslední</th>
		<th>Akce</th>
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
			<@lib.showOption5 "5", "&lt;5", FILTER.checked("filterAuthorsByArticles", "5")/>
			<@lib.showOption5 "10", "&lt;10", FILTER.checked("filterAuthorsByArticles", "10")/>
			<@lib.showOption5 "50", "&lt;50", FILTER.checked("filterAuthorsByArticles", "50")/>
			<@lib.showOption5 "100", "&lt;100", FILTER.checked("filterAuthorsByArticles", "100")/>
			<@lib.showOption5 "101", "&gt;100", FILTER.checked("filterAuthorsByArticles", "101")/>
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

	<#list FOUND.data as author>
		<tr>
			<td>${(author.name)!?html}</td>
			<td>
			<a href="${URL.make("/redakce/autori/show/?aId=${author.id}&amp;action=show")}">${(author.surname)!?html}</a></td>
			<td>smlouva</td>
			<td>${(author.active)!?string("ano","ne")}</td>
			<td>${(author.articleCount)!}</td>
			<#assign date=""/>
			<#if author.lastArticleDate??>
				<#assign date=DATE.show(author.lastArticleDate, "CZ_DMY") />
			</#if>
			<td>${date}</td>
			<td style="white-space: nowrap">
			<#if author.email??><a href="mailto:${(author.email)!?html}" title="Poslat email">@</a>
			<#else>@</#if>&nbsp;
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
