<#include "../header.ftl">

<@lib.showMessages/>

<p>Blogy jsou moderní formou vedení deníèku na internetu.
Ve svém blogu mù¾ete psát rady a návody, nebo pokroky va¹eho
Open Source projektu, komentovat dìní na linuxové scénì èi
jako zaèáteèník popisovat své pokroky s Linuxem. Nicménì blogy
jsou otevøené a pokud svými pøíspìvky nebudete poru¹ovat zákony
èi normy slu¹ného chování, mù¾ete psát na libovolné téma.
</p>

<p>Blogy na abclinuxu jsou slu¾bou jeho ètenáøùm. Pokud nejste
ètenáø abclinuxu a s Linuxem nemáte nic spoleèného, jen hledáte
prostor na publikování, radìji si zvolte jiný veøejný blogovací
systém.</p>

<p>Prvním krokem je zalo¾ení blogu. Musíte zvolit
jméno blogu. Toto jméno bude pevnou a nedílnou
souèástí URL va¹eho blogu, proto mù¾e obsahovat
jen písmena anglické abecedy, èíslice a podtr¾ítko
(navíc první písmeno nesmí být èíslice). Na velikosti písmen
nezále¾í. Napøíklad pojmenujete-li si blog snehulak, jeho
adresa bude www.abclinuxu.cz/blog/snehulak. 
</p>

<p>Dále mù¾ete vytvoøit a¾ tøi kategorie,
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
