package com.backend.FaceRecognition.utils;

import java.util.Random;
import java.util.random.RandomGenerator;

public class UniqueCodeGenerator {
    public static String generateCode(int len){
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i< len; i++){
            sb.append(random.nextInt(10));
        }
        return  sb.toString();
    }
}
