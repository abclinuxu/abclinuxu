<#include "../header.ftl">

<h1>�asto kladen� ot�zky</h1>

<p>�asto kladen� ot�zky (anglicky FAQ) jsou kolekc� p�edem
zodpov�zen�ch ot�zek, na kter� se �ten��i �asto ptaj�
v diskusn�m f�ru. Pokud �e��te ur�it� probl�m nebo se za��n�te
seznamovat s Linuxem, m�li byste za��t na t�to str�nce a prostudovat
pe�liv� jednotliv� ot�zky. V�hodou oproti f�ru je p�ehlednost a
(v�t�inou i) �plnost odpov�di. Jeliko� jde o spole�nou
pr�ci, kter�koliv �ten�� sm� vylep�it �i up�esnit odpov��,
d�ky �emu� nar�st� kvalita zodpov�zen�ch ot�zek.
</p>

<h2>Sekce</h2>

<p>Pro usnadn�n� orientace jsou zodpov�zen� ot�zky �azeny
do sekc�, kter� pokr�vaj� jedno t�ma. Chcete-li p�idat novou
zodpov�zenou ot�zku a c�t�te-li, �e se nehod� do ��dn� sekce,
po��dejte administr�tory o vytvo�en� nov� sekce. Netu��te-li,
ve kter� sekci hledat, projd�te si <a href="/History?type=faq">historii</a>,
kde jsou v�echny ot�zky �azeny nez�visle na sekci podle datumu
posledn� zm�ny.
</p>

<table border="0">
    <tr>
        <th>Sekce</th>
        <th>Po�et</th>
    </tr>
    <#list VARS.faqTree.children as faq>
        <tr>
            <td>
                <a href="${faq.url}">${faq.name}</a>
                <#if faq.description?exists>
                    <br>${faq.description}
                </#if>
            </td>
            <td align="right">${faq.size}</td>
        </tr>
    </#list>
</table>

<#include "../footer.ftl">
