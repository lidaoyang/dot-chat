package com.dot.msg.chat.tio.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.tio.core.ChannelContext;
import org.tio.core.TioConfig;
import org.tio.core.intf.Packet;
import org.tio.core.stat.IpStat;
import org.tio.core.stat.IpStatListener;
import org.tio.utils.json.Json;

/**
 * IP监控,可以加入黑名单
 */
@Slf4j
@Component
public class JRIpStatListener implements IpStatListener {

    @Override
    public void onExpired(TioConfig tioConfig, IpStat ipStat) {
        //在这里把统计数据入库中或日志
        if (log.isInfoEnabled()) {
            log.info("可以把统计数据入库--{}", Json.toFormatedJson(ipStat));
        }
    }

    @Override
    public void onAfterConnected(ChannelContext channelContext, boolean isConnected, boolean isReconnect, IpStat ipStat) {
        // if (log.isInfoEnabled()) {
        //     log.info("onAfterConnected--{}", isReconnect);
        // }
    }

    @Override
    public void onDecodeError(ChannelContext channelContext, IpStat ipStat) {
        // if (log.isInfoEnabled()) {
        //     log.info("onDecodeError--{}", Json.toFormatedJson(ipStat));
        // }
    }

    @Override
    public void onAfterSent(ChannelContext channelContext, Packet packet, boolean isSentSuccess, IpStat ipStat) {
        // if (log.isInfoEnabled()) {
        //     log.info("onAfterSent--{}--{}", packet.logstr(), packet);
        // }
    }

    @Override
    public void onAfterDecoded(ChannelContext channelContext, Packet packet, int packetSize, IpStat ipStat) {
        // if (log.isInfoEnabled()) {
        //     log.info("onAfterDecoded--{}--{}", packet.logstr(), packetSize);
        // }
    }

    @Override
    public void onAfterReceivedBytes(ChannelContext channelContext, int receivedBytes, IpStat ipStat) {
        // if (log.isInfoEnabled()) {
        //     log.info("onAfterReceivedBytes--{}", ipStat.getFormatedDuration());
        // }
    }

    @Override
    public void onAfterHandled(ChannelContext channelContext, Packet packet, IpStat ipStat, long cost) {
        // if (log.isInfoEnabled()) {
        //     log.info("onAfterHandled--{}--{}", packet.logstr(), packet);
        // }
    }

}
