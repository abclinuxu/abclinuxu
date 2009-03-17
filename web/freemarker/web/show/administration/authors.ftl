<#include "../../header.ftl">

<@lib.showMessages/>
<h3>Správa autorů</h3>
<p>
Na této stránce můžete prohlížet a spravovat autory. Sloupeček Smlouva ukazuje, zda uživatel souhlasil s autorskou
smlouvou a zda není stará. Sloupeček Souhlas indikuje souhlas se zpracováním osobních údajů.
</p> 

<table>
	<tr>
		<th>Jméno</th>
		<th>Příjmení</th>
		<th>Smlouva</th>
		<th>Aktivní</th>
		<th>Článků</th>
		<th>Poslední</th>
		<th>Akce</th>
	</tr>
	<tr>	
	<form action="" method="post">
		<td><input type="text" name="filterAuthorsByName" value="${FILTER.value("filterAuthorsByName")}" /></td>
		<td><input type="text" name="filterAuthorsBySurname" value="${FILTER.value("filterAuthorsBySurname")}" /></td>
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
			<td>${author.name}</td>
			<td>${author.surname}</td>
			<td>smlouva</td>
			<td>${author.active?string("ano","ne")}</td>
			<td>${author.articleCount}</td>
			<td>${(author.lastArticleDate?date)!}</td>
			<td style="white-space:nowrap">
			<#if author.email??><a href="mailto:${author.email}">email</a>
			<#else>email
			</#if>&nbsp;
			<a href="${URL.make("redakce/autori?edit")}">upravit</a>&nbsp;
			<a href="${URL.make("autori/namety")}">náměty</a>&nbsp;
			<a href="${URL.make("autori/clanky")}">články</a>&nbsp;
			<a href="${URL.make("autori/honorare")}">honoráře</a>&nbsp;
			<a href="${URL.make("autori/smlouvy")}">smlouvy</a>&nbsp;
			<a href="${URL.make("redakce/autori?delete")}">smazat</a>&nbsp;
			</td>
		</tr>
	</#list>
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
