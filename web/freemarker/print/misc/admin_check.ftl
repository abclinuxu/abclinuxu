<html>
<head>
 <title>${TITLE}</title>
 <style type="text/css">
   span { color: red }
 </style>
</head>
<body>
<#if DATABASE_VALID>
 <#global PERSISTANCE="OK">
<#else>
 <#global PERSISTANCE="<span>porucha</span>">
</#if>
Persistance: ${PERSISTANCE}<br>
</body>
</html>