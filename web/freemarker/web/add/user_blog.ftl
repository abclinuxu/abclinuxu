<#include "../header.ftl">

<@lib.showMessages/>

<p>Blogy jsou moderní formou vedení deníčku na Internetu.
Ve svém blogu můžete psát rady a návody, seznamovat s pokroky vašeho
Open Source projektu, komentovat dění na linuxové scéně či
jako začátečník popisovat své první kroky s Linuxem. Nicméně blogy
jsou otevřené, a pokud svými příspěvky nebudete porušovat zákony
či normy slušného chování, můžete psát na libovolné téma.</p>

<p>Blogy na abclinuxu jsou službou jeho čtenářům. Pokud nejste
čtenářem AbcLinuxu.cz nebo s Linuxem nemáte nic společného a jen hledáte
prostor na publikování, raději si zvolte jiný veřejný blogovací
systém.</p>

<p>Mějte na paměti, že pod vaše zápisky v blogu může kdokoliv
vkládat komentáře. Na rozdíl od jiných blogovacích systémů není
možné u blogů na AbcLinuxu.cz moderovat diskuzi,
takže nad tím, kdo a jaké komentáře k vašemu zápisku vloží, nebudete
mít žádnou kontrolu.</p>

<p>Další vlastností zdejšího blogovacího systému je to, že
jakmile se pod vaším zápiskem objeví nějaký komentář, nebude
možné zápisek smazat, protože by tím byl smazán i daný komentář.
Svůj zápisek však samozřejmě budete moci kdykoliv a libovolně
editovat.</p>

<p>Prvním krokem je založení blogu. Musíte zvolit
jméno blogu. Toto jméno bude pevnou a nedílnou
součástí URL vašeho blogu, proto může obsahovat
jen písmena anglické abecedy, číslice a podtržítko
(navíc první písmeno nesmí být číslice). Na velikosti písmen
nezáleží. Například pojmenujete-li si blog "snehulak", jeho
adresa bude www.abclinuxu.cz/blog/snehulak.</p>

<p>Dále můžete vytvořit až tři kategorie,
do kterých budete zařazovat své příspěvky. Například
<i>Linux</i>, <i>Škola</i>, <i>Básničky</i> či <i>GNU</i>.</p>

<br />

<@lib.addForm URL.noPrefix("/blog/edit/"+USER.id)>
    <@lib.addInput true, "blogName", "Jméno blogu", 32 />
    <@lib.addFormField false, "Kategorie">
        <@lib.addInputBare "category1", 32 />
        <@lib.addInputBare "category2", 32 />
        <@lib.addInputBare "category3", 32 />
    </@lib.addFormField>
    <@lib.addSubmit "Dokonči" />
    <@lib.addHidden "action", "addBlog2" />
</@lib.addForm>


<#include "../footer.ftl">
