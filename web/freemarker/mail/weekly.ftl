Tyden ${WEEK}/${YEAR}

Prehled clanku
==============

<#list ARTICLES as clanek>
 ${clanek.title}
 ${DATE.show(clanek.published, "CZ_FULL")} | ${clanek.author}

 ${clanek.perex}

 http://www.abclinuxu.cz/clanky/show/${clanek.relationId}

 ---------------------

</#list>

Prehled zpravicek
=================

<#list NEWS as news>
 ${news.content}
 ${DATE.show(news.published, "CZ_FULL")} | ${news.author}

 Komentaru: ${news.comments}

 http://www.abclinuxu.cz/news/show/${news.relationId}

 ---------------------

</#list>



Pokud si neprejete dale dostavat tyto emaily, muzete tak ucinit
na adrese http://www.abclinuxu.cz/EditUser/${USER.id}?action=subscribe
Vase prihlasovaci jmeno je ${USER.login}.
