Vazeny uzivateli,

bohuzel vas musime informovat, ze vase nize uvedena zpravicka
byla smazana nasim administratorem. Typicky se tak deje, pokud
zpravicka duplikuje jiz existujici zpravicku, neodpovida
zamereni naseho portalu nebo jde o nepovolenou inzerci.

Puvodni URL:
 http://www.abclinuxu.cz/news/ViewRelation?rid=${RELATION.id}
Datum:
 ${DATE.show(RELATION.child.created,"CZ_FULL")}

${TOOL.xpath(RELATION.child,"data/content")}

<#if MESSAGE?exists>
Vzkaz od administratora:

${MESSAGE}
</#if>

Za portal www.abclinuxu.cz
vas administrator ${ADMIN.name}

