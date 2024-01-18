package org.jetlinks.core.message.collector;

import lombok.Getter;
import lombok.Setter;
import org.jetlinks.core.message.CommonDeviceMessageReply;
import org.jetlinks.core.message.MessageType;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

/**
 * 写入点位数据回复,由数采网关上报给平台.作为{@link WriteCollectorDataMessage}指令的回复
 *
 * @author zhouhao
 * @see WriteCollectorDataMessage
 * @since 1.2.1
 */
@Getter
@Setter
public final class WriteCollectorDataMessageReply extends CommonDeviceMessageReply<WriteCollectorDataMessageReply> {

    /**
     * 数据列表
     */
    private List<CollectorDataReply> data;

    @Override
    public MessageType getMessageType() {
        return MessageType.WRITE_COLLECTOR_DATA_REPLY;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);

        if (CollectionUtils.isEmpty(data)) {
            out.writeInt(0);
        } else {
            out.writeInt(data.size());
            for (CollectorDataReply datum : data) {
                datum.writeExternal(out);
            }
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);

        int size = in.readInt();
        this.data = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            CollectorDataReply data = new CollectorDataReply();
            data.readExternal(in);
            this.data.add(data);
        }
    }
}
