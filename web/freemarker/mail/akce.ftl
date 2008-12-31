Dovolujeme si vam pripomenout, ze uz za dva dny
se kona akce, u ktere jste potvrdil svou ucast.

Nazev akce: ${TOOL.childName(ITEM)}
URL: http://www.abclinuxu.cz${RELATION.url}
Zacatek: ${DATE.show(ITEM.created,"CZ_FULL",false)}
<#if ITEM.date1??>Konec: ${DATE.show(ITEM.date1,"CZ_FULL",false)}</#if>

${TOOL.xpath(ITEM,"//descriptionShort")}

