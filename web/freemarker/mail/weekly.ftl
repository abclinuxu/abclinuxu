Souhrn clanku pro tyden ${WEEK}/${YEAR}

<#list ARTICLES as relation> <#global clanek=relation.child>
 ${TOOL.xpath(clanek,"data/name")}
 ${DATE.show(clanek.created, "CZ_FULL")} | ${AUTHORS[relation_index].name}

 ${TOOL.xpath(clanek,"/data/perex")}

 http://www.abclinuxu.cz/clanky/ViewRelation?rid=${relation.id}

 ---------------------

</#list>

Zasilani teto sluzby muzete zrusit na adrese:

http://www.abclinuxu.cz/Profile?action=myPage&uid=${USER.id}

Vase prihlasovaci jmeno je ${USER.login}.
