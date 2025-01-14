package com.emsi.appmed.web;

import com.emsi.appmed.entities.Medcin;
import com.emsi.appmed.entities.Patient;
import com.emsi.appmed.entities.RendezVous;
import com.emsi.appmed.repositories.MedcinRepository;
import com.emsi.appmed.repositories.PatientRepository;
import com.emsi.appmed.repositories.RendezVousRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Controller
public class RendezVousController {
    @Autowired
    private RendezVousRepository rendezVousRepository;
    @Autowired
    private MedcinRepository medcinRepository;
    @Autowired
    private PatientRepository patientRepository;

    @GetMapping("/indexRendezVous")
    public String indexRendezVous(Model model,
                                  @RequestParam(name = "page", defaultValue = "0") int page,
                                  @RequestParam(name = "size", defaultValue = "3") int size,
                                  @RequestParam(name = "keyword", required = false) String keyword) {
        Page<RendezVous> rendezVousPage;
        if (keyword != null && !keyword.isEmpty()) {
            List<Patient> patients = patientRepository.findByNomContaining(keyword);
            List<Integer> patientIds = patients.stream().map(Patient::getId).collect(Collectors.toList());
            rendezVousPage = rendezVousRepository.findByPatientsIdIn(patientIds, PageRequest.of(page, size));
        } else {
            rendezVousPage = rendezVousRepository.findAll(PageRequest.of(page, size));
        }
        model.addAttribute("listRendezVous", rendezVousPage.getContent());
        model.addAttribute("pages", new int[rendezVousPage.getTotalPages()]);
        model.addAttribute("currentPage", page);
        model.addAttribute("patientList", patientRepository.findAll());
        model.addAttribute("keyword", keyword);
        return "rendezVous";
    }

    @GetMapping("/deleteRendezVous")
    public String deleteRendezVous(@RequestParam(name = "id") int id,
                                   @RequestParam(name = "keyword", defaultValue = "") String keyword,
                                   @RequestParam(name = "page", defaultValue = "0") int page) {
        rendezVousRepository.deleteById(id);
        return "redirect:/indexRendezVous?page=" + page + "&keyword=" + keyword;
    }

    @GetMapping("/RendezVous")
    public String index() {
        return "redirect:/indexRendezVous";
    }

    @GetMapping("/editRendezVous")
    public String editRendezVous(@RequestParam("id") int id, Model model) {
        RendezVous rendezVous = rendezVousRepository.findById(id).orElse(null);
        model.addAttribute("rendezVous", rendezVous);
        List<Patient> patientList = patientRepository.findAll();
        List<Medcin> medcinList = medcinRepository.findAll();
        model.addAttribute("patientList", patientList);
        model.addAttribute("medcinList", medcinList);
        return "formRendezVous";
    }

    @GetMapping("/formRendezVous")
    public String formRendezVous(Model model) {
        List<Medcin> medcinList = medcinRepository.findAll();
        List<Patient> patientList = patientRepository.findAll();
        model.addAttribute("rendezVous", new RendezVous());
        model.addAttribute("patientList", patientList);
        model.addAttribute("medcinList", medcinList);
        return "formRendezVous";
    }

    @PostMapping("/saveRendezVous")
    public String saveRendezVous(@ModelAttribute @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) RendezVous rendezVous, Model model) {
        Date currentDate = new Date();
        if (rendezVous.getDate().before(currentDate)) {
            List<Medcin> medcinList = medcinRepository.findAll();
            List<Patient> patientList = patientRepository.findAll();
            model.addAttribute("patientList", patientList);
            model.addAttribute("medcinList", medcinList);
            model.addAttribute("error", "La date du rendez-vous ne peut pas être dans le passé");
            return "formRendezVous";
        }
        rendezVousRepository.save(rendezVous);
        return "redirect:/RendezVous";
    }
}

