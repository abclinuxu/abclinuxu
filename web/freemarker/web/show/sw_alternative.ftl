<#assign plovouci_sloupec>
    <div class="s_sekce">
        <ul>
            <li><a href="/software/alternativy">Alternativy k aplikacím</a></li>
        </ul>
    </div>
</#assign>

<#include "../header.ftl">

<div class="no-col-ad">
    <@lib.advertisement id="hypertext2nahore" />
    <@lib.advertisement id="square" />
    <@lib.advertisement id="hypertext2dole" />
</div>


<h1>Alternativy pro „${SOFTWARE}“</h1>

<div class="sw">

<#if ITEMS??>
    <@lib.showSoftwareList ITEMS />
<#else>
    <p>Litujeme, ale pro tento software nejsou v systému definovány žádné alternativy.</p>
</#if>

</div>


<#include "../footer.ftl">
