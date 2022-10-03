<#import "template.ftl" as t/>
<@t.page>
    <div class="d-flex flex-row align-items-center">
        <img src='${self.effectiveAvatarUrl}' alt='${self.name}%27s Avatar' class='rounded-circle p-2' />
        <h1>
            ${self.name} <a href="https://github.com/GeyserMC/GeyserDiscordBot/" class="fs-4"><i class="bi bi-file-earmark-code"></i></a><br>
            <small class="text-muted">Serving ${guilds?size} Discord server<#if guilds?size != 1>s</#if> and ${members} members!</small>
        </h1>
    </div>
    <div class="row row-cols-3 g-2 pb-2">
        <#list guilds as guild>
            <div class="col" id="${guild.id}">
                <div class="card bg-light h-100">
                    <div class="row g-0 h-100">
                        <div class="col-md-4 d-flex flex-row align-items-center p-2">
                            <#if guild.iconUrl??><img src="${guild.iconUrl}" class="card-img-top m-auto d-block" alt="${guild.name}%27s Logo" style="max-height: 128px;max-width: 128px;"></#if>
                        </div>
                        <div class="col-md-8">
                            <div class="card-body d-flex flex-column h-100">
                                <h5 class="card-title">${guild.name}</h5>
                                <#if guild.description?? && guild.description?has_content><p class="card-text">${guild.description}</p></#if>
                                <a href="/leaderboard/${guild.id}" class="btn btn-primary mt-auto">View leaderboard</a>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </#list>
    </div>
</@t.page>