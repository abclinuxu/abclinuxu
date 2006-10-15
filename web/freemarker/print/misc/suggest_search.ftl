new Array(
    <#list QUERIES as query>
        new Array("${TOOL.limit(query[0],40,"")?html}","${query[1]}&times;")<#if query_has_next>, </#if>
    </#list>
);