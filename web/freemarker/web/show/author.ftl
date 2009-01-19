<#include "../header.ftl">

<@lib.showMessages/>

<#assign autor = RELATION.child, name = TOOL.xpath(autor, "/data/firstname")?default("UNDEFINED"),
         surname = TOOL.xpath(autor, "/data/surname")?default("UNDEFINED"),
         nickname = TOOL.xpath(autor, "/data/nickname")?default("UNDEFINED"),
         uid = autor.numeric1?default(0)>

<h1>
    Autor
    <#if name!="UNDEFINED">${name}</#if>
    <#if surname!="UNDEFINED">${surname}</#if>
    <#if nickname!="UNDEFINED">(${nickname})</#if>
</h1>

<p>
<#if uid != 0>
    <#assign uauthor=TOOL.createUser(uid)>
    <a href="/lide/${uauthor.login}" title="${name?default("")} ${surname?default("")}">Profil autora na abclinuxu</a>
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
        <#if TOOL.xpath(AUTHOR,"/data/birthNumber")??>
            <tr>
                <td>Rodné číslo:</td><td>${TOOL.xpath(AUTHOR,"/data/birthNumber")}</td>
            </tr>
        </#if>
        <#if TOOL.xpath(AUTHOR,"/data/accountNumber")??>
            <tr>
                 <td>Číslo účtu:</td><td>${TOOL.xpath(AUTHOR,"/data/accountNumber")}</td>
            </tr>
        </#if>
        <#if TOOL.xpath(AUTHOR,"/data/email")??>
            <tr>
                 <td>Email:</td><td>${TOOL.xpath(AUTHOR,"/data/email")}</td>
            </tr>
        </#if>
        <#if TOOL.xpath(AUTHOR,"/data/phone")??>
            <tr>
                 <td>Telefon:</td><td>${TOOL.xpath(AUTHOR,"/data/phone")}</td>
            </tr>
        </#if>
        <#if TOOL.xpath(AUTHOR,"/data/address")??>
            <tr>
                 <td>Adresa:</td><td>${TOOL.xpath(AUTHOR,"/data/address")}</td>
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
    <#global CITACE = TOOL.getRelationCountersValue(ARTICLES.data,"read")/>
    <#list ARTICLES.data as relation>
      <#assign clanek=relation.child, tmp=TOOL.groupByType(clanek.children, "Item"),
               url=relation.url?default("/clanky/show/"+relation.id),
               rating=TOOL.ratingFor(clanek.data)!"UNDEF">
      <tr>
        <td><a href="${url}">${clanek.title}</a></td>
        <td class="td-datum">${DATE.show(clanek.created, "SMART_DMY")}</td>
        <td class="td-meta td-right"><@lib.showCounter clanek, .globals["CITACE"]!, "read" />&times;</td>
        <td class="td-meta td-right">
          <#if tmp.discussion??>
              <#assign diz=TOOL.analyzeDiscussion(tmp.discussion[0])>
              <#if (diz.responseCount > 0)>
                  <a href="${diz.url?default("/clanky/show/"+diz.relationId)}">${diz.responseCount}<@lib.markNewComments diz/></a>
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

<#include "../footer.ftl">
