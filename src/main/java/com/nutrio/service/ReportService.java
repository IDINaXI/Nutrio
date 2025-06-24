package com.nutrio.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.nutrio.model.BodyMeasurement;
import com.nutrio.model.PillReminder;
import com.nutrio.model.User;
import com.nutrio.model.WeightEntry;
import com.nutrio.repository.BodyMeasurementRepository;
import com.nutrio.repository.PillReminderRepository;
import com.nutrio.repository.UserRepository;
import com.nutrio.repository.WeightEntryRepository;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.ChartUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReportService {
    @Autowired private UserRepository userRepository;
    @Autowired private WeightEntryRepository weightEntryRepository;
    @Autowired private BodyMeasurementRepository bodyMeasurementRepository;
    @Autowired private PillReminderRepository pillReminderRepository;

    // Цвета для дизайна
    private static final BaseColor PRIMARY_COLOR = new BaseColor(46, 204, 113); // Зелёный
    private static final BaseColor SECONDARY_COLOR = new BaseColor(52, 152, 219); // Синий
    private static final BaseColor ACCENT_COLOR = new BaseColor(155, 89, 182); // Фиолетовый
    private static final BaseColor LIGHT_GRAY = new BaseColor(245, 245, 245);
    private static final BaseColor DARK_GRAY = new BaseColor(52, 73, 94);

    // Шрифты
    private com.itextpdf.text.Font titleFont;
    private com.itextpdf.text.Font subtitleFont;
    private com.itextpdf.text.Font headerFont;
    private com.itextpdf.text.Font bodyFont;
    private com.itextpdf.text.Font smallFont;

    public ReportService() {
        try {
            // Используем Montserrat-Regular.ttf с поддержкой кириллицы
            String fontPath = "src/main/resources/fonts/FreeSans.ttf";
            BaseFont baseFont = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            titleFont = new com.itextpdf.text.Font(baseFont, 24, com.itextpdf.text.Font.BOLD);
            titleFont.setColor(PRIMARY_COLOR);
            subtitleFont = new com.itextpdf.text.Font(baseFont, 16, com.itextpdf.text.Font.BOLD);
            subtitleFont.setColor(DARK_GRAY);
            headerFont = new com.itextpdf.text.Font(baseFont, 12, com.itextpdf.text.Font.BOLD);
            headerFont.setColor(BaseColor.WHITE);
            bodyFont = new com.itextpdf.text.Font(baseFont, 10, com.itextpdf.text.Font.NORMAL);
            bodyFont.setColor(DARK_GRAY);
            smallFont = new com.itextpdf.text.Font(baseFont, 8, com.itextpdf.text.Font.NORMAL);
            smallFont.setColor(DARK_GRAY);
        } catch (Exception e) {
            // Fallback на стандартные шрифты
            titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 24, com.itextpdf.text.Font.BOLD);
            titleFont.setColor(PRIMARY_COLOR);
            subtitleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 16, com.itextpdf.text.Font.BOLD);
            subtitleFont.setColor(DARK_GRAY);
            headerFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.BOLD);
            headerFont.setColor(BaseColor.WHITE);
            bodyFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.NORMAL);
            bodyFont.setColor(DARK_GRAY);
            smallFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 8, com.itextpdf.text.Font.NORMAL);
            smallFont.setColor(DARK_GRAY);
        }
    }

    public byte[] generateUserReport(Long userId, LocalDate from, LocalDate to) throws Exception {
        User user = userRepository.findById(userId).orElseThrow();
        List<WeightEntry> weights = weightEntryRepository.findByUserIdOrderByDateAsc(userId)
                .stream().filter(w -> !w.getDate().isBefore(from) && !w.getDate().isAfter(to)).toList();
        List<BodyMeasurement> measures = bodyMeasurementRepository.findByUserIdOrderByDateAsc(userId)
                .stream().filter(m -> !m.getDate().isBefore(from) && !m.getDate().isAfter(to)).toList();
        List<PillReminder> pills = pillReminderRepository.findByUserId(userId);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 50, 50, 80, 50);
        PdfWriter.getInstance(doc, baos);
        doc.open();

        // Добавляем заголовок
        addHeader(doc, user, from, to);
        
        // Добавляем информацию о пользователе
        addUserInfo(doc, user);
        
        // Добавляем разделитель
        addSeparator(doc);

        // График веса
        if (!weights.isEmpty()) {
            addWeightChart(doc, weights, from, to);
            addSeparator(doc);
        }

        // График объёмов тела
        if (!measures.isEmpty()) {
            addBodyMeasurementsChart(doc, measures, from, to);
            addSeparator(doc);
        }

        // Таблица лекарств
        if (!pills.isEmpty()) {
            addMedicationsTable(doc, pills);
            addSeparator(doc);
        }

        // Добавляем подвал
        addFooter(doc);

        doc.close();
        return baos.toByteArray();
    }

    private void addHeader(Document doc, User user, LocalDate from, LocalDate to) throws DocumentException {
        // Логотип и название
        Paragraph title = new Paragraph("NUTRIO", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(5);
        doc.add(title);

        // Подзаголовок
        Paragraph subtitle = new Paragraph("Медицинский отчёт", subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(20);
        doc.add(subtitle);

        // Информация о периоде
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        Paragraph period = new Paragraph(
            String.format("Период: %s — %s", from.format(formatter), to.format(formatter)),
            bodyFont
        );
        period.setAlignment(Element.ALIGN_CENTER);
        period.setSpacingAfter(10);
        doc.add(period);

        // Дата генерации
        Paragraph generated = new Paragraph(
            "Отчёт сгенерирован: " + LocalDate.now().format(formatter),
            smallFont
        );
        generated.setAlignment(Element.ALIGN_CENTER);
        generated.setSpacingAfter(20);
        doc.add(generated);
    }

    private void addUserInfo(Document doc, User user) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(10);

        // Заголовок таблицы
        PdfPCell headerCell = new PdfPCell(new Phrase("Информация о пациенте", headerFont));
        headerCell.setColspan(2);
        headerCell.setBackgroundColor(PRIMARY_COLOR);
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell.setPadding(10);
        table.addCell(headerCell);

        // Данные пользователя
        addTableRow(table, "ФИО:", user.getName(), bodyFont);
        addTableRow(table, "Email:", user.getEmail(), bodyFont);
        addTableRow(table, "Возраст:", user.getAge() + " лет", bodyFont);
        addTableRow(table, "Пол:", user.getGender().toString(), bodyFont);
        addTableRow(table, "Рост:", user.getHeight() + " см", bodyFont);
        addTableRow(table, "Вес:", user.getWeight() + " кг", bodyFont);
        addTableRow(table, "Цель:", getGoalText(user.getGoal()), bodyFont);
        addTableRow(table, "Уровень активности:", getActivityText(user.getActivityLevel()), bodyFont);
        
        if (user.getAllergies() != null && !user.getAllergies().isEmpty()) {
            addTableRow(table, "Аллергии:", String.join(", ", user.getAllergies()), bodyFont);
        }

        doc.add(table);
    }

    private void addWeightChart(Document doc, List<WeightEntry> weights, LocalDate from, LocalDate to) throws Exception {
        // Заголовок раздела
        Paragraph sectionTitle = new Paragraph("Динамика веса", subtitleFont);
        sectionTitle.setSpacingBefore(20);
        sectionTitle.setSpacingAfter(10);
        doc.add(sectionTitle);

        // Создаём график
        XYSeries series = new XYSeries("Вес (кг)");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM");
        
        for (int i = 0; i < weights.size(); i++) {
            series.add(i + 1, weights.get(i).getWeight());
        }
        
        XYSeriesCollection dataset = new XYSeriesCollection(series);
        JFreeChart chart = ChartFactory.createXYLineChart(
            null, // Убираем заголовок графика
            "День измерения",
            "Вес (кг)",
            dataset,
            PlotOrientation.VERTICAL,
            false, true, false
        );

        // Настраиваем внешний вид графика
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(new Color(200, 200, 200));
        plot.setRangeGridlinePaint(new Color(200, 200, 200));
        
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, new Color(PRIMARY_COLOR.getRed(), PRIMARY_COLOR.getGreen(), PRIMARY_COLOR.getBlue()));
        renderer.setSeriesStroke(0, new BasicStroke(3.0f));
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesShapesFilled(0, true);
        plot.setRenderer(renderer);

        // Добавляем график в документ
        BufferedImage chartImage = chart.createBufferedImage(500, 300);
        ByteArrayOutputStream chartBaos = new ByteArrayOutputStream();
        ChartUtils.writeBufferedImageAsPNG(chartBaos, chartImage);
        com.itextpdf.text.Image chartImg = com.itextpdf.text.Image.getInstance(chartBaos.toByteArray());
        chartImg.setAlignment(Element.ALIGN_CENTER);
        doc.add(chartImg);

        // Добавляем статистику
        if (weights.size() > 1) {
            double minWeight = weights.stream().mapToDouble(w -> w.getWeight()).min().orElse(0);
            double maxWeight = weights.stream().mapToDouble(w -> w.getWeight()).max().orElse(0);
            double avgWeight = weights.stream().mapToDouble(w -> w.getWeight()).average().orElse(0);
            double change = weights.get(weights.size() - 1).getWeight() - weights.get(0).getWeight();
            
            PdfPTable statsTable = new PdfPTable(4);
            statsTable.setWidthPercentage(100);
            statsTable.setSpacingBefore(10);
            
            addStatsCell(statsTable, "Минимальный вес", String.format("%.1f кг", minWeight));
            addStatsCell(statsTable, "Максимальный вес", String.format("%.1f кг", maxWeight));
            addStatsCell(statsTable, "Средний вес", String.format("%.1f кг", avgWeight));
            addStatsCell(statsTable, "Изменение", String.format("%+.1f кг", change));
            
            doc.add(statsTable);
        }
    }

    private void addBodyMeasurementsChart(Document doc, List<BodyMeasurement> measures, LocalDate from, LocalDate to) throws Exception {
        // Заголовок раздела
        Paragraph sectionTitle = new Paragraph("Объёмы тела", subtitleFont);
        sectionTitle.setSpacingBefore(20);
        sectionTitle.setSpacingAfter(10);
        doc.add(sectionTitle);

        // Создаём график
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM");
        
        for (BodyMeasurement m : measures) {
            String date = m.getDate().format(formatter);
            if (m.getWaist() != null) dataset.addValue(m.getWaist(), "Талия", date);
            if (m.getChest() != null) dataset.addValue(m.getChest(), "Грудь", date);
            if (m.getHips() != null) dataset.addValue(m.getHips(), "Бёдра", date);
            if (m.getArm() != null) dataset.addValue(m.getArm(), "Рука", date);
            if (m.getLeg() != null) dataset.addValue(m.getLeg(), "Нога", date);
        }

        JFreeChart chart = ChartFactory.createLineChart(
            null,
            "Дата измерения",
            "Объём (см)",
            dataset,
            PlotOrientation.VERTICAL,
            true, true, false
        );

        // Настраиваем внешний вид графика
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(new Color(200, 200, 200));
        plot.setRangeGridlinePaint(new Color(200, 200, 200));
        
        LineAndShapeRenderer renderer = new LineAndShapeRenderer();
        renderer.setSeriesPaint(0, new Color(PRIMARY_COLOR.getRed(), PRIMARY_COLOR.getGreen(), PRIMARY_COLOR.getBlue()));
        renderer.setSeriesPaint(1, new Color(SECONDARY_COLOR.getRed(), SECONDARY_COLOR.getGreen(), SECONDARY_COLOR.getBlue()));
        renderer.setSeriesPaint(2, new Color(ACCENT_COLOR.getRed(), ACCENT_COLOR.getGreen(), ACCENT_COLOR.getBlue()));
        renderer.setSeriesPaint(3, new Color(231, 76, 60));
        renderer.setSeriesPaint(4, new Color(243, 156, 18));
        
        for (int i = 0; i < 5; i++) {
            renderer.setSeriesStroke(i, new BasicStroke(2.5f));
            renderer.setSeriesShapesVisible(i, true);
            renderer.setSeriesShapesFilled(i, true);
        }
        
        plot.setRenderer(renderer);

        // Добавляем график в документ
        BufferedImage chartImage = chart.createBufferedImage(500, 300);
        ByteArrayOutputStream chartBaos = new ByteArrayOutputStream();
        ChartUtils.writeBufferedImageAsPNG(chartBaos, chartImage);
        com.itextpdf.text.Image chartImg = com.itextpdf.text.Image.getInstance(chartBaos.toByteArray());
        chartImg.setAlignment(Element.ALIGN_CENTER);
        doc.add(chartImg);
    }

    private void addMedicationsTable(Document doc, List<PillReminder> pills) throws DocumentException {
        // Заголовок раздела
        Paragraph sectionTitle = new Paragraph("Лекарства и напоминания", subtitleFont);
        sectionTitle.setSpacingBefore(20);
        sectionTitle.setSpacingAfter(10);
        doc.add(sectionTitle);

        // Создаём таблицу
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);

        // Заголовки таблицы
        String[] headers = {"Название", "Время приёма", "Дни недели", "Дозировка", "Комментарий"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(PRIMARY_COLOR);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(8);
            cell.setBorderWidth(1);
            cell.setBorderColor(PRIMARY_COLOR);
            table.addCell(cell);
        }

        // Данные
        for (PillReminder pill : pills) {
            addPillRow(table, pill);
        }

        doc.add(table);
    }

    private void addPillRow(PdfPTable table, PillReminder pill) {
        // Название
        PdfPCell nameCell = new PdfPCell(new Phrase(pill.getName(), bodyFont));
        nameCell.setPadding(6);
        nameCell.setBorderWidth(0.5f);
        nameCell.setBorderColor(LIGHT_GRAY);
        table.addCell(nameCell);

        // Время
        PdfPCell timeCell = new PdfPCell(new Phrase(
            pill.getTime() != null ? pill.getTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "-",
            bodyFont
        ));
        timeCell.setPadding(6);
        timeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        timeCell.setBorderWidth(0.5f);
        timeCell.setBorderColor(LIGHT_GRAY);
        table.addCell(timeCell);

        // Дни недели
        String daysText = "";
        if (pill.getDaysOfWeek() != null && !pill.getDaysOfWeek().isEmpty()) {
            String[] dayNames = {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"};
            daysText = pill.getDaysOfWeek().stream()
                .map(day -> dayNames[day - 1])
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        }
        PdfPCell daysCell = new PdfPCell(new Phrase(daysText.isEmpty() ? "Ежедневно" : daysText, bodyFont));
        daysCell.setPadding(6);
        daysCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        daysCell.setBorderWidth(0.5f);
        daysCell.setBorderColor(LIGHT_GRAY);
        table.addCell(daysCell);

        // Дозировка
        PdfPCell dosageCell = new PdfPCell(new Phrase(
            pill.getDosage() != null ? pill.getDosage() : "-",
            bodyFont
        ));
        dosageCell.setPadding(6);
        dosageCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        dosageCell.setBorderWidth(0.5f);
        dosageCell.setBorderColor(LIGHT_GRAY);
        table.addCell(dosageCell);

        // Комментарий
        PdfPCell commentCell = new PdfPCell(new Phrase(
            pill.getComment() != null ? pill.getComment() : "-",
            bodyFont
        ));
        commentCell.setPadding(6);
        commentCell.setBorderWidth(0.5f);
        commentCell.setBorderColor(LIGHT_GRAY);
        table.addCell(commentCell);
    }

    private void addFooter(Document doc) throws DocumentException {
        Paragraph footer = new Paragraph(
            "© 2024 Nutrio - Система управления питанием и здоровьем\n" +
            "Данный отчёт предназначен для медицинских специалистов",
            smallFont
        );
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(30);
        doc.add(footer);
    }

    private void addSeparator(Document doc) throws DocumentException {
        LineSeparator separator = new LineSeparator();
        separator.setLineWidth(1);
        separator.setLineColor(LIGHT_GRAY);
        separator.setPercentage(50);
        separator.setAlignment(Element.ALIGN_CENTER);
        doc.add(new Chunk(separator));
        doc.add(new Paragraph(" "));
    }

    private void addTableRow(PdfPTable table, String label, String value, com.itextpdf.text.Font font) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setBackgroundColor(LIGHT_GRAY);
        labelCell.setPadding(6);
        labelCell.setBorderWidth(0.5f);
        labelCell.setBorderColor(LIGHT_GRAY);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, font));
        valueCell.setPadding(6);
        valueCell.setBorderWidth(0.5f);
        valueCell.setBorderColor(LIGHT_GRAY);
        table.addCell(valueCell);
    }

    private void addStatsCell(PdfPTable table, String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(8);
        cell.setBorderWidth(1);
        cell.setBorderColor(PRIMARY_COLOR);
        cell.setBackgroundColor(LIGHT_GRAY);
        
        Paragraph labelP = new Paragraph(label, smallFont);
        labelP.setAlignment(Element.ALIGN_CENTER);
        Paragraph valueP = new Paragraph(value, headerFont);
        valueP.setAlignment(Element.ALIGN_CENTER);
        valueP.setSpacingBefore(5);
        
        cell.addElement(labelP);
        cell.addElement(valueP);
        table.addCell(cell);
    }

    private String getGoalText(User.Goal goal) {
        switch (goal) {
            case LOSE_WEIGHT: return "Снижение веса";
            case MAINTAIN_WEIGHT: return "Поддержание веса";
            case GAIN_WEIGHT: return "Набор веса";
            default: return goal.toString();
        }
    }

    private String getActivityText(User.ActivityLevel activity) {
        switch (activity) {
            case SEDENTARY: return "Малоподвижный";
            case LIGHTLY_ACTIVE: return "Лёгкая активность";
            case MODERATELY_ACTIVE: return "Умеренная активность";
            case VERY_ACTIVE: return "Высокая активность";
            case EXTREMELY_ACTIVE: return "Очень высокая активность";
            default: return activity.toString();
        }
    }
} 