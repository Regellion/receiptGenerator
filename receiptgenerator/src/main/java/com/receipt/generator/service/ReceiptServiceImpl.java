package com.receipt.generator.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.receipt.generator.dto.BookingReceiptDto;
import com.receipt.generator.dto.BookingReceiptInfoDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

@Service
@Slf4j
@AllArgsConstructor
public class ReceiptServiceImpl implements ReceiptService {
    private static final String RECEIPT_REPORT_FILEPATH = "classpath:reports/ReceiptReport/ReceiptReport.jrxml";
    private static final String PATH_TO_REPORTS_DIR = "C:\\git senla\\receipt\\";

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<Long, BookingReceiptInfoDto> kafkaTemplate;

    @Override
    public void generateReceipt(ConsumerRecord<Long, String> record) {
        BookingReceiptDto bookingReceiptDto;
        try {
            bookingReceiptDto = objectMapper.readValue(record.value(), BookingReceiptDto.class);
        } catch (JsonProcessingException e) {
            log.warn("In generateReceipt failed mapping record: {} to BookingReceiptDto.class", record);
            throw new RuntimeException("Failed mapping record");
        }
        String uuid = UUID.randomUUID().toString();
        BookingReceiptInfoDto bookingReceiptInfoDto = new BookingReceiptInfoDto(bookingReceiptDto.getId(), uuid);
        generateReport(bookingReceiptDto, uuid);
        responseBookingInfoDto(bookingReceiptInfoDto);
        log.info("In generateReceipt generated receipt with name: {} is success", uuid);
    }

    @Override
    public File getReceipt(String receiptName) {
        File file = new File(PATH_TO_REPORTS_DIR + receiptName + ".pdf");
        if (!file.exists()) {
            log.warn("In getReceipt file to file path: {}.pdf not found", PATH_TO_REPORTS_DIR + receiptName);
            throw new RuntimeException("File not found");
        }
        log.info("In getReceipt file upload success");
        return file;
    }

    private void responseBookingInfoDto(BookingReceiptInfoDto bookingReceiptInfoDto) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            log.warn("In responseBookingInfoDto Thread sleep is failed");
            throw new RuntimeException(e.getMessage());
        }
        ListenableFuture<SendResult<Long, BookingReceiptInfoDto>> future =
                kafkaTemplate.send("responseReceiptCreation", bookingReceiptInfoDto);
        future.addCallback(System.out::println, System.err::println);
        kafkaTemplate.flush();
        log.info("In responseBookingInfoDto BookingReceiptInfoDto: {} was send", bookingReceiptInfoDto);
    }

    private void generateReport(BookingReceiptDto bookingReceiptDto, String uuid) {
        try {
            List<BookingReceiptDto> bookingReceiptDtoList = Collections.singletonList(bookingReceiptDto);
            File file = ResourceUtils.getFile(RECEIPT_REPORT_FILEPATH);
            JasperReport jasperReport = JasperCompileManager.compileReport(file.getAbsolutePath());
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(bookingReceiptDtoList);
            Map<String, Object> param = new HashMap<>();
            param.put("Author", "Artem Martynov");

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, param, dataSource);
            JasperExportManager.exportReportToPdfFile(jasperPrint, PATH_TO_REPORTS_DIR + uuid + ".pdf");
            log.info("In generateReport report generated complete");
        } catch (FileNotFoundException | JRException e) {
            log.warn("In generateReport report generated failed");
            throw new RuntimeException(e.getMessage());
        }
    }
}
