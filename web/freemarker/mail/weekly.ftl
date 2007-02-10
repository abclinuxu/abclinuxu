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

Zveme Vas na workshop "JBoss sales day CZ", ktery Vas
uvede do obchodnich i technickych zakladu produktu JBoss.
Workshop se kona 27.2. od 10.00 hod v budove spolecnosti
Servodata, Dolnomecholupska 12, Praha 10.
Workshop je pro ucastniky zdarma.

Vice informaci o produktu a registraci naleznete na:
http://www.studovna.servodata.cz/home.php3?lang=cz&id=62


Prehled zpravicek
=================

<#list NEWS as news>
 ${news.title}
 ${DATE.show(news.published, "CZ_FULL")} | ${news.author} | Komentaru: ${news.comments}

 ${news.content}

 http://www.abclinuxu.cz${news.url}

 ---------------------
</#list>


Reklama
=======

CVO Technology Recruitment je ta spravna platforma
pro rizeni Vasi dalsi profesni kariery! Pro vedouci
mezinarodni i lokalni technologicke spolecnosti
v regionu stredni a vychodni Evropy hledame technicke
nadsence na specializovane IT pracovni pozice.

http://www.cvotechnology.com/JobSearch.aspx?Search=auto&CountryId=Czech%20Republic?utm_source=abcprace&utm_medium=banner&campaign=cvot


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
