package com.oneby.main;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

/**
 * @ClassName AntiEpidemicSubmit
 * @Description TODO
 * @Author Oneby
 * @Date 2021/1/29 16:25
 * @Version 1.0
 */
public class AntiEpidemicSubmit {

    public static void main(String[] args) {
        String requestUrl = null; // 请求地址
        String dateTimePefix = null; // 日期前缀部分
        String fixedPart = null; // 固定不变的部分

        // 从 classpath 目录中读取配置文件
        try (InputStream is = AntiEpidemicSubmit.class.getClassLoader().getResourceAsStream("config.properties")) {
            Properties prop = new Properties();
            prop.load(is);
            requestUrl = prop.getProperty("requestUrl");
            dateTimePefix = prop.getProperty("dateTimePefix");
            fixedPart = prop.getProperty("fixedPart");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 将日期部分抽离出来
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
        String datePart = formatter.format(new Date());
        datePart = dateTimePefix + URLEncoder.encode(datePart);

        // 拼接得到请求体
        String param = datePart + fixedPart;
        String result = null;
        for (int i = 0; i < 10; i++) {
            try {
                result = rawPost(requestUrl, param);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if (result.contains("true")) {
                System.out.println(result);
                return;
            }
        }
        System.out.println("哦豁，打卡失败了~");
    }

    /***
     * @description: 使用 HttpClient 客户端发送 raw 请求
     * @param: requestUrl           请求的 url 地址
     * @param: param                请求的 raw 参数
     * @return: java.lang.String    响应体内容
     * @author Oneby
     * @date: 21:57 2021/1/29
     */
    public static String rawPost(String requestUrl, String param) throws UnsupportedEncodingException {
        // HttpClients.createDefault() 等价于 HttpClientBuilder.create().build();
        CloseableHttpClient closeableHttpClient = HttpClients.createDefault();

        // 创建执行 Post 请求的 Http 客户端
        HttpPost httpost = new HttpPost(requestUrl);

        // 设置 Content-type 为 application/x-www-form-urlencoded
        httpost.setHeader("Content-type", "application/x-www-form-urlencoded");

        // 将请求参数封装为 StringEntity 对象
        StringEntity stringEntity = new StringEntity(param);
        httpost.setEntity(stringEntity);

        String content = null;
        CloseableHttpResponse httpResponse = null;
        try {
            // 执行 Http 请求并获取 Http 响应
            httpResponse = closeableHttpClient.execute(httpost);
            HttpEntity entity = httpResponse.getEntity();
            // 获取响应体信息
            content = EntityUtils.toString(entity);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                //关闭连接、释放资源
                httpResponse.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            //关闭连接、释放资源
            closeableHttpClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content; // 返回响应体内容
    }

}
