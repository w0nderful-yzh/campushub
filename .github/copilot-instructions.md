# Project Instructions

## Tech Stack
- Java 17
- Spring Boot
- MyBatis-Plus
- MySQL
- Redis
- JWT
- Maven

## Architecture
- Use layered architecture: Controller -> Service -> Mapper
- Controllers handle HTTP request/response only
- Services contain business logic
- Mappers handle database access
- Use DTO for request and response
- Use Entity for database models
- Use VO for returned view objects when needed

## Coding Style
- Follow RESTful API design
- Use clear class and method names
- Add necessary comments for core logic
- Prefer concise and maintainable code
- Validate request parameters
- Return unified response structure

## Authentication
- Use JWT for authentication
- Support login, register, logout
- Use interceptor or filter to validate token
- Store token blacklist or session in Redis if needed

## Database
- Use MyBatis-Plus for CRUD
- Use logical delete when appropriate
- Add createTime and updateTime fields when needed

## Output Preference
- Generate complete runnable code
- When creating a module, include:
  - Controller
  - Service
  - ServiceImpl
  - Mapper
  - Entity
  - DTO/VO
  - Config if needed