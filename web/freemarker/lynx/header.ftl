<html>
 <head>
  <title>${TITLE}</title>
  <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-2">
  <meta name="author" content="Leos Literak">
  <meta name="keywords" content="linux,abclinuxu,hardware,USB,SCSI,modem,kernel,ovlada�">
 </head>
<body>

<#include "/include/pocitani.txt">

<p>
 <b>AbcLinuxu s.r.o.</b>
 <a href="http://firma.abclinuxu.cz/">Profil</a>,
 <a href="http://firma.abclinuxu.cz/novinky.html">Novinky</a>,
 <a href="http://firma.abclinuxu.cz/produkty.html">Produkty</a>,
 <a href="http://firma.abclinuxu.cz/sluzby.html">Slu�by</a>,
 <a href="http://www.abclinuxu.cz/palirna/index.html">Shop</a>,
 <a href="http://firma.abclinuxu.cz/kontakt.html">Kontakt</a>
</p>

<p>
${DATE.show("CZ_FULL")}
 <#if USER?exists>
  U�ivatel: ${USER.name}
  <a href="${URL.noPrefix("/Profile/"+USER.id)}">M�j profil</a>,
  <a href="${URL.noPrefix("/Index?logout=true")}">Odhl�en�</a>
 <#else>
  <a href="${URL.noPrefix("/Profile?action=login")}">P�ihl�en�</a>,
  <a href="${URL.noPrefix("/EditUser?action=register")}">Registrace</a>
 </#if>
</p>

<p>
 <b>Rubriky</b>
  <#if USER?exists && USER.hasRole("article admin")>
   <a href="/clanky/dir/5">Aktu�ln�</a>,
  </#if>
  <a href="/clanky/dir/4731">Ab��ko</a>,
  <a href="/clanky/dir/4">Jadern� noviny</a>,
  <a href="/clanky/dir/3">N�vody</a>,
  <a href="/clanky/dir/251">Novinky</a>,
  <a href="/clanky/dir/14358">Programov�n�</a>,
  <a href="/clanky/dir/2">Recenze</a>,
  <a href="/clanky/dir/5324">R�zn�</a>,
  <a href="/clanky/dir/8546">T�ma</a>,
  <a href="/clanky/dir/12448">Tutori�ly</a>,
  <a href="/clanky/dir/6">PR �l�nky</a>
</p>

<p>
 <form action="/Search" method="post">
  <b>Vyhled�v�n�</b>
  <input type="text" name="query" size="14">
  <input type="submit" value="Hledej">
  <a href="${URL.make("/clanky/ViewRelation?rid"+5024)}">N�pov�da</a>
 </form>
</p>

<#flush>

<hr width="100%">

<!-- obsah zacina zde -->
