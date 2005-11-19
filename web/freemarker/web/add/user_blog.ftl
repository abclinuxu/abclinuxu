<#include "../header.ftl">

<@lib.showMessages/>

<p>Blogy jsou modern� formou veden� den��ku na internetu.
Ve sv�m blogu m��ete ps�t rady a n�vody, nebo pokroky va�eho
Open Source projektu, komentovat d�n� na linuxov� sc�n� �i
jako za��te�n�k popisovat sv� pokroky s Linuxem. Nicm�n� blogy
jsou otev�en� a pokud sv�mi p��sp�vky nebudete poru�ovat z�kony
�i normy slu�n�ho chov�n�, m��ete ps�t na libovoln� t�ma.
</p>

<p>Blogy na abclinuxu jsou slu�bou jeho �ten���m. Pokud nejste
�ten�� abclinuxu a s Linuxem nem�te nic spole�n�ho, jen hled�te
prostor na publikov�n�, rad�ji si zvolte jin� ve�ejn� blogovac�
syst�m.</p>

<p>Prvn�m krokem je zalo�en� blogu. Mus�te zvolit
jm�no blogu. Toto jm�no bude pevnou a ned�lnou
sou��st� URL va�eho blogu, proto m��e obsahovat
jen p�smena anglick� abecedy, ��slice a podtr��tko
(nav�c prvn� p�smeno nesm� b�t ��slice). Na velikosti p�smen
nez�le��. Nap��klad pojmenujete-li si blog snehulak, jeho
adresa bude www.abclinuxu.cz/blog/snehulak. 
</p>

<p>D�le m��ete vytvo�it a� t�i kategorie,
do kter�ch budete za�azovat sv� p��sp�vky. Nap��klad
<i>Linux</i>, <i>�kola</i>, <i>B�sni�ky</i> �i <i>GNU</i>.
</p>

<br>

<form action="${URL.noPrefix("/blog/edit/"+USER.id)}" method="POST">
 <table border="0">
    <tr>
        <td class="required">Jm�no blogu</td>
        <td>
            <input type="text" name="blogName" value="${PARAMS.blogName?if_exists}" size="32" tabindex="1" class="pole">
            <div class="error">${ERRORS.blogName?if_exists}</div>
        </td>
    </tr>
    <tr>
        <td>Kategorie</td>
        <td>
            <input type="text" name="category1" value="${PARAMS.category1?if_exists}" size="32" tabindex="2" class="pole"><br>
            <input type="text" name="category2" value="${PARAMS.category2?if_exists}" size="32" tabindex="3" class="pole"><br>
            <input type="text" name="category3" value="${PARAMS.category3?if_exists}" size="32" tabindex="4" class="pole"><br>
        </td>
    </tr>
    <tr>
        <td>&nbsp;</td>
        <td><input type="submit" value="Dokon�i" tabindex="5" class="buton"></td>
    </tr>
 </table>
 <input type="hidden" name="action" value="addBlog2">
</form>


<#include "../footer.ftl">
