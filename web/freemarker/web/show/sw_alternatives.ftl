<#include "../header.ftl">

<h1>Softwarové alternativy</h1>

<p>
    Pokud přecházíte z prostředí Windows, určitě vás zajímá,
    jaké aplikace můžete v Linuxu použít místo těch, na které
    jste zvyklí. Na této stránce najdete seznam aplikací
    z Windows, ke kterým jsou přiřazeny linuxové alternativy.
</p>

<ul>
    <#list ALTERNATIVES as alternative>
        <li>
            <a href="/software/alternativy/${alternative?url}">${alternative}</a>
        </li>
    </#list>
</ul>

<#include "../footer.ftl">
