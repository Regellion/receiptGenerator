package com.receipt.generator.controller;

import com.receipt.generator.service.ReceiptService;
import lombok.AllArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class ReceiptController {
    private final ReceiptService receiptService;

    @KafkaListener(topics = "requestReceiptCreation")
    public void generateReceipt(ConsumerRecord<Long, String> record) {
        receiptService.generateReceipt(record);
    }

    @GetMapping("/loadfile/{receiptName}")
    public File getReceipt(@PathVariable String receiptName) {
        return receiptService.getReceipt(receiptName);
    }
}
