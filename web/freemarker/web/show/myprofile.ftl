<#include "../header.ftl">

<@lib.showMessages/>

<#if PARAMS.registrace?exists>
 <h1 class="st_nadpis">P�iv�t�n�</h1>

 <p>
 D�kujeme v�m za projevenou d�v�ru. V���me, �e budete spokojeni
 se v�emi slu�bami na�eho port�lu. Doporu�ujeme v�m proj�t si toto
 str�nku a nastavit si osobn� �daje, p�izp�sobit si va�i ve�ejnou
 osobn� str�nku ke sv�mu obrazu a nakonfigurovat tento ��et.
 P�ihla�ovac�ch �daj� jsme v�m zaslali na va�i emailovou adresu ${PROFILE.email}.
 </p>
</#if>

<h1 class="st_nadpis">Va�e soukrom� str�nka</h1>

<p>Nach�z�te se ve sv� soukrom� str�nce. Zde m��ete
m�nit nastaven� sv�ho ��tu, upravovat sv�j profil
�i p�ihl�sit se k zas�l�n� informac�. Z d�vodu va��
ochrany budete p�i zm�n� �daj� vyzv�n� k zad�n� hesla.
V� profil, jak jej vid� ostatn� n�v�t�vn�ci, zobraz�te
na <a href="/Profile/${PROFILE.id}">t�to</a> str�nce.
</p>

<h2>Z�kladn� �daje</h2>

<p>Mezi z�kladn� �daje pat�� va�e jm�no (${PROFILE.name}),
p�ihla�ovac� jm�no (${PROFILE.login}),
p�ezd�vka (${PROFILE.nick?default("nen� nastavena")}),
email (${PROFILE.email}) a heslo.
</p>

<#if TOOL.xpath(PROFILE,"/data/communication/email[@valid='no']")?exists>
 <p class="error">Administr�to�i ozna�ili v� email za neplatn�!
 Stane se tak tehdy, pokud se n�kter� odeslan� email vr�t� jako
 trvale nedoru�iteln�. Dokud si nezm�n�te adresu, ��dn� dal�� email v�m
 nebude zasl�n.</p>
</#if>

<ul>
    <li>
        <a href="${URL.noPrefix("/EditUser/"+PROFILE.id+"?action=editBasic")}">
        zm�nit z�kladn� �daje</a>
    <li>
        <a href="${URL.noPrefix("/EditUser/"+PROFILE.id+"?action=changePassword")}">
        zm�nit heslo</a>
    <li>
        <a href="${URL.noPrefix("/Profile/"+PROFILE.id+"?action=forgottenPassword")}">
        poslat heslo emailem</a>
</ul>

<h2>Profil</h2>

<p>Port�l www.abclinuxu.cz v�m umo��uje bohat� nastaven� va�� osobn� str�nky,
kter� slou�� pro va�i prezentaci. M��ete zadat �irokou paletu strukturovan�ch
informac�, nap��klad bydli�t�, rok narozen�, adresu va�ich webov�ch str�nek,
pou��van� distribuce, nebo jak dlouho pou��v�te Linux. D�le si m��ete vytvo�it
pati�ku zobrazovanou v diskus�ch u va�ich p��sp�vk�, nahr�t svou fotku
�i upravit profil.
</p>

<ul>
    <li>
        <a href="${URL.noPrefix("/EditUser/"+PROFILE.id+"?action=editPersonal")}">
        zm�nit osobn� �daje</a>
    <li>
        <a href="${URL.noPrefix("/EditUser/"+PROFILE.id+"?action=editProfile")}">
        upravit profil</a>
    <li>
        <a href="${URL.noPrefix("/EditUser/"+PROFILE.id+"?action=uploadPhoto")}">
        zm�nit fotku</a>
</ul>

<h2>Blog</h2>

<p>�havou novinkou Ab��ka jsou blogy. Pro ty z v�s, kte�� tento term�n vid�te poprv�:
jedn� se o modern� formu veden� den��ku na internetu. Pokud za��n�te s Linuxem, m��ete
zde postupn� ps�t sv� zku�enosti a pokroky, v kombinaci s diskusn�m f�rem pak sn�ze
dos�hnete sv�ho c�le. Pokud jste pokro�il� u�ivatel, m�te mo�nost pou��t blogy pro
zve�ej�ov�n� rad a n�vod�. A nebo si pi�te blog jen tak, pro radost.
</p>

<ul>
    <li>
        <#if TOOL.xpath(PROFILE, "//settings/blog")?exists>
            <#assign blog=TOOL.createCategory(TOOL.xpath(PROFILE, "//settings/blog"))>
            <a href="/blog/${blog.subType}">zobrazit blog</a>
        <#else>
            <a href="${URL.noPrefix("/blog/edit/"+PROFILE.id+"?action=addBlog")}">vytvo�it blog</a>
        </#if>
</ul>

<h2>Zas�l�n� informac�</h2>

<p>M�te r�di n� port�l, ale nem�te �as n�s nav�t�vovat denn�? Nastavte
si zas�l�n� M�s��n�ho zpravodaje a T�denn� souhrn �l�nk�. D�le zde m��ete
zapnout zas�l�n� dotaz� a koment��� z diskusn�ho f�ra, tak�e v�m ��dn� odpov��
neute�e.
</p>

<ul><li>
<a href="${URL.noPrefix("/EditUser/"+PROFILE.id+"?action=subscribe")}">
objednat/odhl�sit</a>
</ul>

<h2>Nastaven� ��tu</h2>

<p>V t�to ��sti si m��ete zm�nit nastaven� va�eho ��tu. De facto
se jedn� o personalizaci port�lu, kde m��ete upravit jeho chov�n�
dle sv�ch p�edstav.</p>

<ul>
    <li>
        <a href="${URL.noPrefix("/EditUser/"+PROFILE.id+"?action=editSettings")}">
        zm�nit nastaven�</a>
</ul>

<h2>Va�e ve�ejn� str�nka</h2>

<p><a href="${URL.noPrefix("/Profile/"+PROFILE.id)}">Zp�tky</a>
na svou ve�ejnou domovskou str�nku</p>

<#include "../footer.ftl">
