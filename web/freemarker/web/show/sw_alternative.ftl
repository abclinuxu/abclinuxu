<#assign plovouci_sloupec>
    <div class="s_sekce">
        <ul>
            <li><a href="/software/alternativy">Alternativy k aplikacím</a></li>
        </ul>
    </div>
</#assign>

<#include "../header.ftl">

<@lib.advertisement id="square" />

<h1>Alternativy pro ${SOFTWARE}</h1>

<div class="sw">

<#if ITEMS??>
    <@lib.showSoftwareList ITEMS />
<#else>
    <p>
        Litujeme, ale pro tento software nejsou v systému definovány žádné alternativy.
    </p>
</#if>

</div>


<#include "../footer.ftl">
