import base64
import face_recognition

class ImageIO:
    def __init__(self, matriculation_number=None, im_bytes=None):
        self.matriculation_number = matriculation_number
        self.im_bytes = im_bytes

    @property
    def images(self):
        if self.im_bytes:
            return base64.b64encode(self.im_bytes).decode('utf-8')
        else:
            return None

    @images.setter
    def images(self, value):
        if value:
            self.im_bytes = base64.b64decode(value)
        else:
            self.im_bytes = None



class ProcessedImage:
    def __init__(self, matriculation_number = None, encoding=None):
        self.matriculation_number = matriculation_number
        self.encoding = encoding
    
    
    
#With python Fastapi create an api that accepts accepts files in such format in a post request 
# the path should be api/v1/image-processing
# it returns 
# public class ImageIO {
#     @JsonProperty("matriculation_number")
#     String matriculationNumber;
#     @JsonProperty("images")
#     byte[] imBytes;
# }