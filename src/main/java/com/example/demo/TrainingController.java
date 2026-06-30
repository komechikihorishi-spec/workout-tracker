package com.example.demo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import tools.jackson.databind.ObjectMapper;

@Controller
public class TrainingController {

    @Autowired private TrainingRepository repository;
    @Autowired private BodyMetricsRepository metricsRepository;
    @Autowired private ObjectMapper objectMapper; // ★自動保存やグラフ用に導入

    // 🏠 トップページから直接筋トレ画面へ
    @GetMapping("/")
    public String root() {
        return "redirect:/training";
    }

    // --- メイン画面（日付ごとの作業・履歴仕分け） ---
    @GetMapping("/training")
    public String index(@RequestParam(required = false) String date, Model model) {
        LocalDate targetDate = (date != null && !date.isEmpty()) ? LocalDate.parse(date) : LocalDate.now();
        List<TrainingLog> allLogs = repository.findAllByOrderByCreatedAtDesc();
        
        for (TrainingLog log : allLogs) {
            if (log.getCreatedAt() == null) { log.setCreatedAt(LocalDateTime.now()); }
        }
        
        model.addAttribute("todayLogs", allLogs.stream().filter(l -> l.getCreatedAt().toLocalDate().equals(targetDate) && !l.isCompleted()).toList());
        model.addAttribute("logs", allLogs.stream().filter(l -> l.isCompleted() || !l.getCreatedAt().toLocalDate().equals(targetDate)).toList());
        model.addAttribute("targetDate", targetDate);
        model.addAttribute("metrics", metricsRepository.findByDate(targetDate).orElse(new BodyMetrics()));
        return "training_index";
    }

    // --- トレーニング自動保存 API ---
    @PostMapping("/training/api/autosave")
    @ResponseBody
    public String autosave(@RequestBody TrainingLog log) {
        TrainingLog existing = repository.findById(log.getId()).orElse(null);
        if (existing != null) {
            existing.setExercise(log.getExercise());
            existing.setCategory(log.getCategory());
            existing.setDurationMinutes(log.getDurationMinutes());
            existing.setMemo(log.getMemo());
            if (log.getCreatedAt() != null) { existing.setCreatedAt(log.getCreatedAt()); }
            existing.getSets().clear();
            if (log.getSets() != null) {
                for (TrainingSet s : log.getSets()) {
                    s.setTrainingLog(existing);
                    if (s.getWeight() == null) s.setWeight(0.0);
                    existing.getSets().add(s);
                }
            }
            repository.save(existing);
        }
        return "OK";
    }

    // --- 身体指標自動保存 API ---
    @PostMapping("/training/api/metrics")
    @ResponseBody
    public String saveMetrics(@RequestBody BodyMetrics data) {
        LocalDate targetDate = (data.getDate() != null) ? data.getDate() : LocalDate.now();
        BodyMetrics m = metricsRepository.findByDate(targetDate).orElse(new BodyMetrics());
        m.setDate(targetDate);
        m.setWeight(data.getWeight()); m.setBodyFat(data.getBodyFat());
        m.setMuscleMass(data.getMuscleMass()); m.setWater(data.getWater()); m.setBmi(data.getBmi());
        metricsRepository.save(m);
        return "OK";
    }

    // --- カレンダー用データ供給 API ---
    @GetMapping("/training/api/calendar")
    @ResponseBody
    public List<Map<String, String>> getCalendar() {
        List<TrainingLog> allLogs = repository.findAll();
        Map<String, Map<String, String>> uniqueEvents = new HashMap<>();
        for (TrainingLog log : allLogs) {
            String category = log.getCategory();
            if (category == null || category.isEmpty() || category.equals("記録")) continue;
            if (log.getCreatedAt() == null) continue;
            String date = log.getCreatedAt().toLocalDate().toString();
            String key = date + "-" + category;
            if (!uniqueEvents.containsKey(key)) {
                Map<String, String> event = new HashMap<>();
                event.put("title", category); event.put("start", date);
                event.put("color", getCategoryColor(category));
                uniqueEvents.put(key, event);
            }
        }
        return new ArrayList<>(uniqueEvents.values());
    }

    private String getCategoryColor(String category) {
        switch (category) {
            case "胸": return "#ff5e5e"; case "背中": return "#f1c40f";
            case "肩": return "#3498db"; case "腕": return "#9b59b6";
            case "脚": return "#2ecc71"; case "腹": return "#e67e22";
            default: return "#95a5a6";
        }
    }

    // --- 各種操作・画面遷移 ---
    @GetMapping("/training/add-empty")
    public String addEmpty(@RequestParam(required = false) String date) {
        LocalDate targetDate = (date != null && !date.isEmpty()) ? LocalDate.parse(date) : LocalDate.now();
        TrainingLog log = new TrainingLog();
        log.setExercise("");
        log.setCreatedAt(targetDate.atTime(java.time.LocalTime.now()));
        TrainingSet s = new TrainingSet(); s.setNumber(1); s.setTrainingLog(log); log.getSets().add(s);
        repository.save(log);
        return "redirect:/training?date=" + targetDate;
    }

    @PostMapping("/training/complete/{id}")
    public String complete(@PathVariable("id") Long id, @RequestParam String date) {
        TrainingLog log = repository.findById(id).orElseThrow();
        log.setCompleted(true); repository.save(log);
        return "redirect:/training?date=" + date;
    }

    @PostMapping("/training/delete/{id}")
    public String delete(@PathVariable("id") Long id, @RequestParam String date) {
        repository.deleteById(id);
        return "redirect:/training?date=" + date;
    }

    // ★修正：データを安全なJSON形式に変換して、画面が絶対にフリーズしないようにしました
    @GetMapping("/training/graph")
    public String graph(@RequestParam(required = false) String exercise, Model model) throws Exception {
        model.addAttribute("exercises", repository.findDistinctExercises());
        
        if (exercise != null && !exercise.isEmpty()) {
            List<TrainingLog> logs = repository.findByExerciseOrderByCreatedAtAsc(exercise);
            model.addAttribute("labelsJson", objectMapper.writeValueAsString(logs.stream().map(l -> l.getCreatedAt().toLocalDate().toString()).toList()));
            boolean isBodyweight = logs.stream().flatMap(l -> l.getSets().stream()).allMatch(s -> s.getWeight() == null || s.getWeight() == 0);
            
            List<?> chartData = logs.stream().map(l -> isBodyweight ? 
                l.getSets().stream().mapToInt(s -> s.getReps() != null ? s.getReps() : 0).max().orElse(0) :
                l.getSets().stream().mapToDouble(s -> s.getWeight() != null ? s.getWeight() : 0).max().orElse(0)).toList();
            
            model.addAttribute("dataJson", objectMapper.writeValueAsString(chartData));
            model.addAttribute("yLabel", isBodyweight ? "最大回数 (回)" : "最大重量 (kg)");
            model.addAttribute("selectedExercise", exercise);
        }
        
        List<BodyMetrics> mList = metricsRepository.findAllByOrderByDateAsc();
        model.addAttribute("mLabelsJson", objectMapper.writeValueAsString(mList.stream().map(m -> m.getDate().toString()).toList()));
        model.addAttribute("mWeightJson", objectMapper.writeValueAsString(mList.stream().map(BodyMetrics::getWeight).toList()));
        model.addAttribute("mBodyFatJson", objectMapper.writeValueAsString(mList.stream().map(BodyMetrics::getBodyFat).toList()));
        model.addAttribute("mMuscleJson", objectMapper.writeValueAsString(mList.stream().map(BodyMetrics::getMuscleMass).toList()));
        model.addAttribute("mWaterJson", objectMapper.writeValueAsString(mList.stream().map(BodyMetrics::getWater).toList()));
        model.addAttribute("mBmiJson", objectMapper.writeValueAsString(mList.stream().map(BodyMetrics::getBmi).toList()));
        return "training_graph";
    }

    @GetMapping("/training/day/{date}")
    public String dayDetail(@PathVariable("date") String date, Model model) {
        LocalDate targetDate = LocalDate.parse(date);
        List<TrainingLog> dayLogs = repository.findAll().stream().filter(l -> l.getCreatedAt().toLocalDate().equals(targetDate)).toList();
        model.addAttribute("targetDate", targetDate);
        model.addAttribute("logs", dayLogs);
        model.addAttribute("metrics", metricsRepository.findByDate(targetDate).orElse(null));
        return "training_day";
    }

    @GetMapping("/training/edit/{id}")
    public String edit(@PathVariable("id") Long id, Model model) {
        TrainingLog log = repository.findById(id).orElseThrow();
        if (log.getCreatedAt() == null) { log.setCreatedAt(LocalDateTime.now()); }
        model.addAttribute("trainingLog", log);
        return "training_edit";
    }

    @GetMapping("/training/calendar")
    public String calendarPage() { return "training_calendar"; }
}