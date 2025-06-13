package com.kyungbae.ocr.controller;

import com.kyungbae.ocr.util.FileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RequiredArgsConstructor
@Controller
public class OCRController {

    private final FileUtil fileUtil;

    @PostMapping("/upload")
    public ResponseEntity<?> upload(String type, MultipartFile file){

        // 파일저장
        Map<String, String> map = fileUtil.fileupload("ocr", file);
        // 저장된 파일의 path : map.get("filePath") + "/" + map.get("filesystemName")

        // OCR API 호출


    }

}
