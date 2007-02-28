<html>
<head>
 <title>${TITLE}</title>
</head>
<body>
<#if DATABASE_VALID>
 <#assign PERSISTANCE="OK">
<#else>
 <#assign PERSISTANCE="porucha">
</#if>
<#if FULLTEXT_VALID>
 <#assign FULLTEXT="OK">
<#else>
 <#assign FULLTEXT="porucha">
</#if>
Persistance: ${PERSISTANCE}<br>
Fulltext: ${FULLTEXT}<br>
SQL: ${QUERIES}<br>
</body>
</html>
