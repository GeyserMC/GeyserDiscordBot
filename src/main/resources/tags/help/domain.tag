type: text
aliases: domains, srv, srvrecord, dns, domain

---
Domain guide: 
First, configure Geyser, so that you are able to connect with the **numeric IP + port** (see the usual setup guide on https://wiki.geysermc.org/geyser/setup/) 
When that is done & you are able to connect with the numeric IP + port, proceed with setting up the domain:

1. Get a domain (e.g. via Cloudflare)
2. Add an A-Record (Bedrock doesn't work with SRV DNS records, so you'll have to use an A record instead. Java players will be able to connect fine with an A record. A CNAME record will also work for redirecting both Java and Bedrock players).  
3. Point the record to the IP of your server.
4. Do NOT enable "proxied" mode, it has to be DNS-only!

Then, you should be able to use your domain together with the port to connect to your server. 
