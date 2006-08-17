<#assign plovouci_sloupec>
    <div class="s_sekce">
        <ul>
            <li><a href="/software/alternativy">Alternativy k aplikacím</a></li>
        </ul>
    </div>
</#assign>

<#include "../header.ftl">

<h1>Alternativy pro ${SOFTWARE}</h1>

<div class="sw">

<#if ITEMS?exists>
    <table class="sw-polozky">
      <thead>
        <tr>
            <td class="td01">Jméno</td>
            <td class="td04">Poslední úprava</td>
        </tr>
      </thead>
      <tbody>
        <#list SORT.byName(ITEMS) as polozka>
            <tr>
                <td class="td01">
                    <a href="${URL.make(polozka.url?default("/show/"+polozka.id))}">${TOOL.childName(polozka)}</a>
                </td>
                <td class="td04">${DATE.show(polozka.child.updated,"CZ_FULL")}</td>
            </tr>
        </#list>
      </tbody>
    </table>
<#else>
    <p>
        Litujeme, ale pro tento software nejsou v systému definovány ¾ádné alternativy.
    </p>
</#if>

</div>


<#include "../footer.ftl">
