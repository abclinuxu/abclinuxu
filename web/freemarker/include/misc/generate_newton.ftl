<?xml version="1.0" encoding="UTF-8" ?>
<abc>
    <#list ARTICLES as relation>
    <#assign article = relation.child, autors=TOOL.createAuthorsForArticle(article)>
    <Article>
        <ArtID>${relation.id}</ArtID>
        <date>${DATE.show(article.created, "CZ_DMY2",false)}</date>
        <title>${TOOL.xpath(article,"data/name")}</title>
        <perex>${TOOL.xpath(article,"/data/perex")?xml}</perex>
        <body><![CDATA[${TOOL.getCompleteArticleText(article)}]]></body>
        <author>${TOOL.childName(autors[0])}</author>
        <section>${TOOL.xpath(relation.parent,"data/name")}</section>
        <url>http://www.abclinuxu.cz${relation.url}</url>
	</Article>
    </#list>
    <#list NEWS as relation>
    <#assign item = relation.child, autor=TOOL.createUser(item.owner)>
    <News>
        <ArtID>${relation.id}</ArtID>
        <date>${DATE.show(item.created, "CZ_DMY2",false)}</date>
        <title>${TOOL.xpath(item,"data/title")?xml}</title>
        <body><![CDATA[${TOOL.xpath(item,"data/content")}]]></body>
        <author>${autor.name}</author>
        <section>${NEWS_CATEGORIES[item.subType].name}</section>
        <url>http://www.abclinuxu.cz${relation.url}</url>
	</News>
    </#list>
</abc>
