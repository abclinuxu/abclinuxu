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

Zvysujici se pocet trestnych cinu spachanych prostrednictvim
internetu nuti k vaznemu zamysleni nad bezpecnosti
pocitacovych siti. Trh produktu a sluzeb souvisejicich
s bezpecnosti je tak velky, ze je pro zakaznika narocne se
rozhodnout. Jak se rozhodnout?

Mate prilezitost zucastnit se prvniho Kongresu Bezpecnosti
Siti v Ceske republice. Kongres bude se konat 11. dubna 2007
v Hotelu Diplomat.

Pro ucastniky je vstup ZDARMA! Staci se jen zaregistrovat
na webove strance: www.kongresbezpecnosti.org


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

Sun Microsystems opens another new team in Prague.
The new group will be an extension of Sun's existing
Web Engineering Team with a specific focus on ecommerce
and backend system integration and web service development.

Developers will be working with the Intershop Enfinity
application suite and will be reshaping the way that Sun
does over 2 billion annually in global web based commerce.
Team members will collaborate with the Prague team (target:
6 developers) as well as engineering staff located worldwide,
including Germany, the UK, California and Colorado.

Open positions:

Software Engineering Manager - Webservices
Java Team Leader - Webservices
Application Engineer - Webservices
Junior J2EE Software Engineer - Webservices
Senior J2EE Software Engineer - Webservices

If you are interested in the new positions please send
your CV to Tomas Kolsky at t.kolsky@talents.cz, or visit
www.talents.cz/sunjobs.


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
