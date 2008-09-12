<#include "../header.ftl">

<h1>Poradna</h1>

<p>Poradna slouží jako místo, kde mohou nováčci i zkušení
uživatelé Linuxu vzájemně komunikovat a pomáhat si. Pro každou oblast
jsme vytvořili jedno fórum, abyste snáze nalezli hledané informace.</p>

<p>Chcete-li se na něco zeptat, musíte si nejdříve zvolit diskusním fórum.
Fóra jsou logicky členěna do několika sekcí; rozmyslete se, kam asi
dotaz patří. Když otevřete fórum, najdete v něm odkaz na položení dotazu.
Nejdříve si ale fórum prohlédněte, zda už se někdo před vámi na totéž
neptal. Prvním krokem k vyřešení problému je hledání v naší obrovské databázi.
Teprve když neuspějete, položte nový dotaz.</p>

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
    <li>
        <a href="/History?from=0&amp;count=25&amp;orderBy=update&amp;orderDir=desc&amp;type=discussions">seznam živých diskusí</a>
    </li>
    <li>
        <a href="nntp://news.gmane.org/gmane.user-groups.linux.czech">news rozhraní k diskusnímu fóru</a>
    </li>
    <li>
        zasílání příspěvků emailem si můžete zapnout ve svém profilu
    </li>
</ul>

<#list VARS.mainForums.entrySet() as rid>
    <#assign forum=TOOL.createRelation(rid.key)>
    <h2 class="st_nadpis"><a href="${forum.url}">${TOOL.childName(forum)}</a></h2>
    <p>
        ${TOOL.xpath(forum.child, "//note")?if_exists}
    </p>
</#list>

<h2>Přehled aktuálních diskusí</h2>

<#assign questions=TOOL.analyzeDiscussions(VARS.getFreshQuestions(USER?if_exists))>

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

<#include "../footer.ftl">
