---
description: Guide through deploying or updating a project on a remote server
---

# Server Deployment/Update Guide

Help user deploy new project or update existing deployment on server.

## Prerequisites Check

1. **Verify server access**
   - Can user SSH to server?
   - Is Docker installed?
   - Is the project repo accessible from server?

2. **Check current deployment**
   - What's currently running?
   - What database is in use?
   - Any environment variables needed?

## For New Deployment

1. Clone repo to server
2. Set up `.env` file with production values
3. Run `docker-compose up -d`
4. Verify services are running
5. Check logs for errors

## For Updates (Existing Deployment)

1. **Pull latest changes**
   ```bash
   cd /path/to/project
   git pull origin main
   ```

2. **Rebuild if needed**
   ```bash
   docker-compose down
   docker-compose build --no-cache
   docker-compose up -d
   ```

3. **Database migrations** (if any)
   ```bash
   docker-compose exec app python manage.py migrate
   # or
   docker-compose exec app alembic upgrade head
   ```

4. **Verify**
   - Check `docker-compose logs -f`
   - Test key endpoints
   - Verify frontend loads

## Common Issues

- **Port conflicts**: Check `lsof -i :PORT`
- **Permission issues**: Check file ownership
- **Environment variables**: Ensure `.env` is complete
- **Database connection**: Verify credentials and host

## Output

- Step-by-step commands for user to execute
- Verification checklist
- Troubleshooting tips for common errors
