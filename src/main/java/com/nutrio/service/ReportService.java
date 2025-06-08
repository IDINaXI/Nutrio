package com.nutrio.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
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
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.ChartUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;

@Service
public class ReportService {
    @Autowired private UserRepository userRepository;
    @Autowired private WeightEntryRepository weightEntryRepository;
    @Autowired private BodyMeasurementRepository bodyMeasurementRepository;
    @Autowired private PillReminderRepository pillReminderRepository;

    public byte[] generateUserReport(Long userId, LocalDate from, LocalDate to) throws Exception {
        User user = userRepository.findById(userId).orElseThrow();
        List<WeightEntry> weights = weightEntryRepository.findByUserIdOrderByDateAsc(userId)
                .stream().filter(w -> !w.getDate().isBefore(from) && !w.getDate().isAfter(to)).toList();
        List<BodyMeasurement> measures = bodyMeasurementRepository.findByUserIdOrderByDateAsc(userId)
                .stream().filter(m -> !m.getDate().isBefore(from) && !m.getDate().isAfter(to)).toList();
        List<PillReminder> pills = pillReminderRepository.findByUserId(userId);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document();
        PdfWriter.getInstance(doc, baos);
        doc.open();
        doc.add(new Paragraph("Отчёт для врача по пользователю: " + user.getName()));
        doc.add(new Paragraph("Период: " + from + " — " + to));
        doc.add(new Paragraph("Email: " + user.getEmail()));
        doc.add(new Paragraph("\n"));

        // График веса
        if (!weights.isEmpty()) {
            doc.add(new Paragraph("График веса:"));
            XYSeries series = new XYSeries("Вес");
            for (int i = 0; i < weights.size(); i++) {
                series.add(i + 1, weights.get(i).getWeight());
            }
            XYSeriesCollection dataset = new XYSeriesCollection(series);
            JFreeChart chart = ChartFactory.createXYLineChart(
                    "Вес за период",
                    "День",
                    "Вес (кг)",
                    dataset,
                    PlotOrientation.VERTICAL,
                    false, true, false
            );
            BufferedImage chartImage = chart.createBufferedImage(500, 300);
            ByteArrayOutputStream chartBaos = new ByteArrayOutputStream();
            ChartUtils.writeBufferedImageAsPNG(chartBaos, chartImage);
            Image chartImg = Image.getInstance(chartBaos.toByteArray());
            doc.add(chartImg);
            doc.add(new Paragraph("\n"));
        }

        // График объёмов тела
        if (!measures.isEmpty()) {
            doc.add(new Paragraph("График объёмов тела:"));
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (int i = 0; i < measures.size(); i++) {
                BodyMeasurement m = measures.get(i);
                String date = m.getDate().toString();
                if (m.getWaist() != null) dataset.addValue(m.getWaist(), "Талия", date);
                if (m.getChest() != null) dataset.addValue(m.getChest(), "Грудь", date);
                if (m.getHips() != null) dataset.addValue(m.getHips(), "Бёдра", date);
                if (m.getArm() != null) dataset.addValue(m.getArm(), "Рука", date);
                if (m.getLeg() != null) dataset.addValue(m.getLeg(), "Нога", date);
            }
            JFreeChart chart = ChartFactory.createLineChart(
                    "Объёмы тела за период",
                    "Дата",
                    "Сантиметры",
                    dataset,
                    PlotOrientation.VERTICAL,
                    true, true, false
            );
            BufferedImage chartImage = chart.createBufferedImage(500, 300);
            ByteArrayOutputStream chartBaos = new ByteArrayOutputStream();
            ChartUtils.writeBufferedImageAsPNG(chartBaos, chartImage);
            Image chartImg = Image.getInstance(chartBaos.toByteArray());
            doc.add(chartImg);
            doc.add(new Paragraph("\n"));
        }

        // Лекарства (таблица)
        doc.add(new Paragraph("Список лекарств/напоминаний:"));
        com.itextpdf.text.pdf.PdfPTable pillTable = new com.itextpdf.text.pdf.PdfPTable(5);
        pillTable.addCell("Название");
        pillTable.addCell("Время");
        pillTable.addCell("Дни недели");
        pillTable.addCell("Дозировка");
        pillTable.addCell("Комментарий");
        for (PillReminder p : pills) {
            pillTable.addCell(p.getName());
            pillTable.addCell(p.getTime().toString());
            pillTable.addCell(p.getDaysOfWeek() != null ? p.getDaysOfWeek().toString() : "");
            pillTable.addCell(p.getDosage() != null ? p.getDosage() : "");
            pillTable.addCell(p.getComment() != null ? p.getComment() : "");
        }
        doc.add(pillTable);
        doc.close();
        return baos.toByteArray();
    }
} 