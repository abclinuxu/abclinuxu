<#assign plovouci_sloupec>
  <div class="s_nadpis">
    <a class="info" href="#">?<span class="tooltip">Vlastní blog si po přihlášení můžete založit v nastavení svého profilu.</span></a>
    <a href="/blog">Blogy na AbcLinuxu</a>
  </div>

    <div class="s_sekce">
        <#if DIGEST??>
            Výběr zápisků, které se týkají Linuxu, Open Source či IT. Žádná politika.
        <#else>
            Přehled zápisů ze všech blogů našich uživatelů. Blog si může založit registrovaný uživatel
            ve svém profilu.
        </#if>
        <ul>
            <#if DIGEST??>
                <li>
                    <a href="/blog">Všechny zápisky</a>
                </li>
            <#else>
                <li>
                    <#if SUMMARY??>
                        <a href="/blog">Výpis s perexy</a>
                    <#else>
                        <a href="/blog/souhrn">Stručnější souhrn</a>
                    </#if>
                </li>
                <li>
                    <a href="/blog/vyber">Výběr z blogů</a>
                </li>
            </#if>
            <li><a href="/blogy">Seznam blogů</a></li>
        </ul>
    </div>

    <div class="s_nadpis"><a href="/nej">Nej blogů na AbcLinuxu</a></div>
    <div class="s_sekce">
        <#if VARS.recentMostReadStories??>
            <b>Nejčtenější za poslední měsíc</b>
            <ul>
                <#list VARS.recentMostReadStories.entrySet() as rel>
                    <#if rel_index gt 2><#break></#if>
                    <li><a href="${rel.key.url}">${TOOL.childName(rel.key)}</a></li>
                </#list>
            </ul>
        </#if>
        <@lib.advertisement id="arbo-sq" />

        <#if VARS.recentMostCommentedStories??>
            <b>Nejkomentovanější za poslední měsíc</b>
            <ul>
                <#list VARS.recentMostCommentedStories.entrySet() as rel>
                    <#if rel_index gt 2><#break></#if>
                    <li><a href="${rel.key.url}">${TOOL.childName(rel.key)}</a></li>
                </#list>
            </ul>
        </#if>
    </div>

    <@lib.advertisement id="gg-sq-blog" />

</#assign>

<#include "../header.ftl">

<#if STORIES.total==0>
    <p>Vašemu výběru neodpovídá žádný zápis.</p>
</#if>

<#list STORIES.data as blogStory>
    <@lib.showStoryInListing blogStory, false, SUMMARY!false />
    <hr />
</#list>

<p>
    <#if SUMMARY??>
        <#assign url="/blog/souhrn">
    <#elseif DIGEST??>
        <#assign url="/blog/vyber">
    <#else>
        <#assign url="/blog/">
        <#if YEAR??><#assign url=url+YEAR+"/"></#if>
        <#if MONTH??><#assign url=url+MONTH+"/"></#if>
        <#if DAY??><#assign url=url+DAY+"/"></#if>
    </#if>
    <#if (STORIES.currentPage.row > 0) >
        <#assign start=STORIES.currentPage.row-STORIES.pageSize><#if (start<0)><#assign start=0></#if>
        <a href="${url}?from=${start}">Novější zápisy</a> &#8226;
    </#if>
    <#assign start=STORIES.currentPage.row + STORIES.pageSize>
    <#if (start < STORIES.total) >
        <a href="${url}?from=${start}">Starší zápisy</a>
    </#if>
</p>

<#include "../footer.ftl">
