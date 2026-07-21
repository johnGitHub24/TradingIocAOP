package com.trading.app.api;

import com.trading.app.aspect.AspectRecorder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 【職責】切面觀測 API：把六大 AOP 累積效果攤開供手動驗證與教學。
 * 【技巧】讀取 {@link AspectRecorder} 組成 LinkedHashMap JSON。
 * 【概念】AOP 常「隱形」；此端點讓橫切行為可視化——對照整合測試對 Recorder 的斷言。
 */
@RestController
@RequestMapping("/api/v1/aspects")
public class AspectReportController {

    private final AspectRecorder recorder;

    /**
     * 【職責】注入觀測器單例。
     * 【技巧】建構子注入。
     * 【概念】與所有切面共用同一 Recorder bean。
     */
    public AspectReportController(AspectRecorder recorder) {
        this.recorder = recorder;
    }

    /**
     * 【職責】回傳 logs／timings／audits／exceptions／cache／retry 觀測資料。
     * 【技巧】LinkedHashMap 保鍵順序，方便閱讀。
     * 【概念】教學與 demo 用；非正式監控系統。
     */
    @GetMapping("/report")
    public Map<String, Object> report() {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("logs", recorder.getLogs());
        report.put("timings", recorder.getTimings());
        report.put("audits", recorder.getAudits());
        report.put("exceptions", recorder.getExceptions());
        report.put("cacheHits", recorder.getCacheHits());
        report.put("cacheMisses", recorder.getCacheMisses());
        report.put("retryAttempts", recorder.getRetryAttempts());
        return report;
    }
}
