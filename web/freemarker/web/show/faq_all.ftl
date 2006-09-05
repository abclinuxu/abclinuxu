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

<#assign FAQS = VARS.faqTree>
<table border="0" class="siroka">
    <tr>
        <td>
            <a href="/faq/aplikace">Aplikace</a> (${FAQS.getByRelation(117404).size})
        </td>
        <td>
            <a href="/faq/bezpecnost">Bezpe�nost</a> (${FAQS.getByRelation(105223).size})
        </td>
        <td>
            <a href="/faq/boot">Boot</a> (${FAQS.getByRelation(94492).size})
        </td>
    </tr>
    <tr>
        <td>
            <a href="/faq/digitalni-foto">Digit�ln� foto</a> (${FAQS.getByRelation(94486).size})
        </td>
        <td>
            <a href="/faq/disky">Disky</a> (${FAQS.getByRelation(94480).size})
        </td>
        <td>
            <a href="/faq/distribuce">Distribuce</a> (${FAQS.getByRelation(94496).size})
        </td>
    </tr>
    <tr>
        <td>
            <a href="/faq/grafika">Grafika</a> (${FAQS.getByRelation(94478).size})
        </td>
        <td>
            <a href="/faq/instalace">Instalace</a> (${FAQS.getByRelation(94502).size})
        </td>
        <td>
            <a href="/faq/kernel">Kernel</a> (${FAQS.getByRelation(94493).size})
        </td>
    </tr>
    <tr>
        <td>
            <a href="/faq/klavesnice">Kl�vesnice</a> (${FAQS.getByRelation(94483).size})
        </td>
        <td>
            <a href="/faq/multimedia">Multim�dia</a> (${FAQS.getByRelation(94494).size})
        </td>
        <td>
            <a href="/faq/mysi">My�i</a> (${FAQS.getByRelation(94484).size})
        </td>
    </tr>
    <tr>
        <td>
            <a href="/faq/hardware">Ostatn� hardware</a> (${FAQS.getByRelation(94485).size})
        </td>
        <td>
            <a href="/faq/prava">Pr�va</a> (${FAQS.getByRelation(94490).size})
        </td>
        <td>
            <a href="/faq/site">S�t�</a> (${FAQS.getByRelation(94479).size})
        </td>
    </tr>
    <tr>
        <td>
            <a href="/faq/skripty">Skripty</a> (${FAQS.getByRelation(95259).size})
        </td>
        <td>
            <a href="/faq/souborove-systemy">Souborov� syst�my</a> (${FAQS.getByRelation(94481).size})
        </td>
        <td>
            <a href="/faq/tisk">Tisk</a> (${FAQS.getByRelation(94488).size})
        </td>
    </tr>
    <tr>
        <td>
            <a href="/faq/vypalovani">Vypalov�n�</a> (${FAQS.getByRelation(94491).size})
        </td>
        <td>
            <a href="/faq/web">Web</a> (${FAQS.getByRelation(94495).size})
        </td>
        <td>
            <a href="/faq/zalohovani">Z�lohov�n�</a> (${FAQS.getByRelation(94482).size})
        </td>
    </tr>
    <tr>
        <td colspan="3">
            <a href="/faq/zvuk">Zvuk</a> (${FAQS.getByRelation(94489).size})
        </td>
    </tr>
</table>

<#include "../footer.ftl">
