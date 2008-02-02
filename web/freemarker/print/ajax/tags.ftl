<?xml version="1.0" encoding="UTF-8" ?>
<stitky allowCreate="${CREATE_POSSIBLE?string}">
<#list TAGS as tag>
<s l="${tag.title}" i="${tag.id}"/>
</#list>
</stitky>