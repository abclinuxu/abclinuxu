
<p><b>${QUESTIONER}: ${QUESTION}</b></p>

<#list RESPONSES as response>
    <p>
        <b>${RESPONDERS[response_index]}</b>:
        ${response}
    </p>
    <hr>
</#list>
