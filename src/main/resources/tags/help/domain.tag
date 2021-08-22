type: text
aliases: domains, srv, srvrecord
---

Bedrock doesn't work with SRV DNS records, so you'll have to use an A record instead. Java players will be able to connect fine with an A record.

If your server is in a hosting company, and the address they assign you is not a string of numbers (raw IP), a CNAME record will also work for redirecting both Java and Bedrock players.
