<#macro exportStory(story)><#assign owner=TOOL.createUser(BLOG.owner), ITEM=story.child, category = ITEM.subType!"UNDEF"><#if category!="UNDEF"><#assign category=TOOL.xpath(BLOG, "//category[@id='"+category+"']/@name")!"UNDEF"></#if>
TITLE: ${ITEM.title}
AUTHOR: ${owner.nick?default(owner.name)}
DATE: ${DATE.show(ITEM.created, "US_FULL")}<#if category!="UNDEF">
CATEGORY: ${category}</#if>
STATUS: <#if ITEM.type==15>draft<#else>publish</#if>
-----
<#assign text = TOOL.xpath(ITEM, "/data/perex")!"UNDEF"><#if text!="UNDEF">BODY:
${text}
-----
EXTENDED BODY:
${TOOL.xpath(ITEM, "/data/content")}
-----<#else>BODY:
${TOOL.xpath(ITEM, "/data/content")}
-----</#if>
--------
</#macro>

<#list STORIES as story><@exportStory story/></#list>
<#list UNPUBLISHED_STORIES! as story><@exportStory story/></#list>
