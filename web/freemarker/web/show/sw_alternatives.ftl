<#include "../header.ftl">

<h1>Softwarov� alternativy</h1>

<p>
    Pokud p�ech�z�te z prost�ed� Windows, ur�it� v�s zaj�m�,
    jak� aplikace m��ete v Linuxu pou��t m�sto t�ch, na kter�
    jste zvykl�. Na t�to str�nce najdete seznam aplikac�
    z Windows, ke kter�m jsou p�i�azeny linuxov� alternativy.
</p>

<ul>
    <#list ALTERNATIVES as alternative>
        <li>
            <a href="/software/alternativy/${alternative?url}">${alternative}</a>
        </li>
    </#list>
</ul>

<#include "../footer.ftl">
