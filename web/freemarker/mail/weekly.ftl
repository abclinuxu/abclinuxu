Tyden ${WEEK}/${YEAR}

Zasilani tohoto emailu jste si aktivovali ve svem profilu.
Informace o odhlaseni najdete na konci tohoto emailu.

Prehled clanku
==============

<#list ARTICLES as clanek>
 ${clanek.title}
 ${DATE.show(clanek.published, "CZ_FULL")} | <#rt>
 <#lt><#list clanek.authors as author>${TOOL.childName(author)}<#if author_has_next>, </#if></#list><#rt>
 <#lt> | Komentaru: ${clanek.comments}

 ${clanek.perex}
 http://www.abclinuxu.cz${clanek.url}
 ---------------------

</#list>


Reklama
=======

PERSONALNI INZERCE: Nase spolecnost pusobi v oblasti
hostingu jiz nekolik let. Do naseho portfolia sluzeb
patri i sprava vnitropodnikovych siti a vyvoj
intranetovych systemu. Hledame proto noveho kolegu na
pozici Administrator linuxovych serveru. Podrobnosti:

http://www.abcprace.cz/hosting90-s-r-o-2/administrator-linuxovych-serveru


Prehled zpravicek
=================

<#list NEWS as news>
 ${news.title}
 ${DATE.show(news.published, "CZ_FULL")} | ${news.author} | Komentaru: ${news.comments}

 ${news.content}

 http://www.abclinuxu.cz${news.url}

 ---------------------
</#list>


<#if JOBS?size gt 0>
Nove nabidky zamestnani
=======================

Prehled novych IT a linuxovych pracovnich pozic ze serveru www.praceabc.cz.

<#list JOBS as job>
${job.title}
Region: ${job.region}, Kategorie: ${job.category}<#if job.itJob>, IT</#if><#if job.linuxJob>, Linux</#if>
http://www.praceabc.cz/www/detail.php?id=${job.id}

 ---------------------

</#list>
</#if>

Pokud si jiz neprejete zasilat tyto emaily, zmente si nastaveni na adrese
http://www.abclinuxu.cz/EditUser/${USER.id}?action=subscribe
Vase prihlasovaci jmeno je ${USER.login}.
