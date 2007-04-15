<html>
 <head>
  <title>${TITLE}</title>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <meta name="author" content="Leos Literak">
  <meta name="keywords" content="linux,abclinuxu,hardware,USB,SCSI,modem,kernel,ovladač">
  <link rel="bookmark" href="#obsah" title="Obsah stránky" type="text/html">
    <script type="text/javascript" src="/data/site/scripts.js"></script>
    <#if html_header?exists>
        ${html_header}
    </#if>
 </head>
<body>

<#import "macros.ftl" as lib>

<p>
${DATE.show("CZ_FULL")}
 <#if USER?exists>
  Uživatel: ${USER.name}
  <a href="${URL.noPrefix("/Profile/"+USER.id)}">Můj profil</a>,
  <a href="${URL.noPrefix("/Index?logout=true")}">Odhlášení</a>
 <#else>
  <a href="${URL.noPrefix("/Profile?action=login")}">Přihlášení</a>,
  <a href="${URL.noPrefix("/EditUser?action=register")}">Registrace</a>
 </#if>

 <form action="/hledani" method="post">
  <b>Hledávání</b>
  <input type="text" name="dotaz" size="14">
  <input type="submit" value="Hledej">
  <a href="/doc/napoveda/hledani">Nápověda</a>
 </form>
</p>

<hr width="100%">

<!-- obsah zacina zde --><a name="obsah"></a>

    <#if SYSTEM_CONFIG.isMaintainanceMode()>
        <div style="color: red; border: medium solid red; margin: 30px; padding: 3ex">
            <p style="font-size: xx-large; text-align: center">Režim údržby</p>
            <p>
                Právě provádíme údržbu portálu. Prohlížení obsahu by mělo nadále fungovat,
                úpravy obsahu bohužel nejsou prozatím k dispozici. Děkujeme za pochopení.
            </p>
        </div>
    </#if>
