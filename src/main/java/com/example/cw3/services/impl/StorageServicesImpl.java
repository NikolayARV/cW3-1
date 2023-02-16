package com.example.cw3.services.impl;

import com.example.cw3.model.Socks;
import com.example.cw3.services.FileService;
import com.example.cw3.services.StorageServices;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

@Service
public class StorageServicesImpl implements StorageServices {
    final private FileService fileService;
    private static Map<Long, Socks> socksStorage = new HashMap<>();
    private Long id;


    public StorageServicesImpl(FileService fileService) {
        this.fileService = fileService;
    }

    @PostConstruct
    private void init() {
        try {
            readFromFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Socks addSocks(Socks socks1, Long quantity) {
        if (isSockExist(socks1)) {
            editSocks(socks1, quantity);
        } else {
            createSocks(socks1, quantity);
        }
        return socks1;
    }

    private Long idSetter() {
        Long c;
        if (socksStorage.isEmpty()) {
            c = 1L;
        } else {
            c = lastId();
        }
        return c;
    }

    private Long lastId() {
        Long c = 0L;
        for (Long id : socksStorage.keySet()) {
            if (c <= id) {
                c = id + 1L;
            }
        }
        return c;
    }

    private boolean isSockExist(Socks socks1) {
        if (socksStorage.containsValue(socks1)) {
            return true;
        } else {
            return false;
        }
    }

    private void createSocks(Socks socks1, Long quantity) {
        socks1.setQuantity(quantity);
        id = idSetter();
        socksStorage.put(id, socks1);
        saveToFile();
    }

    private void editSocks(Socks socks1, Long quantity) {
        for (Map.Entry<Long, Socks> socks : socksStorage.entrySet()) {
            if (socks1.equals(socks.getValue())) {
                Long c = quantity + socks.getValue().getQuantity();
                socks.getValue().setQuantity(c);
                saveToFile();
            }
        }
    }

    private boolean cutSocks(Socks socks1, Long count) {
        for (Socks socks : socksStorage.values()) {
            if (socks1.equals(socks) && socks.getQuantity() >= count && count>=0) {
                Long c = socks.getQuantity() - count;
                socks.setQuantity(c);
                saveToFile();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean sendSocks(Socks socks1, Long count) {
        return isSockExist(socks1) && cutSocks(socks1, count);
    }


    @Override
    public Long getSocksCount(Socks socks1) {
        Long a = 0L;
        for (Socks socks : socksStorage.values()) {
            if (socks.equals(socks1)) {
                a = socks.getQuantity();
                break;
            }
        }
        return a;
    }

    @Override
    public boolean deleteSocks(Socks socks) {
        for (Map.Entry<Long, Socks> socksEntry : socksStorage.entrySet()) {
            if (socksEntry.getValue().equals(socks)) {
                socksStorage.remove(socksEntry.getKey());
                saveToFile();
                return true;
            }
        }
        return false;
    }

    private void saveToFile() {
        try {
            String json = new ObjectMapper().writeValueAsString(socksStorage);
            fileService.saveToFile(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException();
        }
    }

    private void readFromFile() {
        try {
            String json = fileService.readFromFile();
            socksStorage = new ObjectMapper().readValue(json, new TypeReference<HashMap<Long, Socks>>() {
            });
        } catch (
                JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public Path getSocksMap() throws IOException {
        Path path = fileService.createTempFile("Socks");
        for (Map.Entry<Long, Socks> socks : socksStorage.entrySet()) {
            try (Writer writer = Files.newBufferedWriter(path, StandardOpenOption.APPEND)) {
                writer.append("Цвет: " + socks.getValue().getColor() + ". ");
                writer.append("\n");
                writer.append("Размер: " + socks.getValue().getSize());
                writer.append("\n");
                writer.append("Содержание хлопка: " + socks.getValue().getCottonPart() + "%");
                writer.append("\n");
                writer.append("Количество на складе: " + socks.getValue().getQuantity() + " пар");
                writer.append("\n");
            }
        }
        return path;
    }
}