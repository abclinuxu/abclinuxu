<#include "../header.ftl">

<@lib.showMessages/>

<p>Blogy jsou moderní formou vedení deníèku na internetu.
Na Abíèku budou va¹e pøíspìvky ihned dostipné desítkám tisícùm
ètenáøù. Ve svém blogu mù¾ete zapisovat rady a návody,
nebo pokroky va¹eho Open Source projektu, komentovat
dìní na linuxové scénì èi jako zaèáteèník popisovat své
pokroky s Linuxem (v kombinaci s diskusním fórem díky
kontextu snáze dosáhnete odpovìdi na své dotazy).
Nicménì blogy jsou otevøené a pokud svými pøíspìvky
nebudete poru¹ovat zákony èi normy slu¹ného chování,
mù¾ete psát na libovolné téma.
</p>

<p>Prvním krokem je zalo¾ení blogu. Potøebujeme
od vás vìdìt jeho název, pod kterým bude blog dostupný.
Napøíklad pojmenujete-li si blog snehulak, jeho
adresa bude www.abclinuxu.cz/blog/snehulak. Název blogu
by mìl obsahovat pouze písmena A-Z, èíslice a podtr¾ítko.
</p>

<p>Volitelnì mù¾ete u¾ teï vytvoøit a¾ tøi kategorie,
do kterých budete zaøazovat své pøíspìvky. Napøíklad
<i>Linux</i>, <i>©kola</i>, <i>Básnièky</i> èi <i>GNU</i>.
</p>

<br>

<form action="${URL.noPrefix("/blog/edit/"+USER.id)}" method="POST">
 <table border="0">
    <tr>
        <td class="required">Jméno blogu</td>
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
        <td><input type="submit" value="Dokonèi" tabindex="5" class="buton"></td>
    </tr>
 </table>
 <input type="hidden" name="action" value="addBlog2">
</form>


<#include "../footer.ftl">
