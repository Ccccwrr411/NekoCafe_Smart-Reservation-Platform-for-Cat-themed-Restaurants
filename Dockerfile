# 使用 nginx 镜像作为 web 服务器
FROM nginx:alpine

# 删除 nginx 默认的欢迎页
RUN rm -rf /usr/share/nginx/html/*

# 复制你的 index.html 到 nginx 的 html 目录
COPY index.html /usr/share/nginx/html/

# 暴露 80 端口
EXPOSE 80

# 启动 nginx
CMD ["nginx", "-g", "daemon off;"]