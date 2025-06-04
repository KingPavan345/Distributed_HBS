# Use Python image
FROM python:3.11-slim

WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

RUN mkdir -p /app/logs

EXPOSE 8001
CMD ["gunicorn", "-b", "0.0.0.0:8001", "-w", "4", "--timeout", "120", "--max-requests", "1000", "--max-requests-jitter", "100", "app:app"]
