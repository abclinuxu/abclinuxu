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

DataScript s.r.o. jako odborna vzdelavaci a treninkova
spolecnost je certifikovanym Red Hat Training Partnerem.
V ramci sve cinnosti poskytuje skoleni a certifikace
pro dosazeni kvalifikaci Red Hat Certified Technician
(RHCT) a Red Hat Certified Engineer (RHCE).

Skoleni a certifikace Red Hat jsou dlouhodobe povazovana
za jednoznacne nejlepsi a nejprinosnejsi pro praxi v cele
oblasti Linuxu.

http://www.abclinuxu.cz/skoleni/red-hat



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
