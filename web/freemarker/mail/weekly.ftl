Souhrn clanku pro tyden ${WEEK}/${YEAR}

<#list ARTICLES as relation> <#global clanek=relation.child>
 ${TOOL.xpath(clanek,"data/name")}
 ${DATE.show(clanek.created, "CZ_FULL")} | ${AUTHORS[relation_index].name}

 ${TOOL.xpath(clanek,"/data/perex")}

 http://www.abclinuxu.cz/clanky/show/${relation.id}

 ---------------------

</#list>

Pokud si neprejete dale dostavat tyto emaily, muzete tak ucinit
na adrese http://www.abclinuxu.cz/EditUser/ ${USER.id}?action=subscribe
Vase prihlasovaci jmeno je ${USER.login}.
