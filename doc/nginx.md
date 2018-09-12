```
 /etc/nginx/sites-enabled/default
 
 server {
        ssl     on;
        listen 443 ssl;

        gzip off;
        charset     utf-8;
        client_max_body_size 75M;

        server_name nonaryr.com; #www.nonaryr.com;

        location / {
                proxy_pass http://127.0.0.1:8081/;
                proxy_set_header Upgrade $http_upgrade;
                proxy_set_header Connection "upgrade";
                proxy_set_header Host $host;
                proxy_http_version 1.1;
                }

        ssl_certificate /etc/letsencrypt/live/nonaryr.com/fullchain.pem;
        ssl_certificate_key /etc/letsencrypt/live/nonaryr.com/privkey.pem;
        ssl_trusted_certificate /etc/letsencrypt/live/nonaryr.com/fullchain.pem;

}

server {
        listen 80 default_server;
        listen [::]:80 default_server;

        server_name nonaryr.com; #www.nonaryr.com;
        return 301 https://$host$request_uri;

        include /etc/nginx/snippets/letsencrypt.conf;

}
#https://oxozle.com/2018/01/21/nastrojka-ssl-let-s-encrypt-v-nginx-na-ubuntu
```

```
/etc/nginx/nginx.conf


user www-data;
worker_processes auto;
pid /run/nginx.pid;

events {
        worker_connections 768;
        # multi_accept on;
}

http {

        sendfile on;
        tcp_nopush on;
        tcp_nodelay on;
        keepalive_timeout 65;
        types_hash_max_size 2048;
        # server_tokens off;

        # server_names_hash_bucket_size 64;
        # server_name_in_redirect off;

        include /etc/nginx/mime.types;
        default_type application/octet-stream;

        ##
        # SSL Settings
        ##

        ssl_protocols TLSv1.1 TLSv1.2; # Dropping SSLv3, ref: POODLE
        ssl_prefer_server_ciphers on;
        ssl_ciphers ECDH+AESGCM:ECDH+AES256:ECDH+AES128:DHE+AES128:!ADH:!AECDH:!MD5;
        #ssl_session_cache   shared:SSL:10m;
        #ssl_session_timeout 10m;       

        ssl_ecdh_curve secp384r1;

        ssl_stapling on;
        ssl_stapling_verify on;

        #add_header Strict-Transport-Security "max-age=15768000; includeSubdomains; preload";
        #add_header X-Frame-Options DENY;
        add_header X-Content-Type-Options nosniff;

        map $http_upgrade $connection_upgrade {
          default upgrade;
          "" close;
         }
                 ##
        # Logging Settings
        ##

        access_log /var/log/nginx/access.log;
        error_log /var/log/nginx/error.log;

        ##
        # Gzip Settings
        ##

        #gzip on;
        #gzip_disable "msie6";

        # gzip_vary on;
        # gzip_proxied any;
        # gzip_comp_level 6;
        # gzip_buffers 16 8k;
        # gzip_http_version 1.1;
        # gzip_types text/plain text/css application/json application/javascript text/xml application/xml application/xml+rss text/javascript;

        ##
        # Virtual Host Configs
        ##

        include /etc/nginx/conf.d/*.conf;
        include /etc/nginx/sites-enabled/*;
}
```
