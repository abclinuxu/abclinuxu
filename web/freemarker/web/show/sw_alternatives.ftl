<#include "../header.ftl">

<h1>Softwarové alternativy</h1>

<p>
    Pokud pøecházíte z prostøedí Windows, urèitì vás zajímá,
    jaké aplikace mù¾ete v Linuxu pou¾ít místo tìch, na které
    jste zvyklí. Na této stránce najdete seznam aplikací
    z Windows, ke kterým jsou pøiøazeny linuxové alternativy.
</p>

<ul>
    <#list ALTERNATIVES as alternative>
        <li>
            <a href="/software/alternativy/${alternative?url}">${alternative}</a>
        </li>
    </#list>
</ul>

<#include "../footer.ftl">
