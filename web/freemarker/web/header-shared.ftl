<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0//EN" "http://www.w3.org/TR/REC-html40/strict.dtd" >
<html lang="cs">
<head>
    <#if PARAMS.debug??>
        <!-- Sablona: ${TEMPLATE!"neznama"} -->
    </#if>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="Robots" content="index,follow" />
    <title>${PARAMS.TITLE!TITLE!'www.abclinuxu.cz'}</title>
    <link rel="stylesheet" type="text/css" href="/jquery-theme/ui.all.css" />
    <link rel="stylesheet" type="text/css" href="${CSS_URI!}">
    <#if CANONICAL_URL??><link rel=”canonical” href="${CANONICAL_URL}" ></#if>
    <#if INLINE_CSS??>
        <style type="text/css">
            ${INLINE_CSS}
        </style>
    </#if>
    <!--[if IE]>
       <link href="/msie.css" type="text/css" rel="stylesheet">
    <![endif]-->
    <!--[if IE 7]>
       <link href="/msie7.css" type="text/css" rel="stylesheet">
    <![endif]-->
    <!--[if lt IE 7]>
       <link href="/msie6.css" type="text/css" rel="stylesheet">
    <![endif]-->
    <link rel="icon" href="/images/site2/favicon.png" type="image/png">
    <#if IS_INDEX??>
        <meta name="description" content="Komunitní portál: Linux, Open Source, BSD a jiné unixy. Nejvíce funkcí, nejaktivnější poradna, nejživější blogy. Pozor - návykové.">
        <link rel="alternate" title="abclinuxu.cz: články" href="http://www.abclinuxu.cz/auto/abc.rss" type="application/rss+xml">
        <link rel="alternate" title="abclinuxu.cz: blogy" href="http://www.abclinuxu.cz/auto/blog.rss" type="application/rss+xml">
        <link rel="alternate" title="abclinuxu.cz: linuxové blogy" href="http://www.abclinuxu.cz/auto/blogDigest.rss" type="application/rss+xml">
        <link rel="alternate" title="abclinuxu.cz: zprávičky" href="http://www.abclinuxu.cz/auto/zpravicky.rss" type="application/rss+xml">
        <link rel="alternate" title="abclinuxu.cz: diskuse" href="http://www.abclinuxu.cz/auto/diskuse.rss" type="application/rss+xml">
        <link rel="alternate" title="abclinuxu.cz: bazar" href="http://www.abclinuxu.cz/auto/bazar.rss" type="application/rss+xml">
        <link rel="alternate" title="abclinuxu.cz: hardware" href="http://www.abclinuxu.cz/auto/hardware.rss" type="application/rss+xml">
        <link rel="alternate" title="abclinuxu.cz: software" href="http://www.abclinuxu.cz/auto/software.rss" type="application/rss+xml">
        <link rel="alternate" title="abclinuxu.cz: ankety" href="http://www.abclinuxu.cz/auto/ankety.rss" type="application/rss+xml">
        <link rel="bookmark" href="#obsah" title="Obsah stránky" type="text/html">
    </#if>
    <#if RSS??>
        <link rel="alternate" title="RSS zdroj aktuální sekce" href="http://www.abclinuxu.cz${RSS}" type="application/rss+xml">
    </#if>

    <#if ASSIGNED_TAGS?? && ASSIGNED_TAGS?size &gt; 0>
        <#assign keywords = "">
        <#list ASSIGNED_TAGS as tag>
           <#assign keywords = keywords + tag.title + ", ">
        </#list>
        <meta name="keywords" lang="cs" content="${keywords}">
    <#elseif IS_INDEX??>
        <meta name="keywords" lang="cs" content="linux,open source,free software,linux hardware,software,ovladače,pomoc">
    </#if>

    <script type="text/javascript" src="/data/site/jquery/jquery-1.3.2.min.js"></script>
    <script type="text/javascript" src="/data/site/scripts.js"></script>
    <#if ! IS_INDEX??>
        <script type="text/javascript" src="/data/syntaxhighlighter/scripts/shCore.js"></script>
        <script type="text/javascript" src="/data/syntaxhighlighter/scripts/shBrushBash.js"></script>
        <script type="text/javascript" src="/data/syntaxhighlighter/scripts/shBrushCSharp.js"></script>
        <script type="text/javascript" src="/data/syntaxhighlighter/scripts/shBrushCpp.js"></script>
        <script type="text/javascript" src="/data/syntaxhighlighter/scripts/shBrushCss.js"></script>
        <script type="text/javascript" src="/data/syntaxhighlighter/scripts/shBrushDelphi.js"></script>
        <script type="text/javascript" src="/data/syntaxhighlighter/scripts/shBrushDiff.js"></script>
        <script type="text/javascript" src="/data/syntaxhighlighter/scripts/shBrushGroovy.js"></script>
        <script type="text/javascript" src="/data/syntaxhighlighter/scripts/shBrushJScript.js"></script>
        <script type="text/javascript" src="/data/syntaxhighlighter/scripts/shBrushJava.js"></script>
        <script type="text/javascript" src="/data/syntaxhighlighter/scripts/shBrushJavaFX.js"></script>
        <script type="text/javascript" src="/data/syntaxhighlighter/scripts/shBrushPerl.js"></script>
        <script type="text/javascript" src="/data/syntaxhighlighter/scripts/shBrushPhp.js"></script>
        <script type="text/javascript" src="/data/syntaxhighlighter/scripts/shBrushPlain.js"></script>
        <script type="text/javascript" src="/data/syntaxhighlighter/scripts/shBrushPowerShell.js"></script>
        <script type="text/javascript" src="/data/syntaxhighlighter/scripts/shBrushPython.js"></script>
        <script type="text/javascript" src="/data/syntaxhighlighter/scripts/shBrushRuby.js"></script>
        <script type="text/javascript" src="/data/syntaxhighlighter/scripts/shBrushSql.js"></script>
        <script type="text/javascript" src="/data/syntaxhighlighter/scripts/shBrushVb.js"></script>
        <script type="text/javascript" src="/data/syntaxhighlighter/scripts/shBrushXml.js"></script>
        <script type="text/javascript">$(document).ready(function () {SyntaxHighlighter.all();});</script>
    </#if>
    <@lib.initRTE />
    <script type="text/javascript">
    	Page = new Object();
        <#if RELATION??>
        	Page.relationID = ${RELATION.id};
	    </#if>
        <#if USER??>
        	Page.userID = ${USER.id};
            Page.ticket = "${TOOL.ticketValue(USER)}";
	    </#if>
    </script>
    <#if html_header??>
        ${html_header}
    </#if>
</head>

<body id="www-abclinuxu-cz">

<@lib.advertisement id="netmonitor" />

<@lib.advertisement id="ad-init" />

<@lib.advertisement id="sf-lista" />

<@lib.advertisement id="arbo-lb" />

<div id="zh-kont">
    <div id="zh-text" class="zh-box">
        <@lib.advertisement id="zahl-vlevo" />
        <@lib.advertisement id="zahl-vpravo" />
        <@lib.advertisement id="zahl-komplet" />
    </div>
    <div id="zh-logo" class="zh-box"><a href="/"></a></div>
    <div id="zh-hledani" class="zh-box">
        <form action="/hledani" method="get">
            <div>
                <input type="text" class="text" name="dotaz">
                <input class="button" type="submit" value="Hledej">
            </div>
        </form>
        <a href="/hledani?action=toAdvanced">Rozšířené hledání</a>
    </div>
</div>
<div class="cistic"></div>

<#include "/include/menu.txt">

<div class="obal">

    <div class="hl">
        <div class="hl_vpravo">
            <#if USER??>
                <@lib.showUser USER/> |
                <#assign blogName = TOOL.xpath(USER,"/data/settings/blog/@name")!"UNDEF">
                <#if blogName!="UNDEF"><a href="/blog/${blogName}">Blog</a> |</#if>
                <a href="/History?type=comments&amp;uid=${USER.id}">Mé komentáře</a> |
                <a href="/History?type=discussions&amp;uid=${USER.id}&amp;filter=last">Navštívené diskuse</a> |
                <a href="/History?type=questions&amp;uid=${USER.id}">Mé dotazy</a> |
                <a href="/lide/${USER.login}/zalozky">Záložky</a> |
                <a href="/Profile/${USER.id}?action=myPage">Nastavení</a> |
                <a href="${URL.noPrefix("/Index?logout=true")}">Odhlásit</a>
            <#else>
                <a href="${URL.noPrefix("/Profile?action=login")}">Přihlášení</a> |
                <a href="${URL.noPrefix("/EditUser?action=register")}">Registrace</a>
            </#if>
        </div>
        <div class="hl_vlevo">
            <ul class="menu-top">
                <li>
                    <a href="/doc/napoveda/alternativni-design">Styl</a>
                    <ul class="menu-drop">
                        <#list TOOL.getOfferedCssStyles().entrySet() as style>
                        <li>
                            <a href="/EditUser/<#if USER??>${USER.id}</#if>?action=changeStyle${TOOL.ticket(USER,false)}&amp;css=${style.key}">${style.value}</a>
                        </li>
                        </#list>
                    </ul>
                </li>
            </ul>
        </div>
    </div>
