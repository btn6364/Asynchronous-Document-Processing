# Asynchronous Document Processing API

Small Spring Boot service to upload documents, persist metadata to PostgreSQL and enqueue processing jobs.

## Requirements
- Docker (version 29.1.3)
- Docker Compose (version v2.40.3-desktop.1)
- Java 21+ (to build locally)
- Maven (to build locally) — optional if using Docker build

## Quick start with Docker Compose
1. Verify the following files at the repo root:
- `docker-compose.yml` 
- `Dockerfile`
2. From the root directory of the project, close any existing service and start the PostgreSQL database and the API service:
```
docker compose down -v
docker compose up --build
```
3. The API will be accessible at `http://localhost:8080`.

## Swagger Documentation
The API documentation is available at `http://localhost:8080/swagger-ui.html` once the application is running.

## Test the API Endpoints
You can use tools like Postman to test the API endpoints. Example test files are in `data/files` folder. You can also use your own PDF files.

1.`POST /documents` - Upload a document.
- Call the endpoint http://localhost:8080/documents and include the pdf file in the request body. Here's an example of a successful request. 
![POST request](images/successful_post_documents.png)
- You can also submit multiple files and the background processing will handle them one by one.
- From the terminal, run ```docker exec -it document_db psql -U documents_user -d documents_db``` to open PostgreSQL shell. Then run ```SELECT * FROM documents;``` to see the records. You should see something like this:
![PostgreSQL data](images/done_processing.png)
- If the file doesn't exist or isn't a PDF file, you will get a 400 response.
![Missing file](images/missing_file.png)
![Only PDF](images/only_pdf.png)

2. `GET /documents/{id}` - Retrieve document metadata.
- Call the endpoint http://localhost:8080/documents/{id} and replace `id` with the document id. Here's an example of a successful request. 
![GET request](images/get_document.png)
- If the document with the given ID doesn't exist, you will get a 404 response.
![Not found](images/not_found.png)

## Architecture Trade-offs
1. Use SQL (PostgreSQL) to store metadata.
  - Advantage: ACID transactions and rich query capabilities for reliable metadata management, 
  - Trade-off: More challenging horizontal scaling compared with NoSQL (like MongoDB).
2. Store files on disk (app.files.base-dir):
  - Advantage: simple to implement. 
  - Trade-off: Hard to scale. Storing images on cloud is better for production. 
3. Layered controller/service/repository
  - Advantage: clear separation of concern and easier unit testing. 
  - Trade-off: extra complexity. 
4. Dockerize the application
  - Advantage: easy to set up and run the application with dependencies (PostgreSQL).
  - Trade-off: extra overhead on the development side.