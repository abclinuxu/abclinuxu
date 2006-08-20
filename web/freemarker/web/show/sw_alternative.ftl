<#assign plovouci_sloupec>
    <div class="s_sekce">
        <ul>
            <li><a href="/software/alternativy">Alternativy k aplikac�m</a></li>
        </ul>
    </div>
</#assign>

<#include "../header.ftl">

<h1>Alternativy pro ${SOFTWARE}</h1>

<div class="sw">

<#if ITEMS?exists>
    <#list SORT.byName(ITEMS) as software>
        <@lib.showSoftwareInList software />
    </#list>
<#else>
    <p>
        Litujeme, ale pro tento software nejsou v syst�mu definov�ny ��dn� alternativy.
    </p>
</#if>

</div>


<#include "../footer.ftl">
