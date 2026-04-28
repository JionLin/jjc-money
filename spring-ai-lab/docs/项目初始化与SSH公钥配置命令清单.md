# 项目初始化与 SSH 公钥配置命令清单（Git Bash）

> 适用环境：Windows + Git Bash（MINGW64）  
> 当前项目目录：`/d/spring-ai-lab`

## 1. 在当前目录初始化 Git 仓库

```bash
git init
```

## 2. 补充忽略规则（日志与环境变量）

```bash
printf "\n*.log\n.env\n" >> ".gitignore"
```

## 3. 首次提交

```bash
git add .
git commit -m "chore: initial commit"
```

## 4. 生成 SSH 密钥对（含公钥）

```bash
ssh-keygen -t ed25519 -C "250912986@qq.com"
```

执行后按提示：

- `Enter file in which to save the key ...`：直接回车（使用默认路径）
- `Enter passphrase ...`：可回车留空，或输入口令
- `Enter same passphrase again ...`：与上一步一致

## 5. 启动 ssh-agent 并加载私钥

```bash
eval "$(ssh-agent -s)"
ssh-add ~/.ssh/id_ed25519
```

## 6. 查看并复制公钥（用于 GitHub）

```bash
cat ~/.ssh/id_ed25519.pub
```

复制整行内容（从 `ssh-ed25519` 开头，到邮箱结尾）到 GitHub 的 `Settings -> SSH and GPG keys -> New SSH key`。

## 7. 网络限制场景（22 端口不可用）切换到 443

```bash
mkdir -p ~/.ssh
cat > ~/.ssh/config << 'EOF'
Host github.com
  HostName ssh.github.com
  Port 443
  User git
  IdentityFile ~/.ssh/id_ed25519
  IdentitiesOnly yes
EOF
chmod 600 ~/.ssh/config
```

## 8. 验证 SSH 认证

```bash
ssh -T git@github.com
```

出现 `successfully authenticated` 即表示 SSH 配置成功。
