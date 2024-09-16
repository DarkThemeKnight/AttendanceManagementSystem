# Face Recognition Attendance System

## Overview

The Face Recognition Attendance System is designed to automate the attendance tracking process in a school environment using facial recognition technology. This system comprises several microservices that work together to detect faces, recognize students, manage attendance records, and provide an intuitive interface for users.

## Services

### 1. Face Detection Service

- **Description**: This service is responsible for detecting faces in images or video streams.
- **Technologies**: OpenCV, Dlib, Python
- **API Endpoints**: 
  - `/detect`: Accepts images or video streams and returns coordinates of detected faces.

### 2. Face Recognition Service

- **Description**: Performs face recognition against a database of known faces to identify students.
- **Technologies**: OpenCV, Dlib, Python
- **API Endpoints**:
  - `/recognize`: Accepts detected faces and matches them against the database.

### 3. Image Processing Service

- **Description**: Handles preprocessing tasks like resizing, cropping, and normalization of images.
- **Technologies**: OpenCV, Python
- **API Endpoints**:
  - `/process`: Accepts raw images and returns processed images suitable for face detection and recognition.

### 4. Attendance Management Service

- **Description**: Manages attendance records, generates reports, and provides APIs for querying attendance information.
- **Technologies**: Java Springboot, MongoDB
- **API Endpoints**:
  - `/attendance`: CRUD operations for managing attendance records.

### 5. Authentication and Authorization Service

- **Description**: Handles user authentication and authorization.
- **Technologies**: JWT, Java Springboot, MongoDB
- **API Endpoints**:
  - `/login`: Authenticates users and generates JWT tokens    

### 6. Notification Service 

- **Description**: Sends notifications to teachers, students, and administrators about attendance updates.
- **Technologies**: STMP
- **API Endpoints**:
  - `/notify`: Sends notifications via email or SMS.

### 7. Data Persistence Service

- **Description**: Stores and retrieves data from databases.
- **Technologies**: MongoDB, PostgreSQL, MySQL
- **Database Schema**:
  - Students
  - AttendanceLogs
  - Users

### 8. Logging and Monitoring Service

- **Description**: Monitors the health and performance of the system and logs events for auditing and debugging.
- **Technologies**: ELK Stack (Elasticsearch, Logstash, Kibana), Prometheus, Grafana

