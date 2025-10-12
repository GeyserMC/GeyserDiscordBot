type: text
aliases: domains, srv, srvrecord, dns, domain
title: :tools: Domain Guide
color: help
button: [Setup Guide](https://wiki.geysermc.org/geyser/setup/)

---

Before doing this domain guide, please make sure you have configured Geyser to allow you to be able connect with the **Numeric IP + Port** using the Geyser setup guide. To setup a domain for Geyser:
1. Get a domain (e.g. via [Cloudflare](https://domains.cloudflare.com/))
2. Add an A-Record:
└> Bedrock doesn't work with SRV DNS records, so you'll have to use an A record instead.
└> Java players will be able to connect fine with an A record.
└> A CNAME record will also work for redirecting both Java and Bedrock players.
3. Point the record to the IP of your server.
4. Do NOT enable "proxied" mode, it has to be DNS-only!
5. Connect to your server with your domain and port (e.g. `play.example.com:19132`).