import cv2
from tempfile import NamedTemporaryFile

def process_image(file):
    # Open temporary file to write the uploaded image
    with NamedTemporaryFile(delete=False) as temp_image:
        temp_image.write(file.file.read())
        temp_image_path = temp_image.name    
    # Read the image using OpenCV
    image = cv2.imread(temp_image_path)
    
    # Clean up the temporary file
    temp_image.close()
    
    return image