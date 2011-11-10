<html>
 <head>
  <title>${TITLE}</title>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
     <#if CANONICAL_URL??><link rel=”canonical” href="${CANONICAL_URL}" ></#if>
  <meta name="robots" content="noindex,nofollow" />
  <meta name="viewport" content="width=device-width" />
  <link rel="apple-touch-icon" href="http://www.abclinuxu.cz/images/site2/abc-logo.gif" />
  <link rel="apple-touch-icon-precomposed" href="http://www.abclinuxu.cz/images/site2/abc-logo.gif" />

    <#if html_header??>
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
