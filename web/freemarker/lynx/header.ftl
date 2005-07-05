<html>
 <head>
  <title>${TITLE}</title>
  <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-2">
  <meta name="author" content="Leos Literak">
  <meta name="keywords" content="linux,abclinuxu,hardware,USB,SCSI,modem,kernel,ovlada�">
  <link rel="bookmark" href="#obsah" title="Obsah str�nky" type="text/html">
 </head>
<body>

<#import "macros.ftl" as lib>
<#include "/include/pocitani2.txt">
<#include "/include/pocitani1.txt">

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
 <form action="/Search" method="post">
  <b>Vyhled�v�n�</b>
  <input type="text" name="query" size="14">
  <input type="submit" value="Hledej">
  <a href="${URL.make("/clanky/ViewRelation?rid"+5024)}">N�pov�da</a>
 </form>
</p>

<#flush>

<hr width="100%">

<!-- obsah zacina zde --><a name="obsah"></a>
