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

<p>
Abychom usnadnili nejrůznějším zájmovým skupinám v linuxové komunitě
spolupráci, komunikaci a rozvoj dalších aktivit, nabízíme funkci
skupin. Zde si mohou uživatelé do určité míry vytvořit vlastní
malý portál, vydávat články, vést diskuze atd.
</p>

<p>
Zároveň chceme zamezit vzniku spousty nepoužívaných a zbytečných portálů,
nelze si jej tedy nechat vytvořit automaticky. Pokud máte o subportál zájem,
kontaktuje administrátory, kteří věc zváží.
</p>

<hr />

<#list CHILDREN as relation>
    <#assign cat=relation.child,
        icon=TOOL.xpath(cat,"/data/icon")?default("UNDEF"),
        url=relation.url,
        desc=TOOL.xpath(cat,"/data/description")?default("UNDEF"),
        members = cat.getProperty("member")>
    <#if icon!="UNDEF"><div style="float: left; padding: 5px"><img src="${icon}" alt="${cat.title}" /></div></#if>

    <h2 class="st_nadpis"><a href="${url}">${cat.title}</a></h2>
    <p>${desc}</p>
    <p class="meta-vypis"><a href="${url}?action=members">Členů</a>: ${members?size} | Vznik: ${DATE.show(cat.created,"CZ_SHORT")}</p>
    <hr />
</#list>

<@lib.advertisement id="hosting90" />

<#include "../footer.ftl">
