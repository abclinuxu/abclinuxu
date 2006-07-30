<#include "../header.ftl">

<@lib.showMessages/>

<h2>�vod</h2>

<p>Tato str�nka slou�� ke zm�n� jm�na blogu. Jedn� se
o docela delik�tn� operaci, zvl�t� pokud je v� blog ji�
ve�ejn� zn�m. T�mto �konem se toti� zm�n� URL, p�es kter�
je v� blog p��stupn�. Odkazy, kter� maj� va�i �ten��i
�i zn�m� ve sv�ch z�lo�k�ch, se tedy stanou neplatn�mi.
Pokud jste si skute�n� jisti, zm�nit jm�no m��ete n�e.</p>

<p>Jm�no m��e obsahovat jen p�smena anglick� abecedy, ��slice
a podtr��tko (nav�c prvn� p�smeno nesm� b�t ��slice). Na velikosti p�smen
nez�le��. Nap��klad pojmenujete-li si blog snehulak, jeho
adresa bude www.abclinuxu.cz/blog/snehulak. Jm�no
blogu m��ete pozd�ji zm�nit, p�estane-li v�m vyhovovat.</p>

<form action="${URL.make("/blog/edit/"+REL_BLOG.id)}" method="POST">
    <p>
        <span class="required">Jm�no</span>
        <input type="text" name="blogName" size="40" maxlength="70" value="${PARAMS.blogName?if_exists?html}">
        <input type="submit" name="finish" value="Dokon�i">
    </p>
    <div class="error">${ERRORS.blogName?if_exists}</div>
    <input type="hidden" name="action" value="rename2">
</form>

<#include "../footer.ftl">
