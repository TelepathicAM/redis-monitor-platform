package com.danuvius.platform.util;

import com.danuvius.platform.model.dto.monitor.ReportEventDTO;
import com.danuvius.platform.model.dto.monitor.ReportOverviewDTO;
import com.danuvius.platform.model.dto.monitor.ReportQueryResponse;
import com.danuvius.platform.model.dto.monitor.ReportTrendDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 报告文件生成器。
 * 导出内容与查询条件一致：概览统计、指标趋势图、告警与切换明细、节点可用率。
 * 导出类型说明：
 * - excel：生成 CSV（UTF-8 带 BOM，Excel 可直接打开），含全部数据表；
 * - pdf：生成 HTML 报告（内嵌 SVG 趋势图，浏览器打开后可打印/另存为 PDF）。
 * 若后续需要标准 PDF/xlsx，可在此类中扩展（或引入 OpenPDF/EasyXML 依赖后替换生成实现），
 * 对外接口不变。
 */
@Slf4j
@Component
public class ReportFileGenerator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 生成导出文件。
     *
     * @param exportType pdf / excel
     * @return 生成的文件
     */
    public File generate(String exportType, ReportQueryResponse report, String instanceId,
                         String reportType, String exportDir) throws IOException {
        File dir = new File(exportDir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("创建导出目录失败: " + exportDir);
        }
        String suffix = "pdf".equalsIgnoreCase(exportType) ? "html" : "csv";
        File file = new File(dir, "redis-report-" + instanceId + "-" + reportType + "-"
                + System.currentTimeMillis() + "." + suffix);
        if ("pdf".equalsIgnoreCase(exportType)) {
            writeHtml(file, report, instanceId, reportType);
        } else {
            writeCsv(file, report);
        }
        return file;
    }

    // ---------------------------------------------------------------- CSV

    private void writeCsv(File file, ReportQueryResponse report) throws IOException {
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8))) {
            // UTF-8 BOM，保证 Excel 中文不乱码
            writer.write('\ufeff');

            writer.println("Redis 运行报告 - 概览统计");
            writer.println("指标,平均值,峰值,峰值时间");
            ReportOverviewDTO ov = report.getOverview();
            if (ov != null) {
                writer.println("CPU使用率(%)," + nullSafe(ov.getCpuAvg()) + "," + nullSafe(ov.getCpuMax()) + "," + fmt(ov.getCpuMaxTim()));
                writer.println("Redis内存使用量(GB)," + nullSafe(ov.getRedisMemAvg()) + "," + nullSafe(ov.getRedisMemMax()) + "," + fmt(ov.getRedisMemMaxTim()));
                writer.println("QPS(次/秒)," + nullSafe(ov.getQpsAvg()) + "," + nullSafe(ov.getQpsMax()) + "," + fmt(ov.getQpsMaxTim()));
                writer.println("平均延迟(ms)," + nullSafe(ov.getLatencyAvg()) + "," + nullSafe(ov.getLatencyMax()) + "," + fmt(ov.getLatencyMaxTim()));
                writer.println();
                writer.println("告警总次数," + nullSafe(ov.getAlarmTotal()));
                writer.println("主从切换总次数," + nullSafe(ov.getSwitchTotal()));
                writer.println("最近切换时间," + fmt(ov.getLastSwitchTime()));
                writer.println("Redis服务可用率(%)," + nullSafe(ov.getServiceAvailableRate()));
                writer.println("主节点数," + nullSafe(ov.getMasterCount()));
                writer.println("从节点数," + nullSafe(ov.getReplicaCount()));
            }
            writer.println();

            writer.println("指标趋势");
            List<ReportTrendDTO> trends = report.getTrendList();
            if (trends != null) {
                for (ReportTrendDTO trend : trends) {
                    writer.println(trend.getMetricName() + "(" + trend.getUnit() + ")");
                    writer.println("时间,数值");
                    if (trend.getTimestampList() != null && trend.getValueList() != null) {
                        int size = Math.min(trend.getTimestampList().size(), trend.getValueList().size());
                        for (int i = 0; i < size; i++) {
                            writer.println(trend.getTimestampList().get(i) + "," + nullSafe(trend.getValueList().get(i)));
                        }
                    }
                    writer.println();
                }
            }

            writer.println("告警与主从切换明细");
            writer.println("事件类型,节点,内容,发生时间,状态");
            List<ReportEventDTO> events = report.getEventList();
            if (events != null) {
                for (ReportEventDTO event : events) {
                    writer.println(csvCell(event.getEventType()) + "," + csvCell(event.getNodeDisplay()) + ","
                            + csvCell(event.getContent()) + "," + fmt(event.getHappenTime()) + ","
                            + csvCell(event.getStatus()));
                }
            }
        }
    }

    // ---------------------------------------------------------------- HTML

    private void writeHtml(File file, ReportQueryResponse report, String instanceId,
                           String reportType) throws IOException {
        ReportOverviewDTO ov = report.getOverview();
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html lang=\"zh-CN\"><head><meta charset=\"UTF-8\">")
                .append("<title>Redis 运行报告 - ").append(escape(reportType)).append("</title>")
                .append("<style>")
                .append("body{font-family:'Microsoft YaHei',Arial,sans-serif;margin:24px;color:#333;}")
                .append("h1{font-size:20px;} h2{font-size:16px;margin-top:24px;border-bottom:1px solid #ddd;padding-bottom:4px;}")
                .append(".cards{display:flex;flex-wrap:wrap;gap:12px;}")
                .append(".card{border:1px solid #e0e0e0;border-radius:6px;padding:10px 14px;min-width:150px;}")
                .append(".card .avg{font-size:20px;font-weight:bold;}")
                .append(".card .peak{font-size:12px;color:#888;}")
                .append("table{border-collapse:collapse;width:100%;font-size:12px;}")
                .append("th,td{border:1px solid #ddd;padding:4px 8px;text-align:left;}")
                .append("th{background:#f5f5f5;}")
                .append("</style></head><body>");

        sb.append("<h1>Redis 运行报告（").append(escape(reportType)).append("）</h1>");
        sb.append("<p>实例：").append(escape(instanceId)).append("　生成时间：")
                .append(LocalDateTime.now().format(FORMATTER)).append("</p>");

        sb.append("<h2>一、概览统计</h2>");
        if (ov != null) {
            sb.append("<div class=\"cards\">");
            appendCard(sb, "CPU平均使用率(%)", ov.getCpuAvg(), ov.getCpuMax(), ov.getCpuMaxTim());
            appendCard(sb, "Redis内存平均使用量(GB)", ov.getRedisMemAvg(), ov.getRedisMemMax(), ov.getRedisMemMaxTim());
            appendCard(sb, "QPS平均值(次/秒)", ov.getQpsAvg(), ov.getQpsMax(), ov.getQpsMaxTim());
            appendCard(sb, "平均延迟(ms)", ov.getLatencyAvg(), ov.getLatencyMax(), ov.getLatencyMaxTim());
            sb.append("</div>");
            sb.append("<div class=\"cards\">");
            appendPlainCard(sb, "告警总次数", ov.getAlarmTotal());
            appendPlainCard(sb, "主从切换总次数", ov.getSwitchTotal());
            appendPlainCard(sb, "最近切换时间", fmt(ov.getLastSwitchTime()));
            appendPlainCard(sb, "Redis服务可用率(%)", ov.getServiceAvailableRate());
            appendPlainCard(sb, "主节点数", ov.getMasterCount());
            appendPlainCard(sb, "从节点数", ov.getReplicaCount());
            sb.append("</div>");
        }

        sb.append("<h2>二、指标趋势</h2>");
        List<ReportTrendDTO> trends = report.getTrendList();
        if (trends != null) {
            for (ReportTrendDTO trend : trends) {
                sb.append("<h3>").append(escape(trend.getMetricName()))
                        .append("(").append(escape(trend.getUnit())).append(")</h3>");
                sb.append(buildLineChartSvg(trend));
            }
        }

        sb.append("<h2>三、告警与主从切换明细</h2>");
        sb.append("<table><tr><th>事件类型</th><th>节点</th><th>内容</th><th>发生时间</th><th>状态</th></tr>");
        List<ReportEventDTO> events = report.getEventList();
        if (events != null) {
            for (ReportEventDTO event : events) {
                sb.append("<tr><td>").append(escape(event.getEventType())).append("</td>")
                        .append("<td>").append(escape(event.getNodeDisplay())).append("</td>")
                        .append("<td>").append(escape(event.getContent())).append("</td>")
                        .append("<td>").append(escape(fmt(event.getHappenTime()))).append("</td>")
                        .append("<td>").append(escape(event.getStatus())).append("</td></tr>");
            }
        }
        sb.append("</table>");

        sb.append("</body></html>");
        Files.write(file.toPath(), sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    private void appendCard(StringBuilder sb, String name, Object avg, Object max, LocalDateTime maxTime) {
        sb.append("<div class=\"card\"><div>").append(escape(name)).append("</div>")
                .append("<div class=\"avg\">").append(nullSafe(avg)).append("</div>")
                .append("<div class=\"peak\">峰值 ").append(nullSafe(max))
                .append("　").append(escape(fmt(maxTime))).append("</div></div>");
    }

    private void appendPlainCard(StringBuilder sb, String name, Object value) {
        sb.append("<div class=\"card\"><div>").append(escape(name)).append("</div>")
                .append("<div class=\"avg\">").append(nullSafe(value)).append("</div></div>");
    }

    /**
     * 生成简易 SVG 折线图（无外部依赖）。
     */
    private String buildLineChartSvg(ReportTrendDTO trend) {
        List<String> times = trend.getTimestampList();
        List<Double> values = trend.getValueList();
        if (times == null || values == null || times.isEmpty() || values.isEmpty()) {
            return "<p>无数据</p>";
        }
        int width = 640;
        int height = 220;
        int padLeft = 50;
        int padBottom = 30;
        int padTop = 10;
        int padRight = 10;

        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        for (Double v : values) {
            if (v == null) {
                continue;
            }
            min = Math.min(min, v);
            max = Math.max(max, v);
        }
        if (min > max) {
            min = 0;
            max = 1;
        }
        if (min == max) {
            max = min + 1;
        }

        int count = Math.min(times.size(), values.size());
        double stepX = count > 1 ? (double) (width - padLeft - padRight) / (count - 1) : 0;

        StringBuilder points = new StringBuilder();
        for (int i = 0; i < count; i++) {
            Double v = values.get(i);
            if (v == null) {
                continue;
            }
            double x = padLeft + i * stepX;
            double y = padTop + (height - padTop - padBottom) * (1 - (v - min) / (max - min));
            if (points.length() > 0) {
                points.append(" ");
            }
            points.append(String.format("%.1f,%.1f", x, y));
        }

        StringBuilder svg = new StringBuilder();
        svg.append("<svg width=\"").append(width).append("\" height=\"").append(height)
                .append("\" viewBox=\"0 0 ").append(width).append(" ").append(height).append("\"")
                .append(" xmlns=\"http://www.w3.org/2000/svg\" style=\"background:#fafafa;border:1px solid #eee;\">");
        // 坐标轴
        svg.append("<line x1=\"").append(padLeft).append("\" y1=\"").append(height - padBottom)
                .append("\" x2=\"").append(width - padRight).append("\" y2=\"").append(height - padBottom)
                .append("\" stroke=\"#999\"/>");
        svg.append("<line x1=\"").append(padLeft).append("\" y1=\"").append(padTop)
                .append("\" x2=\"").append(padLeft).append("\" y2=\"").append(height - padBottom)
                .append("\" stroke=\"#999\"/>");
        // 最大值/最小值标注
        svg.append("<text x=\"").append(4).append("\" y=\"").append(padTop + 10)
                .append("\" font-size=\"10\" fill=\"#666\">").append(String.format("%.2f", max)).append("</text>");
        svg.append("<text x=\"").append(4).append("\" y=\"").append(height - padBottom)
                .append("\" font-size=\"10\" fill=\"#666\">").append(String.format("%.2f", min)).append("</text>");
        // 首尾时间标注
        svg.append("<text x=\"").append(padLeft).append("\" y=\"").append(height - 10)
                .append("\" font-size=\"10\" fill=\"#666\">").append(escape(times.get(0))).append("</text>");
        svg.append("<text x=\"").append(width - padRight - 110).append("\" y=\"").append(height - 10)
                .append("\" font-size=\"10\" fill=\"#666\">").append(escape(times.get(count - 1))).append("</text>");
        // 折线
        svg.append("<polyline points=\"").append(points).append("\" fill=\"none\" stroke=\"#d9534f\" stroke-width=\"1.5\"/>");
        svg.append("</svg>");
        return svg.toString();
    }

    // ---------------------------------------------------------------- 工具

    private String nullSafe(Object value) {
        return value == null ? "-" : value.toString();
    }

    private String fmt(LocalDateTime time) {
        return time == null ? "" : time.format(FORMATTER);
    }

    private String csvCell(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
