<html>
 <head>
  <title>${TITLE}</title>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <meta name="author" content="Leos Literak">
    <#if html_header?exists>
        ${html_header}
    </#if>
 </head>
<body>

<#import "macros.ftl" as lib>

<#if SYSTEM_CONFIG.isMaintainanceMode()>
    <div style="color: red; border: medium solid red; margin: 30px; padding: 3ex">
        <p style="font-size: xx-large; text-align: center">Režim údržby</p>
        <p>
            Právě provádíme údržbu portálu. Prohlížení obsahu by mělo nadále fungovat,
            úpravy obsahu bohužel nejsou prozatím k dispozici. Děkujeme za pochopení.
        </p>
    </div>
</#if>
