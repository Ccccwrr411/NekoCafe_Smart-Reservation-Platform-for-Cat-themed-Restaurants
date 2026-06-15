.PHONY: up down logs ps restart clean init help

# 默认目标
help:
	@echo "NekoCafé 可用命令:"
	@echo ""
	@echo "  make init      - 首次初始化（需先配置 .env）"
	@echo "  make up        - 构建并启动所有服务"
	@echo "  make down      - 停止并移除所有容器（保留数据）"
	@echo "  make restart   - 重启所有服务"
	@echo "  make logs      - 查看所有服务日志"
	@echo "  make ps        - 查看容器状态"
	@echo "  make clean     - 停止并删除容器+卷（⚠️ 会丢失数据）"
	@echo ""
	@echo "  单独操作:"
	@echo "    make up-backend    只启动后端"
	@echo "    make logs-backend  只看后端日志"

up:			; docker compose up -d --build
down:		; docker compose down
restart:	; docker compose restart
logs:		docker compose logs -f --tail=100
ps:			docker compose ps
clean:		docker compose down -v --rmi local
init:		; @bash scripts/init-deploy.sh "$(REPO_URL)"

# 单独服务操作
up-backend:			; docker compose up -d --build backend
logs-backend:		docker compose logs -f --tail=100 backend
restart-backend:	docker compose restart backend
