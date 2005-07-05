<html>
<head>
<title>${TITLE}</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-2">
<link rel="StyleSheet" href="/styles.css" type="text/css">
</head>
<body bgcolor="white" style="margin: 5px">
<#import "macros.ftl" as lib>
<p>Portál <a href="http://www.abclinuxu.cz">AbcLinuxu</a><#if USER?exists>,
přihlášen ${USER.name}</#if>, ${DATE.show("CZ_FULL_TXT")}
<#include "/include/pocitani2.txt">
<#include "/include/pocitani1.txt">
</p>
<!-- obsah -->
