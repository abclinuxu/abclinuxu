<#include "../header.ftl">

<div class="no-col-ad">
    <@lib.advertisement id="hypertext2nahore" />
    <@lib.advertisement id="square" />
    <@lib.advertisement id="hypertext2dole" />
</div>


<h1>Poradna</h1>

<p>Poradna slouží jako místo, kde mohou nováčci i zkušení uživatelé Linuxu vzájemně komunikovat a pomáhat si. Pro každou oblast jsme vytvořili jedno fórum, abyste snáze nalezli hledané informace.</p>

<p>Chcete-li se na něco zeptat, nejdříve si ale fórum prohlédněte, zda už se někdo před vámi na totéž neptal. Prvním krokem k vyřešení problému je hledání v naší obrovské databázi. Teprve když neuspějete, položte nový dotaz.</p>

<ul>
    <li>
        <form action="/hledani" method="get">
         <div>
          <input type="text" class="text" name="dotaz">
          <input type="hidden" name="typ" value="poradna">
          <input class="button" type="submit" value="prohledej poradnu">
         </div>
        </form>
    </li>
    <li><a href="/History?from=0&amp;count=25&amp;orderBy=update&amp;orderDir=desc&amp;type=discussions">seznam živých diskusí</a></li>
    <li><a href="nntp://news.gmane.org/gmane.user-groups.linux.czech">news rozhraní k diskusnímu fóru</a></li>
    <li>zasílání příspěvků e-mailem 
        <#if USER??>
            si můžete zapnout ve <a href="/Profile/${USER.id}?action=myPage">svém profilu</a>
        <#else>
            je dostupné registrovaným uživatelům (<a href="${URL.noPrefix("/EditUser?action=register")}">registrace</a>)
        </#if>
    </li>
</ul>

<#list VARS.mainForums.entrySet() as rid>
    <#if rid.key gt 0>
        <#assign forum=TOOL.createRelation(rid.key)>
        <h2 class="st_nadpis"><a href="${forum.url}">${TOOL.childName(forum)}</a></h2>
        <p>
            ${TOOL.xpath(forum.child, "//note")!}
        </p>
    </#if>
</#list>

<#assign questions=TOOL.analyzeDiscussions(VARS.getFreshQuestions(USER))>
<#if questions??>
	<h2>Přehled aktuálních diskusí</h2>

	<table class="ds">
	<thead>
	  <tr>
		<td class="td-nazev">Dotaz</td>
		<td class="td-meta">Stav</td>
		<td class="td-meta">Reakcí</td>
		<td class="td-datum">Poslední</td>
	  </tr>
	</thead>
	<tbody>
	 <#list questions as diz>
	  <tr>
		<td><a href="${diz.url?default("/forum/show/"+diz.relationId)}">${TOOL.limit(diz.title,60,"...")}</a></td>
		<td class="td-meta"><@lib.showDiscussionState diz /></td>
		<td class="td-meta">${diz.responseCount}</td>
		<td class="td-datum">${DATE.show(diz.updated,"CZ_SHORT")}</td>
	  </tr>
	 </#list>
	</tbody>
	</table>

	<ul>
		<li><a href="/History?type=discussions&amp;from=${questions?size}&amp;count=20">Starší diskuse</a></li>
	</ul>
</#if>

<#include "../footer.ftl">
