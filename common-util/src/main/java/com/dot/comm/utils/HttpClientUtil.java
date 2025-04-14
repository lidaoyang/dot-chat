package com.dot.comm.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.http.Method;
import com.alibaba.fastjson2.JSONObject;
import com.dot.comm.em.ExceptionCodeEm;
import com.dot.comm.exception.ApiException;
import com.dot.comm.utils.vo.HttpResponseVo;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author: Dao-yang.
 * @date: Created in 2022/7/25 09:27
 */
@Slf4j
public class HttpClientUtil {

    // 前缀
    private static final String PREFIX = "--";
    // 换行符
    private static final String ROW = "\r\n";
    // 产生一个边界
    private static final String BOUNDARY = UUID.randomUUID().toString().replaceAll("-", "");

    /**
     * post 请求
     *
     * @param url      URL
     * @param jsonBody json格式的body字符串参数
     * @return HttpResponseVo
     */
    public static HttpResponseVo doPost(String url, String jsonBody) {
        return doPost(url, defaultHeader(), jsonBody);
    }

    /**
     * post 请求
     *
     * @param url      URL
     * @param jsonBody json格式的body字符串参数
     * @return HttpResponseVo
     */
    public static HttpResponseVo doPost(String url, Map<String, String> headerMap, String jsonBody) {
        return doPost(url, headerMap, jsonBody, false);
    }

    /**
     * post 请求
     *
     * @param url      URL
     * @param jsonBody json格式的body字符串参数
     * @return HttpResponseVo
     */
    public static HttpResponseVo doPostAndRetBytes(String url, String jsonBody) {
        return doPostAndRetBytes(url, defaultHeader(), jsonBody);
    }

    /**
     * post 请求 返回byte数组
     *
     * @param url      URL
     * @param jsonBody json格式的body字符串参数
     * @return HttpResponseVo
     */
    public static HttpResponseVo doPostAndRetBytes(String url, Map<String, String> headerMap, String jsonBody) {
        return doPost(url, headerMap, jsonBody, true);
    }

    /**
     * post 请求
     *
     * @param url      URL
     * @param jsonBody json格式的body字符串参数
     * @param isByte   是否返回byte数组
     * @return HttpResponseVo
     */
    public static HttpResponseVo doPost(String url, Map<String, String> headerMap, String jsonBody, boolean isByte) {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        return doPost(httpclient, url, headerMap, jsonBody, isByte);
    }

    /**
     * post 请求
     *
     * @param httpclient httpclient
     * @param url        URL
     * @param jsonBody   json格式的body字符串参数
     * @return HttpResponseVo
     */
    public static HttpResponseVo doPost(CloseableHttpClient httpclient, String url, String jsonBody) {
        return doPost(httpclient, url, defaultHeader(), jsonBody);
    }

    /**
     * post 请求 返回byte数组
     *
     * @param httpclient httpclient
     * @param url        URL
     * @param jsonBody   json格式的body字符串参数
     * @return HttpResponseVo
     */
    public static HttpResponseVo doPost(CloseableHttpClient httpclient, String url,
                                        Map<String, String> headerMap, String jsonBody) {
        return doPost(httpclient, url, headerMap, jsonBody, false);
    }

    /**
     * post 请求
     *
     * @param httpclient httpclient
     * @param url        URL
     * @param jsonBody   json格式的body字符串参数
     * @param isByte     是否返回byte数组
     * @return HttpResponseVo
     */
    public static HttpResponseVo doPost(CloseableHttpClient httpclient, String url, Map<String, String> headerMap,
                                        String jsonBody, boolean isByte) {
        HttpResponseVo response = new HttpResponseVo();
        response.setMethod(Method.POST.name());

        HttpPost post = new HttpPost(url);
        CloseableHttpResponse res = null;
        try {
            addHeader(headerMap, post);
            if (StringUtils.isNotBlank(jsonBody)) {
                StringEntity stringEntity = new StringEntity(jsonBody, "UTF-8");
                post.setEntity(stringEntity);
            }
            res = httpclient.execute(post);
            response.setStatus(res.getStatusLine().getStatusCode());
            HttpEntity entity = res.getEntity();
            if (entity == null) {
                return response;
            }
            setResponseVo(isByte, entity, response);
        } catch (Exception e) {
            log.error("post请求异常", e);
            response.setStatus(500);
            throw new ApiException(e);
        } finally {
            try {
                if (res != null) {
                    res.close();
                }
                httpclient.close();
            } catch (IOException e) {
                log.error("关闭res流异常", e);
            }
        }
        return response;
    }

    private static void setResponseVo(boolean isByte, HttpEntity entity, HttpResponseVo response) throws IOException {
        if (isByte) {
            byte[] bytes = EntityUtils.toByteArray(entity);
            response.setBytesBody(bytes);
        } else {
            String result = EntityUtils.toString(entity);// 返回json格式：
            response.setHttpBody(result);
            if (result.startsWith("{") && result.endsWith("}")) {
                JSONObject jsonObject = JSONObject.parseObject(result);
                response.setJsonObject(jsonObject);
                if (jsonObject.containsKey("code")) {
                    response.setCode(jsonObject.getString("code"));
                }
            }
        }
    }

    /**
     * get 请求
     *
     * @param url URL
     * @return HttpResponseVo
     */
    public static HttpResponseVo doGet(String url) {
        return doGet(url, null, false);
    }

    /**
     * get 请求
     *
     * @param url       URL
     * @param headerMap header参数
     * @return HttpResponseVo
     */
    public static HttpResponseVo doGet(String url, Map<String, String> headerMap, boolean isByte) {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        return doGet(httpclient, url, headerMap, isByte);
    }


    /**
     * get 请求
     *
     * @param httpclient httpclient
     * @param url        URL
     * @return HttpResponseVo
     */
    public static HttpResponseVo doGet(CloseableHttpClient httpclient, String url) {
        return doGet(httpclient, url, defaultHeader(), false);
    }

    /**
     * get 请求
     *
     * @param httpclient httpclient
     * @param url        URL
     * @param headerMap  header参数
     * @return HttpResponseVo
     */
    public static HttpResponseVo doGet(CloseableHttpClient httpclient, String url, Map<String, String> headerMap,
                                       boolean isByte) {
        HttpResponseVo response = new HttpResponseVo();
        response.setMethod(Method.GET.name());

        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse res = null;
        try {
            addHeader(headerMap, httpGet);
            res = httpclient.execute(httpGet);
            HttpEntity entity = res.getEntity();
            if (isByte) {
                byte[] bytes = EntityUtils.toByteArray(entity);
                response.setBytesBody(bytes);
            } else {
                String result = EntityUtils.toString(entity);// 返回json格式：
                response.setHttpBody(result);
                response.setStatus(res.getStatusLine().getStatusCode());
                response.setCode("-1");
                JSONObject jsonObject = new JSONObject();
                if (result.startsWith("{")) {
                    jsonObject = JSONObject.parseObject(result);
                    if (jsonObject.containsKey("code")) {
                        response.setCode(jsonObject.getString("code"));
                    }
                }
                response.setJsonObject(jsonObject);
            }
        } catch (Exception e) {
            response.setStatus(500);
            log.error("get请求异常", e);
            throw new ApiException(e);
        } finally {
            try {
                if (res != null) {
                    res.close();
                }
                httpclient.close();
            } catch (IOException e) {
                log.error("关闭res流异常", e);
            }
        }
        return response;
    }

    /**
     * get 请求，返回byte数组
     *
     * @param url URL
     * @return HttpResponseVo
     */
    public static HttpResponseVo doGetAndRetBytes(String url) {
        return doGet(url, null, true);
    }


    /**
     * 微信上传素材的请求方法
     *
     * @param url       URL
     * @param mediaFile 上传文件
     * @return HttpResponseVo
     */
    public static HttpResponseVo formDataPost(String url, MultipartFile mediaFile) {
        return formDataPost(url, "media", mediaFile, null);
    }

    /**
     * 微信上传素材的请求方法
     *
     * @param url       URL
     * @param mediaFile 上传文件
     * @param params    其他参数
     * @return HttpResponseVo
     */
    public static HttpResponseVo formDataPost(String url, MultipartFile mediaFile, Map<String, Object> params) {
        return formDataPost(url, "media", mediaFile, params);
    }

    /**
     * 微信上传素材的请求方法
     *
     * @param url           URL
     * @param mediaFile     上传文件
     * @param fileParamName 文件参数名
     * @param params        其他参数
     * @return HttpResponseVo
     */
    public static HttpResponseVo formDataPost(String url, String fileParamName, MultipartFile mediaFile,
                                              Map<String, Object> params) {
        HttpResponseVo response = new HttpResponseVo();
        try {
            // 1.建立连接
            URL urlObj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
            con.setRequestMethod("POST");
            // 1.1输入输出设置
            con.setDoInput(true);
            con.setDoOutput(true);
            // post方式不能使用缓存
            con.setUseCaches(false);
            // 1.2设置请求头信息
            con.setRequestProperty("Connection", "Keep-Alive");
            con.setRequestProperty("Charset", "UTF-8");
            // 1.3设置边界
            con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
            // 获得输出流
            OutputStream out = new DataOutputStream(con.getOutputStream());
            // 请求正文信息
            // 2.将文件头输出到微信服务器
            addFileData(mediaFile, fileParamName, out);
            // 3.将参数输出到服务器
            addParamsData(params, out);
            // 4.将结尾部分输出到微信服务器
            byte[] foot = (ROW + PREFIX + BOUNDARY + PREFIX + ROW).getBytes(StandardCharsets.UTF_8);// 定义最后数据分隔线
            out.write(foot);
            out.flush();
            out.close();

            // 5.将微信服务器返回的输入流转换成字符串
            StringBuilder buffer = new StringBuilder();
            // 定义BufferedReader输入流来读取URL的响应
            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            reader.close();
            con.disconnect();

            response.setHttpBody(buffer.toString());
            response.setJsonObject(JSONObject.parseObject(buffer.toString()));
            return response;
        } catch (Exception e) {
            log.error("发送POST请求出现异常", e);
        }
        throw new ApiException(ExceptionCodeEm.VALIDATE_FAILED, "请求微信上传媒体文件异常");
    }

    private static void addFileData(MultipartFile mediaFile, String fileParamName,
                                    OutputStream out) throws IOException {
        // 必须多两道线
        String sb = PREFIX + BOUNDARY + ROW
                    + "Content-Disposition: form-data;name=\"" + fileParamName // 文件的键名
                    + "\"; filelength=\"" + mediaFile.getSize() // 文件大小
                    + "\"; filename=\"" + mediaFile.getOriginalFilename() + "\"" + ROW // 文件名称
                    + "Content-Type:application/octet-stream; charset=UTF-8" + ROW + ROW; // 这两个换行很重要 标识文件信息的结束 后面的信息为文件流
        byte[] head = sb.getBytes(StandardCharsets.UTF_8);

        // 将表头写入输出流中：输出表头
        out.write(head);
        // 3.将文件正文部分输出到微信服务器
        // 把文件以流文件的方式 写入到微信服务器中
        DataInputStream in = new DataInputStream(mediaFile.getInputStream());
        int bytes = 0;
        byte[] bufferOut = new byte[1024];
        while ((bytes = in.read(bufferOut)) != -1) {
            out.write(bufferOut, 0, bytes);
        }
        // 文件流写完之后 需要换行表示结束
        out.write(ROW.getBytes(StandardCharsets.UTF_8));
        in.close();
    }

    private static void addParamsData(Map<String, Object> params, OutputStream out) {
        if (CollUtil.isEmpty(params)) {
            return;
        }
        params.forEach((key, value) -> {
            StringBuilder sb = new StringBuilder();
            // 先写入数据的边界标识
            sb.append(PREFIX).append(BOUNDARY).append(ROW);
            sb.append("Content-Disposition: form-data; name=\"")
                    .append(key).append("\"").append(ROW);
            // 数据类型及编码
            sb.append("Content-Type: text/plain; charset=UTF-8");
            // 连续两个换行符 表示文字的键信息部分结束
            sb.append(ROW).append(ROW);
            // 写入信息的值
            sb.append(value);
            // 表示数据的结尾
            sb.append(ROW);
            // 写入数据 键值对一起写入
            try {
                out.write(sb.toString().getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                log.error("参数写入失败,params:{}", params);
                log.error("异常信息", e);
                throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "参数写入失败");
            }
        });
    }

    public static void downLoadFile(HttpServletResponse response, String httpUrl) {
        ServletOutputStream out;
        try {
            // 与服务器建立连接
            URL url = new URL(httpUrl);
            URLConnection conn = url.openConnection();
            InputStream inputStream = conn.getInputStream();
            try {
                String fieldValue = conn.getHeaderField("Content-Disposition");
                String filename = "download.png";
                if (StringUtils.isNotBlank(fieldValue) && fieldValue.contains("filename=\"")) {
                    filename = fieldValue.substring(fieldValue.indexOf("filename=\"") + 10, fieldValue.length() - 1);
                }
                // 1.设置文件ContentType类型，这样设置，会自动判断下载文件类型
                response.setContentType("multipart/form-data");
                response.setHeader("Content-Length", String.valueOf(conn.getContentLengthLong()));
                response.addHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
            } catch (Exception e) {
                log.error("文件下载异常", e);
            }
            out = response.getOutputStream();
            // 读取文件流
            int len = 0;
            byte[] buffer = new byte[1024 * 10];
            while ((len = inputStream.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            out.flush();
            out.close();
            inputStream.close();
        } catch (Exception e) {
            log.error("文件下载异常", e);
        }
    }

    private static void addHeader(Map<String, String> headerMap, HttpRequestBase request) {
        if (headerMap != null) {
            // 循环增加header
            headerMap.forEach(request::addHeader);
        }
    }

    private static HttpRequestBase getHttpRequestBase(String url, Method method, String jsonBody) {
        HttpRequestBase request;
        if (method == Method.GET) {
            request = new HttpGet(url);
        } else {
            request = new HttpPost(url);
            if (StringUtils.isNotBlank(jsonBody)) {
                StringEntity stringEntity = new StringEntity(jsonBody, "UTF-8");
                ((HttpPost) request).setEntity(stringEntity);
            }
        }
        return request;
    }

    public static Map<String, String> defaultHeader() {
        Map<String, String> header = new HashMap<>();
        header.put("Accept", "application/json");
        header.put("Content-Type", "application/json;charset=utf-8");
        return header;
    }
}
