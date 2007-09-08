URL pozadavku: http://www.abclinuxu.cz${URL}
<#assign url = TOOL.xpath(REQUEST, "data/url")?default("UNDEFINED")><#if url != "UNDEFINED">URL dokumentu: ${url}</#if>
Pozadavek: ${TOOL.xpath(REQUEST, "/data/text")}
