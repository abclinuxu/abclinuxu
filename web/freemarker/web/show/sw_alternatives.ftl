<#include "../header.ftl">

<div class="no-col-ad">
    <@lib.advertisement id="hypertext2nahore" />
    <@lib.advertisement id="square" />
    <@lib.advertisement id="hypertext2dole" />
</div>


<h1>Softwarové alternativy</h1>

<p>Pokud přecházíte z prostředí Windows, určitě vás zajímá, které aplikace můžete v Linuxu použít místo těch, na něž jste zvyklí. Na této stránce najdete seznam aplikací z Windows, ke kterým jsou přiřazeny linuxové alternativy.</p>

<ul>
    <#list ALTERNATIVES as alternative>
        <li><a href="/software/alternativy/${alternative?url}">${alternative}</a></li>
    </#list>
</ul>

<#include "../footer.ftl">
