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
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

@Service
public class StorageServicesImpl implements StorageServices {
    final private FileService fileService;
    private static Map<Socks, Long> socksStorage = new HashMap<>();

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
    public Socks addSocks(Socks socks1, Long count) {
        if (socksStorage.containsKey(socks1)) {
            Long c = count + socksStorage.get(socks1);
            socksStorage.put(socks1, c);
            saveToFile();
        } else {
            socksStorage.put(socks1, count);
            saveToFile();

        }
        return socks1;
    }


    @Override
    public Long sendSocks(Socks socks1, Long count) {
        if (socksStorage.containsKey(socks1) && socksStorage.get(socks1) >= count) {
            Long c = socksStorage.get(socks1) - count;
            socksStorage.put(socks1, c);
            saveToFile();
        } else {
            return 0L;
        }
        return count;
    }


    @Override
    public Long getSocksCount(Socks socks1) {
        Long a = 0L;
        if (socksStorage.containsKey(socks1)) {
            a = socksStorage.get(socks1);
        }
        return a;
    }

    @Override
    public boolean deleteSocks(Socks socks) {
        if (socksStorage.containsKey(socks)) {
            socksStorage.remove(socks);
            saveToFile();
            return true;
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
            socksStorage = new ObjectMapper().readValue(json, new TypeReference<HashMap<Socks, Long>>(){
            });
        } catch(
                JsonProcessingException e)

        {
            throw new RuntimeException(e);
        }

    }

    @Override
    public Path getSocksMap() throws IOException {
        Path path = fileService.createTempFile("Socks");
        for (Map.Entry<Socks, Long> socks : socksStorage.entrySet()) {
            try (Writer writer = Files.newBufferedWriter(path, StandardOpenOption.APPEND)) {
                writer.append("Цвет: " + socks.getKey().getColor() + ". ");
                writer.append("\n");
                writer.append("Размер: " + socks.getKey().getSize());
                writer.append("\n");
                writer.append("Содержание хлопка: " + socks.getKey().getCottonPart() + "%");
                writer.append("\n");
                writer.append("Количество на складе: " + socks.getValue() + " пар");
                writer.append("\n");
            }
        }
        return path;
    }
}