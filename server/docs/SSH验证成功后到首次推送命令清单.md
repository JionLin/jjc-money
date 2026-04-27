# SSH 验证成功后到首次推送命令清单（Git Bash）

> 适用前提：已执行 `ssh -T git@github.com`，并看到 `successfully authenticated`。  
> 当前项目目录：`/d/spring-ai-lab`

## 1. 添加远程仓库地址

```bash
git remote add origin "git@github.com:jionlin/spring-ai-lab.git"
```

如果提示 `remote origin already exists`，改用：

```bash
git remote set-url origin "git@github.com:jionlin/spring-ai-lab.git"
```

## 2. 将默认分支重命名为 main

```bash
git branch -M main
```

## 3. 首次推送并建立上游跟踪

```bash
git push -u origin main
```

## 4. 验证推送状态

```bash
git status
git remote -v
git log --oneline -n 5
```

期望结果：

- `Your branch is up to date with 'origin/main'`
- 远程地址显示 `origin git@github.com:jionlin/spring-ai-lab.git`
- 提交历史可看到 `chore: initial commit`

## 5. 仓库迁移提示（可选处理）

若推送时出现仓库迁移提示，可更新远程地址：

```bash
git remote set-url origin "git@github.com:JiOnLin/spring-ai-lab.git"
git remote -v
```
