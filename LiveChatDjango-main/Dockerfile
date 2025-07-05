# livechat-django/Dockerfile

FROM python:3.9-slim

# 1. 시스템 패키지 설치 (예: psycopg2, libpq 등)
RUN apt-get update && apt-get install -y --no-install-recommends \
    gcc \
    libpq-dev \
    curl \
    && rm -rf /var/lib/apt/lists/*

# 2. 작업 디렉토리 설정
WORKDIR /app

# 3. 의존성 설치
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 4. 프로젝트 복사
COPY . .

# 5. collectstatic
ENV DJANGO_SETTINGS_MODULE=mysite.settings

# 6. 포트 노출
EXPOSE 8000

# 7. 실행 (ASGI 기반 Gunicorn + UvicornWorker)
CMD ["gunicorn", "mysite.asgi:application", "-k", "uvicorn.workers.UvicornWorker", "--bind", "0.0.0.0:8000"]
