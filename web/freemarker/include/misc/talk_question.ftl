
<p><b>${QUESTIONER}</b> - ${QUESTION}</p>

<#list RESPONSES as response>
    <p>
        <b>${RESPONDERS[response_index]}</b>
        - ${response}
    </p>
</#list>
