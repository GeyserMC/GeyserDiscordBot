type: text
issues: Please connect through the official Geyser

---

This error is caused by a Floodgate key.pem mismatch, most likely between your proxy and your backend servers.
Make sure that the key.pem file in every Floodgate config folder is the same file.
If this still doesn't work and you're using an FTP client, try using the [WinSCP](https://winscp.net/eng/index.php) client.