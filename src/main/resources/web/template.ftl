<#macro page title="">
    <!doctype html>
    <html lang="en">
    <head>
        <!-- Required meta tags -->
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">

        <!-- Bootstrap CSS -->
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.1/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-+0n0xVW2eSR5OomGNYDnhzAbDsOXxcvSN1TPprVMTNDbiYZCxYbOOl7+AMvyTG2x" crossorigin="anonymous">

        <!-- Bootstrap Icons CSS -->
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.5.0/font/bootstrap-icons.css">

        <title>Geyser Bot<#if title?has_content>: ${title}</#if></title>

        <style>
            .btn-toggle-mode {
                position: absolute;
                top: 5px;
                right: 5px;
            }

            .dark-mode {
                background-color: var(--bs-dark);
                color: var(--bs-white);
            }
            .dark-mode .bg-light {
                background-color: #42484c !important;
            }
            .dark-mode .border {
                border-color: var(--bs-gray-dark) !important;
            }
            .dark-mode .text-muted {
                color: #c3ccd5 !important;
            }
            .dark-mode .dark-hidden {
                display: none;
            }
            .dark-visible {
                display: none;
            }
            .dark-mode .dark-visible {
                display: block;
            }
        </style>
    </head>
    <body class="${darkMode?then('dark-mode', '')}">
    <div class="container">
        <button type="button" onclick="toggleDark()" class="btn btn-light dark-visible btn-toggle-mode"><i class="bi bi-sun-fill"></i></button>
        <button type="button" onclick="toggleDark()" class="btn btn-dark dark-hidden btn-toggle-mode"><i class="bi bi-sun"></i></button>

        <#nested/>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/@popperjs/core@2.9.2/dist/umd/popper.min.js" integrity="sha384-IQsoLXl5PILFhosVNubq5LC7Qb9DXgDA9i+tQ8Zj3iwWAwPtgFTxbJ8NT4GN1R8p" crossorigin="anonymous"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.0.1/dist/js/bootstrap.min.js" integrity="sha384-Atwg2Pkwv9vp0ygtn1JAojH0nYbwNJLPhwyoVbhoPwBhjQPR5VtM2+xf0Uwh9KtT" crossorigin="anonymous"></script>

    <script>
        function toggleDark() {
            document.body.className = document.body.className == "dark-mode" ? "" : "dark-mode";
            document.cookie = "darkMode=" + document.body.className;
        }
    </script>
    </body>
    </html>
</#macro>