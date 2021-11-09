package org.jetlinks.core.message;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.bean.FastBeanCopier;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class CommonDeviceMessage implements DeviceMessage {
    private static final long serialVersionUID = -6849794470754667710L;

    private String code;

    private String messageId;

    private String deviceId;

    private Map<String, Object> headers;

    private long timestamp = System.currentTimeMillis();

    @Override
    @JsonIgnore
    @JSONField(serialize = false)
    public final String getThingId() {
        return getDeviceId();
    }

    @Override
    public synchronized DeviceMessage addHeader(String header, Object value) {
        if (headers == null) {
            this.headers = new ConcurrentHashMap<>();
        }
        if (header != null && value != null) {
            this.headers.put(header, value);
        }
        return this;
    }

    @Override
    public synchronized DeviceMessage addHeaderIfAbsent(String header, Object value) {
        if (headers == null) {
            this.headers = new ConcurrentHashMap<>();
        }
        if (header != null && value != null) {
            this.headers.putIfAbsent(header, value);
        }
        return this;
    }

    @Override
    public DeviceMessage removeHeader(String header) {
        if (this.headers != null) {
            this.headers.remove(header);
        }
        return this;
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = FastBeanCopier.copy(this, new JSONObject());
        json.put("messageType", getMessageType().name());
        return json;
    }

    @Override
    public void fromJson(JSONObject jsonObject) {
        DeviceMessage.super.fromJson(jsonObject);
    }

    @Override
    public String toString() {
        return toJson().toJSONString();
    }

}
