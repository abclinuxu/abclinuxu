<#include "../header.ftl">

<@lib.showMessages/>

<p>Chyst�te se zalo�it si sv�j osobn� blog. Blogy na na�em
port�le slou�� nap��klad pro komentov�n� ud�lost� kolem Linuxu
nebo publikov�n� r�zn�ch post�eh�, tip� �i n�vod�. Blog
m��e b�t u�ite�n� i pro za��te�n�ky, kte�� si zde mohou
zapisovat sv� pokroky s Linuxem. Pokud budou pot�ebovat
radu z diskusn�ho f�ra, je snadn� uv�st odkaz na blog,
tak�e diskutuj�c� budou zn�t kontext a l�pe mohou poradit.
Nicm�n� blogy jsou otev�en� a pokud sv�mi p��sp�vky
nebudete poru�ovat z�kony �i normy slu�n�ho chov�n�,
m��ete publikovat na libovoln� t�ma.
</p>

<p>V t�to f�zi se chyst�te zalo�it si blog. Pot�ebujeme
od v�s v�d�t jeho n�zev, pod kter�m bude blog dostupn�.
Nap��klad pojmenujete-li si blog snehulak, jeho
adresa bude www.abclinuxu.cz/blog/snehulak. N�zev blogu
by m�l obsahovat pouze p�smena A-Z, ��slice a podtr��tko.
</p>

<form action="${URL.noPrefix("/EditUser")}" method="POST">
 <div>
  <span class="required">Jm�no blogu</span>
  <input type="text" name="blogName" value="${PARAMS.blogName?if_exists}" size="16" tabindex="1" class="pole">
  <input type="submit" value="Dokon�i" tabindex="2" class="buton">
  <input type="hidden" name="action" value="addBlog2">
  <div class="error">${ERRORS.blogName?if_exists}</div>
 </div>
</form>


<#include "../footer.ftl">
