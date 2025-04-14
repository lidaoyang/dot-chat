package com.dot.comm.utils;


import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * 文件监控工具类
 *
 * @author: Dao-yang.
 * @date: Created in 2025/4/8 10:24
 */
@Slf4j
public class FileMonitorTool {
    private Path filePath;
    private final FileChangeListener listener;
    private Thread monitorThread;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    /**
     * 文件变化监听器接口
     */
    public interface FileChangeListener {
        void onFileModified(String newContent);

        void onFileDeleted();

        void onError(Exception e);
    }

    public FileMonitorTool(FileChangeListener listener) {
        this.listener = listener;
    }

    /**
     * 启动文件监控
     *
     * @param fileDesc 文件路径描述符
     */
    public synchronized void start(String fileDesc) {
        if (isRunning()) {
            return;
        }
        this.filePath = Paths.get(fileDesc);
        if (!Files.exists(filePath)) {
            log.warn("文件不存在: {}", filePath);
            return;
        }
        isRunning.set(true);
        monitorThread = new Thread(this::monitorFile);
        monitorThread.setDaemon(true);
        monitorThread.setName("FileMonitor-" + filePath.getFileName());
        monitorThread.start();
    }

    /**
     * 停止文件监控
     * 该方法会设置停止标志，并尝试中断监控线程，最多等待2秒让线程结束
     */
    public synchronized void stop() {
        if (!isRunning()) {
            return;
        }

        isRunning.set(false);       // 1. 设置停止标志
        monitorThread.interrupt();  // 2. 发送中断信号（如果线程在阻塞状态）

        try {
            monitorThread.join(2000); // 3. 最多等待2秒让线程结束
            if (monitorThread.isAlive()) {
                log.warn("监控线程未在超时时间内终止");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 4. 恢复中断状态
        }
    }

    /**
     * 检查文件监控是否正在运行
     *
     * @return 返回监控是否正在运行的状态
     */
    public boolean isRunning() {
        return isRunning.get();
    }

    /**
     * 监控文件变化的核心方法
     * 该方法会注册文件所在目录的监听服务，并根据文件的变化事件（修改、删除、创建）进行相应的处理
     */
    private void monitorFile() {
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            Path dir = filePath.getParent();
            dir.register(watchService, ENTRY_MODIFY, ENTRY_DELETE, ENTRY_CREATE);

            long lastSize = Files.exists(filePath) ? Files.size(filePath) : 0;

            // 初始读取文件内容
            if (lastSize > 0) {
                notifyFileModified(readFileContent(filePath, 0, lastSize));
            }

            while (isRunning()) {
                WatchKey key;
                try {
                    key = watchService.take();
                } catch (InterruptedException e) {
                    // 正常退出
                    break;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    Path changed = ((WatchEvent<Path>) event).context();
                    if (!changed.equals(filePath.getFileName())) {
                        continue;
                    }

                    WatchEvent.Kind<?> kind = event.kind();

                    try {
                        if (kind == ENTRY_MODIFY) {
                            long newSize = Files.size(filePath);
                            if (newSize > lastSize) {
                                String newContent = readFileContent(filePath, lastSize, newSize);
                                notifyFileModified(newContent);
                                lastSize = newSize;
                            } else if (newSize < lastSize) {
                                log.warn("文件被截断或重置");
                                String fullContent = readFileContent(filePath, 0, newSize);
                                notifyFileModified("[文件被重置]" + fullContent);
                                lastSize = newSize;
                            }
                        } else if (kind == ENTRY_DELETE) {
                            notifyFileDeleted();
                            // 等待文件重新创建
                            while (isRunning.get() && !Files.exists(filePath)) {
                                Thread.sleep(500);
                            }
                            if (Files.exists(filePath)) {
                                lastSize = Files.size(filePath);
                                notifyFileModified("[文件重新创建]" + readFileContent(filePath, 0, lastSize));
                            }
                        }
                    } catch (Exception e) {
                        notifyError(e);
                    }
                }

                if (!key.reset()) {
                    notifyError(new IOException("监控键无效，可能目录不可访问"));
                    break;
                }
            }
        } catch (Exception e) {
            notifyError(e);
        } finally {
            isRunning.set(false);
        }
    }

    /**
     * 读取文件内容
     *
     * @param file  文件路径
     * @param start 读取的起始位置
     * @param end   读取的结束位置
     * @return 返回读取的文件内容
     * @throws IOException 如果读取文件时发生错误
     */
    private String readFileContent(Path file, long start, long end) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(file.toFile(), "r")) {
            raf.seek(start);
            byte[] buffer = new byte[(int) (end - start)];
            int bytesRead = raf.read(buffer);
            return new String(buffer, 0, bytesRead);
        }
    }

    /**
     * 通知文件内容更新
     *
     * @param content 更新后的文件内容
     */
    private void notifyFileModified(String content) {
        if (listener != null) {
            listener.onFileModified(content);
        }
    }

    /**
     * 通知文件被删除
     */
    private void notifyFileDeleted() {
        if (listener != null) {
            listener.onFileDeleted();
        }
    }

    /**
     * 通知监控过程中发生的错误
     *
     * @param e 异常对象
     */
    private void notifyError(Exception e) {
        if (listener != null) {
            listener.onError(e);
        }
    }

    public static void main(String[] args) {
        // 创建监控工具实例
        FileMonitorTool monitor = new FileMonitorTool(
                new FileMonitorTool.FileChangeListener() {
                    @Override
                    public void onFileModified(String newContent) {
                        System.out.println("文件内容更新:\n" + newContent);
                    }

                    @Override
                    public void onFileDeleted() {
                        System.out.println("警告: 监控文件已被删除!");
                    }

                    @Override
                    public void onError(Exception e) {
                        System.err.println("监控出错: " + e.getMessage());
                    }
                });

        // 启动监控
        monitor.start("/Users/daoyang/Downloads/file/资金流信号.csv");
        System.out.println("文件监控已启动");

        // 模拟运行一段时间后停止
        try {
            Thread.sleep(60000); // 监控60秒
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 停止监控
        monitor.stop();
        System.out.println("文件监控已停止");
    }
}