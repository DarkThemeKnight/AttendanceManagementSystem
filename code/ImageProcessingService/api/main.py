from fastapi import FastAPI, UploadFile, File, Query, HTTPException,Header
from services import image_processing as ip
import logging
logger = logging.getLogger(__name__)
import cv2
import face_recognition 
from typing import List
from pydantic import BaseModel
import requests
from typing import List
from pydantic import BaseModel                          
import numpy as np

class EncodedImage(BaseModel):
    message: str
    encoded_image: List[float]

# jwt_token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJmYWNlX3JlY29nbml0aW9uX2F1dGhlbnRpY2F0aW9uIiwiaWF0IjoxNzE4ODAyMjkxLCJleHAiOjIzNDk5NTQyOTF9.UoVc6ir8xs98jrPeS46La4YsqrNeJN5HUIoDHDMJ8G8"
app = FastAPI()

@app.post("/api/v1/image-processing")
async def image_processing(student_id: str = Query(..., title="Student ID"), file: UploadFile = File(...)):
    try:
        image = ip.process_image(file) 
        img = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)       
        face_locations = face_recognition.face_locations(img)
        encode = face_recognition.face_encodings(img, face_locations)
        if(len(encode) != 1):
    
            return EncodedImage(message= "Invalid Amount of Faces detected", encoded_image = None)
        
        encoded =  EncodedImage(message= "Success", encoded_image= encode[0])
        return encoded
    except Exception as e:
        raise HTTPException(status_code=400, detail="Error processing file")
    
    
class FaceRecognitionRequest(BaseModel):
    matriculation_numbers: List[str]
    encodings : list

@app.post("/api/v1/recognize")
async def recognize_face(subject_id: str = Query(..., title="Subject Id"), file: UploadFile = File(...)):
    try:
        # Log request information
        print(f"Recognize face request received for subject ID: {subject_id}")
        
        # Send request to the endpoint to get student encodings for this subject id
        # response = requests.get("http://main_app:8080/api/v1/encodings?code=" + subject_id)
        response = requests.get("http://localhost:8080/api/v1/encodings?code=" + subject_id, verify=False)
        response.raise_for_status()  # Raise HTTPError for bad status codes
        
        val = response.json()
        matriculation_numbers = val.get("matriculation_numbers", [])
        encodings = val.get("encodings", [])
        print(f"matriculation_numbers => {matriculation_numbers} ")
        image = ip.process_image(file)
        image = cv2.resize(image, (265, 240))
        img_rgb = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
        face_locations = face_recognition.face_locations(img_rgb)
        face_encodings = face_recognition.face_encodings(img_rgb, face_locations)

        for face_encoding in face_encodings:
            matches = face_recognition.compare_faces(encodings, face_encoding)
            face_distances = face_recognition.face_distance(encodings, face_encoding)
            match_index = np.argmin(face_distances)
            if matches[match_index]:
                matriculation_number = matriculation_numbers[match_index]
                print(f"Face recognized. Student_id: {matriculation_number}")
                return {"student_id": matriculation_number}

    except requests.HTTPError as e:
        print(f"HTTP Error: {e.response.status_code}")
        raise HTTPException(status_code=e.response.status_code, detail="Error retrieving encodings")

    except Exception as e:
        print("Error processing file")
        raise HTTPException(status_code=500, detail="Error processing file")

    # If no match found
    print("No match found for the recognized face")
    return {"student_id": None}
#start with uvicorn main:app --reload
