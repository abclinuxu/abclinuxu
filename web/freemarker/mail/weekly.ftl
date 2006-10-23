Tyden ${WEEK}/${YEAR}

Zasilani tohoto emailu jste si aktivovali ve svem profilu.
Informace o odhlaseni najdete na konci tohoto emailu.

Prehled clanku
==============

<#list ARTICLES as clanek>
 ${clanek.title}
 ${DATE.show(clanek.published, "CZ_FULL")} | ${clanek.author} | Komentaru: ${clanek.comments}

 ${clanek.perex}

 http://www.abclinuxu.cz${clanek.url}

 ---------------------

</#list>


Reklama
=======

Recruiting the best skills. IBM welcomes all talent.

IBM IT professionals working at the Integrated Delivery Center (IDC) Brno
provide support to international clients in various business areas such
banking, insurance, automotive, retail, telecommunications, public sector as
well as small and medium enterprises.

Joining the IT team in IDC Brno will give you an unique opportunity to
enhance your IT professional career in all areas of IT infrastructure
services (server management / operating systems, databases and application
hosting/, managing corporate network infrastructure, PC/ Laptop end-user's
support, qualified user help desk operations, etc.)

For more details, please, visit our site:
ibm.com/employment/cz/delivery_centre/index.html
or email us: delivery_center@cz.ibm.com

---------------------

Programujte se Seznamem

Presne jednadvacaty rijnovy den spustil internetovy portal Seznam.cz
na svych strankach soutez pro talentovane programatory, kteri maji zajem
zmerit sve odborne syly a take blize poznat zazemi nejnavstevovanejsiho
ceskeho portalu.

http://www.abclinuxu.cz/clanky/pr/programujte-se-seznamem


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
