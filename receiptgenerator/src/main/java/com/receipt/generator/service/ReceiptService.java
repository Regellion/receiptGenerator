package com.receipt.generator.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.io.File;

public interface ReceiptService {
    void generateReceipt(ConsumerRecord<Long, String> record);

    File getReceipt(String receiptName);
}
