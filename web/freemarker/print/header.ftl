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
<p>Port�l <a href="http://www.abclinuxu.cz">AbcLinuxu</a><#if USER?exists>,
p�ihl�en ${USER.name}</#if>, ${DATE.show("CZ_FULL_TXT")}
</p>
<!-- obsah -->

<#if SYSTEM_CONFIG.isMaintainanceMode()>
    <div style="color: red; border: medium solid red; margin: 30px; padding: 3ex">
        <p style="font-size: xx-large; text-align: center">Re�im �dr�by</p>
        <p>
            Pr�v� prov�d�me �dr�bu port�lu. Prohl�en� obsahu by m�lo nad�le fungovat,
            �pravy obsahu bohu�el nejsou prozat�m k dispozici. D�kujeme za pochopen�.
        </p>
    </div>
</#if>
