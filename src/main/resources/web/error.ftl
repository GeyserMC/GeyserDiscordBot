<#import "template.ftl" as t/>
<@t.page title="Error ${errorCode}">
    <style>
        html, body {
            height: 100%;
        }

        .container {
            height: 100%;
            display: flex;
            justify-content: center;
            align-items: center;
            text-align: center;
        }
    </style>
    <div>
        <h1>
            Error ${errorCode}<br>
            <small class="text-muted">${errorMessage}</small>
        </h1>
    </div>
</@t.page>