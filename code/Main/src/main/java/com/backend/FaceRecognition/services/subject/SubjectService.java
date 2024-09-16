package com.backend.FaceRecognition.services.subject;

import com.backend.FaceRecognition.entities.ApplicationUser;
import com.backend.FaceRecognition.entities.Subject;
import com.backend.FaceRecognition.repository.SubjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;

    public SubjectService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }
    public Optional<Subject> findSubjectByCode(String subjectCode) {
        return subjectRepository.findById(subjectCode);
    }
    public void save(Subject subject) {subjectRepository.save(subject);}
    public void deleteSubjectByCode(String subjectCode) {
        subjectRepository.deleteById(subjectCode);
    }
    public List<Subject> findAll(){
        return subjectRepository.findAll();
    }
    public Set<Subject> findAllByLecuturerInCharge(ApplicationUser lecturerInCharge){
        return subjectRepository.findAllByLecturerInCharge(lecturerInCharge);

    }
}
