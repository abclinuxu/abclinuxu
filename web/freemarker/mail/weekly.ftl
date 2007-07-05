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

IBM Lotus Notes 8 prichazi!

Efektivni zpracovani informaci je zakladem vysoke
produktivity. IBM Lotus Notes 8 predstavuje nadstandardni
e-mail a kalendarove sluzby s novym uzivatelskym prostredim,
ale i chat, sdileni dokumentu, integrovany kancelarsky
balik, workflow a propojeni s dalsimi aplikace.

http://web.ogilvyinteractive.cz/ibmcz/logindex.php?kampId=358&odkazId=703&refId=39&redirect=http%3A%2F%2Fwww-306.ibm.com%2Fsoftware%2Fcz%2Flotus%2F

Prehled zpravicek
=================

<#list NEWS as news>
 ${news.title}
 ${DATE.show(news.published, "CZ_FULL")} | ${news.author} | Komentaru: ${news.comments}

 ${news.content}

 http://www.abclinuxu.cz${news.url}

 ---------------------
</#list>


Pokud si jiz neprejete zasilat tyto emaily,
zmente si nastaveni na adrese:
http://www.abclinuxu.cz/EditUser/${USER.id}?action=subscribe
Vase prihlasovaci jmeno je ${USER.login}.
