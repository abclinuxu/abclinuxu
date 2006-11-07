<html>
<head>
    <title>${TITLE}</title>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-2">
    <#if ! css?exists || css=="UNDEF"><#assign css="/styles.css"></#if>
    <link rel="stylesheet" type="text/css" href="${css}">
    <script type="text/javascript" src="/data/site/scripts.js"></script>
</head>
<body style="background:#fff; margin:5px">
<#import "macros.ftl" as lib>
<p>Portál <a href="http://www.abclinuxu.cz">AbcLinuxu</a><#if USER?exists>,
přihlášen ${USER.name}</#if>, ${DATE.show("CZ_FULL_TXT")}
</p>
<!-- obsah -->

<#if SYSTEM_CONFIG.isMaintainanceMode()>
    <div style="color: red; border: medium solid red; margin: 30px; padding: 3ex">
        <p style="font-size: xx-large; text-align: center">Režim údržby</p>
        <p>
            Právě provádíme údržbu portálu. Prohlížení obsahu by mělo nadále fungovat,
            úpravy obsahu bohužel nejsou prozatím k dispozici. Děkujeme za pochopení.
        </p>
    </div>
</#if>
