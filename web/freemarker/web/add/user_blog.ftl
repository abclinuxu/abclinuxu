<#include "../header.ftl">

<@lib.showMessages/>

<p>Chystáte se zalo¾it si svùj osobní blog. Blogy na na¹em
portále slou¾í napøíklad pro komentování událostí kolem Linuxu
nebo publikování rùzných postøehù, tipù èi návodù. Blog
mù¾e být u¾iteèný i pro zaèáteèníky, kteøí si zde mohou
zapisovat své pokroky s Linuxem. Pokud budou potøebovat
radu z diskusního fóra, je snadné uvést odkaz na blog,
tak¾e diskutující budou znát kontext a lépe mohou poradit.
Nicménì blogy jsou otevøené a pokud svými pøíspìvky
nebudete poru¹ovat zákony èi normy slu¹ného chování,
mù¾ete publikovat na libovolné téma.
</p>

<p>V této fázi se chystáte zalo¾it si blog. Potøebujeme
od vás vìdìt jeho název, pod kterým bude blog dostupný.
Napøíklad pojmenujete-li si blog snehulak, jeho
adresa bude www.abclinuxu.cz/blog/snehulak. Název blogu
by mìl obsahovat pouze písmena A-Z, èíslice a podtr¾ítko.
</p>

<form action="${URL.noPrefix("/EditUser")}" method="POST">
 <div>
  <span class="required">Jméno blogu</span>
  <input type="text" name="blogName" value="${PARAMS.blogName?if_exists}" size="16" tabindex="1" class="pole">
  <input type="submit" value="Dokonèi" tabindex="2" class="buton">
  <input type="hidden" name="action" value="addBlog2">
  <div class="error">${ERRORS.blogName?if_exists}</div>
 </div>
</form>


<#include "../footer.ftl">
