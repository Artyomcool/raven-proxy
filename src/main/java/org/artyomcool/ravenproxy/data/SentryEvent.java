package org.artyomcool.ravenproxy.data;

import java.util.List;
import java.util.Map;

public class SentryEvent {

    private String event_id;

    private String level;
    private String message;
    private String culprit;
    private String checksum;
    private String platform;
    private Map<String, String> tags;
    private Map<String, Object> extra;

    private SentryUser user;
    private List<SentryException> exception;

    public String getEventId() {
        return event_id;
    }

    public void setEventId(String eventId) {
        this.event_id = eventId;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCulprit() {
        return culprit;
    }

    public void setCulprit(String culprit) {
        this.culprit = culprit;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public Map<String, Object> getExtra() {
        return extra;
    }

    public void setExtra(Map<String, Object> extra) {
        this.extra = extra;
    }

    public SentryUser getUser() {
        return user;
    }

    public void setUser(SentryUser user) {
        this.user = user;
    }

    public List<SentryException> getException() {
        return exception;
    }

    public void setException(List<SentryException> exception) {
        this.exception = exception;
    }
}
