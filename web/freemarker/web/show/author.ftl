<#include "../header.ftl">

<@lib.showMessages/>

<#assign name = AUTHOR.name!"UNDEFINED",
         surname = AUTHOR.surname!"UNDEFINED",
         nickname = AUTHOR.nickname!"UNDEFINED",
         uid = AUTHOR.uid!0>

<@lib.advertisement id="arbo-sq" />

<h1>
    Autor
    <#if name!="UNDEFINED">${name}</#if>
    <#if surname!="UNDEFINED">${surname}</#if>
    <#if nickname!="UNDEFINED">(${nickname})</#if>
</h1>

<p>
<#if uid != 0>
    <#assign uauthor=TOOL.createUser(uid)>
    <a href="/lide/${uauthor.login}" title="${name!""} ${surname!""}">Profil autora na abclinuxu</a>
</#if>
</p>

<#if USER?? && TOOL.permissionsFor(USER, RELATION).canModify()>
    <p>
    <a href="${URL.noPrefix("/autori/edit?rid="+RELATION.id+"&amp;action=edit")}">Upravit</a>

    <#if TOOL.permissionsFor(USER, RELATION).canDelete()>
        <a href="${URL.noPrefix("/EditRelation?action=remove&amp;rid="+RELATION.id+"&amp;prefix=/autori")}">Smazat</a>
    </#if>
    <a href="${URL.noPrefix("/clanky/honorare?authorId="+RELATION.id)}">Honoráře</a>
    </p>
    <table border="0" cellpadding="2" cellspacing="0">
        <#if AUTHOR.birthNumber??>
            <tr>
                <td>Rodné číslo:</td><td>${AUTHOR.birthNumber}</td>
            </tr>
        </#if>
        <#if AUTHOR.accountNumber??>
            <tr>
                 <td>Číslo účtu:</td><td>${AUTHOR.accountNumber}</td>
            </tr>
        </#if>
        <#if AUTHOR.email??>
            <tr>
                 <td>Email:</td><td>${AUTHOR.email}</td>
            </tr>
        </#if>
        <#if AUTHOR.phone??>
            <tr>
                 <td>Telefon:</td><td>${AUTHOR.phone}</td>
            </tr>
        </#if>
        <#if AUTHOR.address??>
            <tr>
                 <td>Adresa:</td><td>${AUTHOR.address}</td>
            </tr>
        </#if>
    </table>
</#if>

<table class="autor">
  <thead>
    <tr>
      <td class="td-nazev">Článek</td>
      <td class="td-datum">Datum vydání</td>
      <td class="td-meta">Přečteno</td>
      <td class="td-meta">Komentářů</td>
      <td class="td-meta">Hodnocení</td>
      <td class="td-meta">Hlasů</td>
    </tr>
  </thead>
  <tbody>
    <#list ARTICLES.data as relation>
      <#assign clanek=relation.child, tmp=TOOL.groupByType(clanek.children, "Item"),
               url=relation.url!("/clanky/show/"+relation.id),
               rating=TOOL.ratingFor(clanek.data)!"UNDEF">
      <tr>
        <td><a href="${url}">${clanek.title}</a></td>
        <td class="td-datum">${DATE.show(clanek.created, "SMART_DMY")}</td>
        <td class="td-meta td-right"><@lib.showCounter clanek, "read" />&times;</td>
        <td class="td-meta td-right">
          <#if tmp.discussion??>
              <#assign diz=TOOL.analyzeDiscussion(tmp.discussion[0])>
              <#if (diz.responseCount > 0)>
                  <a href="${diz.url!("/clanky/show/"+diz.relationId)}">${diz.responseCount}<@lib.markNewComments diz/></a>
              </#if>
          </#if>
        </td>
        <td class="td-meta"><#if rating!="UNDEF">${rating.percent} %</#if></td>
        <td class="td-meta td-right"><#if rating!="UNDEF">${rating.count}</#if></td>
      </tr>
    </#list>
  </tbody>
</table>

<p>
<#if (ARTICLES.currentPage.row > 0) >
<#assign start=ARTICLES.currentPage.row-ARTICLES.pageSize><#if (start<0)><#assign start=0></#if>
    <a href="${RELATION.url}?from=${start}&amp;count=${ARTICLES.pageSize}">Novější články</a> &#8226;
</#if>
<#assign start=ARTICLES.currentPage.row + ARTICLES.pageSize>
<#if (start < ARTICLES.total) >
    <a href="${RELATION.url}?from=${start}&amp;count=${ARTICLES.pageSize}">Starší články</a>
</#if>
</p>

<@lib.showPageTools RELATION />

<#include "../footer.ftl">
