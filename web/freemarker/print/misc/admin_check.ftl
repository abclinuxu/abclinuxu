<html>
<head>
 <title>${TITLE}</title>
</head>
<body>
<#if DATABASE_VALID>
 <#global PERSISTANCE="OK">
<#else>
 <#global PERSISTANCE="porucha">
</#if>
<#if FULLTEXT_VALID>
 <#global FULLTEXT="OK">
<#else>
 <#global FULLTEXT="porucha">
</#if>
Persistance:${PERSISTANCE}<br>
Fulltext:${FULLTEXT}<br>
</body>
</html>
