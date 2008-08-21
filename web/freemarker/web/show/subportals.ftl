<#import "../macros.ftl" as lib>

<#if USER?exists && USER.hasRole("root")>
    <#assign plovouci_sloupec>
    <div class="s_sekce">
    <ul>
        <li><a href="/skupiny/edit?action=add">Vytvořit nový</a></li>
    </ul>
    </div>
    </#assign>
</#if>

<#include "../header.ftl">

<h1>Seznam skupin</h1>

<p>Abychom usnadnili nejrůznějším zájmovým skupinám v linuxové komunitě
spolupráci, komunikaci a rozvoj dalších aktivit, nabízíme funkci
skupin. Zde si mohou uživatelé do určité míry vytvořit vlastní
malý portál, vydávat články, vést diskuze atd.</p>

<p>Zároveň chceme zamezit vzniku spousty nepoužívaných a zbytečných portálů,
nelze si jej tedy nechat vytvořit automaticky. Pokud máte o subportál zájem,
kontaktuje administrátory, kteří věc zváží.</p>

<hr />

<#list SUBPORTALS.data as relation>
    <#assign cat=relation.child,
        icon=TOOL.xpath(cat,"/data/icon")?default("UNDEF"),
        url=relation.url,
        desc=TOOL.xpath(cat,"/data/descriptionShort")?default("UNDEF"),
        members = cat.getProperty("member"),
        score=cat.getIntProperty("score")?default(-1)>
    <#if icon!="UNDEF"><div style="float:right; padding: 5px"><img src="${icon}" alt="${cat.title}" /></div></#if>

    <h2 class="st_nadpis"><a href="${url}">${cat.title}</a></h2>
    <p>${desc}</p>
    <p class="meta-vypis"><a href="${url}?action=members">Členů</a>: ${members?size} | Vznik: ${DATE.show(cat.created,"CZ_SHORT")}
    <#if score != -1>| Skóre: ${score}</#if></p>
    <hr style="clear:right" />
</#list>

<ul>
    <#if (SUBPORTALS.currentPage.row > 0) >
        <#assign start=SUBPORTALS.currentPage.row-SUBPORTALS.pageSize><#if (start<0)><#assign start=0></#if>
        <li>
            <a href="/bazar?from=${start}&amp;count=${SUBPORTALS.pageSize}">Předchozí skupiny</a>
        </li>
    </#if>
    <#assign start=SUBPORTALS.currentPage.row + SUBPORTALS.pageSize>
    <#if (start < SUBPORTALS.total) >
        <li>
            <a href="/bazar?from=${start}&amp;count=${SUBPORTALS.pageSize}">Další skupiny</a>
        </li>
    </#if>
</ul>

<#include "../footer.ftl">
