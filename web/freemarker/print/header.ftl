<html>
<head>
<title>${TITLE}</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-2">
<link rel="StyleSheet" href="/main.css" type="text/css">
</head>
<body bgcolor="white" style="margin: 5px">
<#import "macros.ftl" as lib>
<#include "/include/pocitani.txt">
<p>Portál <a href="http://www.abclinuxu.cz">AbcLinuxu</a>
<#if USER?exists>, přihlášen ${USER.name}</#if>
, ${DATE.show("CZ_FULL")}</p>
<!-- obsah -->
