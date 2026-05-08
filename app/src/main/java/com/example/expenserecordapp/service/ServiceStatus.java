package com.example.expenserecordapp.service;

/**
 * 服务状态常量类
 * 统一管理服务相关的状态和消息
 */
public class ServiceStatus {

    // 服务状态
    public static final int STATUS_STOPPED = 0;
    public static final int STATUS_RUNNING = 1;
    public static final int STATUS_PAUSED = 2;

    // 服务消息类型
    public static final int MSG_STATUS_UPDATE = 100;
    public static final int MSG_LOG_MESSAGE = 101;
    public static final int MSG_REMINDER = 102;

    // 服务动作
    public static final String ACTION_START_SERVICE = "com.example.expenserecordapp.action.START_SERVICE";
    public static final String ACTION_STOP_SERVICE = "com.example.expenserecordapp.action.STOP_SERVICE";

    // 私有构造函数，防止实例化
    private ServiceStatus() {
        throw new IllegalStateException("Constants class");
    }
}