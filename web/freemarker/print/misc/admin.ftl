<#include "/include/macros.ftl">
<#include "../header.ftl">

<#call showMessages>

<p>Tato str�nka slou�� jako rozcestn�k pro
administr�tory. K n� samotn� nen� kontrolov�n
p��stup, zda u�ivatel m� dostate�n� pr�va
�i nikoliv. K n�kter�m akc�m v�ak ji�
pot�ebujete dostate�n� pr�va.
</p>

<h1>Vypr�zdn�n� cache</h1>

<p>Tato akce slou�� pro vypr�zdn�n� transparentn�
cache. N�sledkem toho budou v�echny objekty znovu
na�teny z datab�ze. Typick� pou�it� je, pokud ru�n�
provedete zm�ny v datab�zi a nechcete �ekat, kdy se
projev�.
</p>

<p><a href="${URL.noPrefix("/Admin?action=clearCache")}">Clear cache</a></p>


<h1>Spr�va pr�v u�ivatel�</h1>

<p>P�i�azov�n� rol� jednotliv�m u�ivatel�m.</p>

<p><a href="${URL.noPrefix("/EditUser?action=grant")}">Grant roles</a></p>

<p>Zneplatn�n� emailu u�ivatel�m</p>

<p><a href="${URL.noPrefix("/EditUser?action=invalidateEmail")}">Invalidate emails</a></p>


<h1>Vytvo�en� ankety</h1>

<p>Zde je mo�n� vytvo�it anketu.</p>

<p><a href="${URL.noPrefix("/EditSurvey?action=add")}">Create survey</a></p>


<h1>Statistika JDBC poolu</h1>

<p>Statistick� informace o JDBC poolu.</p>

<p><a href="/ProxoolAdmin">Proxool info</p>

<#include "../footer.ftl">
