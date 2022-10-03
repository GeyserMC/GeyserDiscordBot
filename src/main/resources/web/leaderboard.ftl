<#import "template.ftl" as t/>
<@t.page title="Levels">
    <style>
        .avatar {
            border-radius: 50%;
            height: 4rem;
            margin-left: .5rem;
        }
        .place {
            border-radius: 50%;
            height: 3rem;
            width: 3rem;
            background-color: var(--bs-secondary);
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: bold;
            flex: none;
        }
        .place-1 {
            background-color: #da9e3b;
        }
        .place-2 {
            background-color: #989898;
        }
        .place-3 {
            background-color: #ae7441;
        }
    </style>

    <div class="d-flex flex-row align-items-center">
        <#if guild.iconUrl??><img src='${guild.iconUrl}' alt='${guild.name}%27s Logo' class='rounded-circle p-2' /></#if>
        <h1>
            ${guild.name}<br>
            <small class="text-muted">Levels: Top 100</small>
        </h1>
    </div>

    <div class="row row-cols-1 g-2 pb-2">
        <#list rows as row>
            <div class='col' id="${row.user.id}">
                <div class='p-3 border rounded bg-light'>
                    <div class='row'>
                        <div class='col d-flex flex-row align-items-center'>
                            <div class='place place-${row_index + 1}'>${row_index + 1}</div>
                            <img src='${row.user.effectiveAvatarUrl!}' class='avatar'  alt="${row.user.name}#${row.user.discriminator}' avatar"/>
                            <span class='p-2 text-break'>${row.user.name}#${row.user.discriminator}</span>
                        </div>
                        <div class='col-xl-4 text-center justify-content-center justify-content-xl-end d-flex flex-row'>
                            <div class='p-2'>
                                <h6><small class='text-muted'>MESSAGES</small></h6>
                                <h6>${row.messages}</h6>
                            </div>
                            <div class='p-2'>
                                <h6><small class='text-muted'>EXPERIENCE</small></h6>
                                <h6>${row.xp}</h6>
                            </div>
                            <div class='p-2'>
                                <h6><small class='text-muted'>LEVEL</small></h6>
                                <h6>${row.level}</h6>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </#list>
    </div>
</@t.page>