package com.kyungbae.ocr.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

@Component
public class OCRUtil {

    @Value("${ncp.clova-ocr.general.url}") // application.properties 에 해당 key값 가져오기
    private String GENERAL_OCR_URL;
    @Value("${ncp.clova-ocr.general.secretKey}")
    private String GENERAL_OCR_SECRET_KEY;
    @Value("${ncp.clova-ocr.template.url}")
    private String TEMPLATE_OCR_URL;
    @Value("${ncp.clova-ocr.template.secretKey}")
    private String TEMPLATE_OCR_SECRET_KEY;

    /**
     * NCP Clova OCR API 호출 후 응답 결과 반환용 메소드
     *
     * @param type - general|template
     * @param path - OCR 할 파일의 경로
     * @return     - OCR 응답결과
     */
    public String processOCR(String type, String path){

        final String OCR_URL = "general".equals(type) ? GENERAL_OCR_URL : "template".equals(type) ? TEMPLATE_OCR_URL : null;
        final String OCR_SECRET_KEY = "general".equals(type) ? GENERAL_OCR_SECRET_KEY : "template".equals(type) ? TEMPLATE_OCR_SECRET_KEY : null;

        try {
            URL url = new URL(OCR_URL);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setUseCaches(false);
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setReadTimeout(30000);
            con.setRequestMethod("POST");
            String boundary = "----" + UUID.randomUUID().toString().replaceAll("-", "");
            con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            con.setRequestProperty("X-OCR-SECRET", OCR_SECRET_KEY);

            /* JSON 방식
            JSONObject json = new JSONObject(); // Map과 대응
            json.put("version", "V2");
            json.put("requestId", UUID.randomUUID().toString());
            json.put("timestamp", System.currentTimeMillis());
            JSONObject image = new JSONObject();
            image.put("format", "jpg");
            image.put("name", "demo");
            JSONArray images = new JSONArray(); // List와 대응
            images.put(image);
            json.put("images", images);
            String postParams = json.toString();
             */

            // Jackson 방식
            Map<String, Object> json = new HashMap<>();
            json.put("version", "V2");
            json.put("requestId", UUID.randomUUID().toString());
            json.put("timestamp", System.currentTimeMillis());
            Map<String, Object> image = new HashMap<>();
            image.put("format", "jpg");
            image.put("name", "demo");
            List<Map<String, Object>> images = new ArrayList<>();
            images.add(image);
            json.put("images", images);

            ObjectMapper objectMapper = new ObjectMapper(); // jackson
            String postParams = objectMapper.writeValueAsString(json); // json 문자열로 반환

            con.connect();
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            long start = System.currentTimeMillis();
            File file = new File(path);
            writeMultiPart(wr, postParams, file, boundary);
            wr.close();

            int responseCode = con.getResponseCode();
            BufferedReader br;
            if (responseCode == 200) {
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();

            return response.toString(); // JSON 문자열

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void writeMultiPart(OutputStream out, String jsonMessage, File file, String boundary) throws
            IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("--").append(boundary).append("\r\n");
        sb.append("Content-Disposition:form-data; name=\"message\"\r\n\r\n");
        sb.append(jsonMessage);
        sb.append("\r\n");

        out.write(sb.toString().getBytes("UTF-8"));
        out.flush();

        if (file != null && file.isFile()) {
            out.write(("--" + boundary + "\r\n").getBytes("UTF-8"));
            StringBuilder fileString = new StringBuilder();
            fileString
                    .append("Content-Disposition:form-data; name=\"file\"; filename=");
            fileString.append("\"" + file.getName() + "\"\r\n");
            fileString.append("Content-Type: application/octet-stream\r\n\r\n");
            out.write(fileString.toString().getBytes("UTF-8"));
            out.flush();

            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[8192];
                int count;
                while ((count = fis.read(buffer)) != -1) {
                    out.write(buffer, 0, count);
                }
                out.write("\r\n".getBytes());
            }

            out.write(("--" + boundary + "--\r\n").getBytes("UTF-8"));
        }
        out.flush();
    }
}
