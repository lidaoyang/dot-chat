package com.dot.comm.utils;


import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.apache.commons.codec.binary.Base64;
import org.springframework.util.ResourceUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.util.EnumMap;
import java.util.Map;

/**
 * 二维码构建器
 *
 * @author: Dao-yang.
 * @date: Created in 2024/3/26 18:04
 */
public class QrCodeBuilder {
    private static final QRCodeWriter qrCodeWriter = new QRCodeWriter();
    /**
     * 必备字段
     */
    private final String qRContent;
    private final int qrSize;
    private final Map<EncodeHintType, Object> hints;

    /**
     * 核心字段
     */
    private BitMatrix bitMatrix = null;
    private BufferedImage qrImg;
    private int logoMargin;

    /**
     * 其他字段
     */
    private Color qrColor = Color.GRAY;
    private Image logoImg = null;
    private Color bgColor = Color.WHITE;

    // 图片格式  如果是png类型，logo图变成黑白的，
    private static final String format = "png";

    private void initQrCode() {
        try {
            bitMatrix = qrCodeWriter.encode(qRContent, BarcodeFormat.QR_CODE, qrSize, qrSize, hints);
            qrImg = MatrixToImageWriter.toBufferedImage(bitMatrix);
        } catch (WriterException e) {
            e.printStackTrace();
            throw new InvalidParameterException("qRContent error");
        }
    }

    /**
     * @param content 二维码编码内容
     * @param size    二维码大小
     * @param hints   配置信息
     */
    public QrCodeBuilder(String content, int size, Map<EncodeHintType, Object> hints) {
        qrSize = size;
        qRContent = content;
        this.hints = hints;
        // 二维码容错率 默认 high
        hints.computeIfAbsent(EncodeHintType.ERROR_CORRECTION, k -> ErrorCorrectionLevel.H);
        // 字符集 默认utf-8
        hints.computeIfAbsent(EncodeHintType.CHARACTER_SET, k -> "utf-8");
        // 白边大小，取值范围 0~4 默认 0
        hints.computeIfAbsent(EncodeHintType.MARGIN, k -> 0);
        initQrCode();
    }

    /**
     * @param content 二维码编码内容
     * @param size    二维码大小
     */
    public QrCodeBuilder(String content, int size) {
        this(content, size, new EnumMap<>(EncodeHintType.class));
    }

    /**
     * @param content 二维码类容
     */
    public QrCodeBuilder(String content) {
        this(content, 300);
    }

    /**
     * 给二维码设置颜色
     *
     * @param color 16进制表示的颜色
     * @return QrCodeBuilder
     */
    public QrCodeBuilder setColor(Color color) {
        this.qrColor = color;
        return this;
    }

    /**
     * 设置二维码背景颜色
     *
     * @param bgColor 16进制表示的颜色
     * @return QrCodeBuilder
     */
    public QrCodeBuilder setBgColor(Color bgColor) {
        this.bgColor = bgColor;
        return this;
    }

    /**
     * 给二维码设置Logo
     *
     * @param logo   logo
     * @param margin logo的 margin
     * @return QrCodeBuilder
     */
    public QrCodeBuilder setLogo(Image logo, int margin) {
        logoImg = logo;
        logoMargin = margin;
        return this;
    }

    public QrCodeBuilder setLogo(Image logo) {
        return setLogo(logo, 3);
    }

    private void renderColor() {
        MatrixToImageConfig matrixToImageConfig = new MatrixToImageConfig(qrColor.getRGB(), bgColor.getRGB());
        qrImg = MatrixToImageWriter.toBufferedImage(bitMatrix, matrixToImageConfig);
    }

    private void renderLogo() {
        if (logoImg == null) {
            return;
        }
        if (logoImg.getWidth(null) * 2 > qrImg.getWidth() ||
                logoImg.getHeight(null) * 2 > qrImg.getHeight()) {
            // 缩放logo
            logoImg = logoImg.getScaledInstance(
                    qrImg.getWidth() / 4,
                    qrImg.getHeight() / 4,
                    Image.SCALE_AREA_AVERAGING
            );
        }
        int logoWidth = logoImg.getWidth(null);
        int logoHeight = logoImg.getHeight(null);
        // logo 的放置位置
        int x = (qrImg.getWidth() - logoWidth) / 2;
        int y = (qrImg.getHeight() - logoHeight) / 2;
        // 绘制Logo
        Graphics2D graph = qrImg.createGraphics();
        graph.drawImage(logoImg, x, y, logoWidth, logoHeight, null);
        // 设置logo 的边框大小
        graph.setStroke(new BasicStroke(logoMargin));
        // 绘制圆角空心矩形（圆角化logo）
        graph.drawRoundRect(x, y, logoWidth, logoHeight, 18, 18);
        // 设置logo 的边框颜色
        graph.setColor(this.bgColor);
        graph.drawRect(x, y, logoWidth, logoHeight);
        graph.dispose();
        logoImg.flush();
    }

    /**
     * 构建二维码
     *
     * @return BufferedImage
     */
    public BufferedImage build() {
        // 先渲染QR颜色
        renderColor();
        // 再渲染Logo
        renderLogo();
        bitMatrix.clear();
        return qrImg;
    }

    /**
     * 生成二维码
     *
     * @param content  二维码内容
     * @param size     二维码大小
     * @param logoFile logo
     * @return 二维码图片base64
     * @throws IOException io
     */
    public static String generateQRCodeBase64(String content, int size, File logoFile)
            throws IOException {
        byte[] qrCodeByte = generateQRCodeByte(content, size, logoFile);
        return CommUtil.getBase64Image(Base64.encodeBase64String(qrCodeByte));
    }

    /**
     * 生成二维码
     *
     * @param content  二维码内容
     * @param size     二维码大小
     * @param logoFile logo
     * @return 二维码图片 byte[]
     * @throws IOException io
     */
    public static byte[] generateQRCodeByte(String content, int size, File logoFile)
            throws IOException {
        BufferedImage logo = ImageIO.read(logoFile);
        BufferedImage qr = new QrCodeBuilder(content, size)
                .setLogo(logo)
                .setColor(new Color(0x515151))
                .setBgColor(new Color(0xeaeaea))
                .build();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        if (!ImageIO.write(qr, format, stream)) {
            throw new IOException("Could not write an image of format " + format);
        }
        return stream.toByteArray();
    }

    /**
     * 生成二维码
     *
     * @param content  二维码内容
     * @param size     二维码大小
     * @param logoIs logo
     * @return 二维码图片base64
     * @throws IOException io
     */
    public static String generateQRCodeBase64(String content, int size, InputStream logoIs)
            throws IOException {
        byte[] qrCodeByte = generateQRCodeByte(content, size, logoIs);
        return CommUtil.getBase64Image(Base64.encodeBase64String(qrCodeByte));
    }

    /**
     * 生成二维码
     *
     * @param content  二维码内容
     * @param size     二维码大小
     * @param logoIs logo
     * @return 二维码图片 byte[]
     * @throws IOException io
     */
    public static byte[] generateQRCodeByte(String content, int size, InputStream logoIs)
            throws IOException {
        BufferedImage logo = ImageIO.read(logoIs);
        BufferedImage qr = new QrCodeBuilder(content, size)
                .setLogo(logo)
                .setColor(new Color(0x515151))
                .setBgColor(new Color(0xeaeaea))
                .build();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        if (!ImageIO.write(qr, format, stream)) {
            throw new IOException("Could not write an image of format " + format);
        }
        return stream.toByteArray();
    }

    /**
     * 测试方法
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        File file = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "static/ico/微信.png");
        String descPath = System.getProperty("user.home") + "/qrcode.png";
        BufferedImage logo = ImageIO.read(file);

        BufferedImage qr = new QrCodeBuilder("https://github.com/zoukun-paul/CommonApi", 300)
                .setLogo(logo)
                .setColor(new Color(0x515151))
                .build();
        ImageIO.write(qr, "png", new File(descPath));
    }
}

