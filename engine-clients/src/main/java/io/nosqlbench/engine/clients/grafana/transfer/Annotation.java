package io.nosqlbench.engine.clients.grafana.transfer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Annotation {

    private Integer id;
    private Integer alertId;
    private Integer dashboardId;
    private Integer panelId;
    private Integer userId;
    private String userName;
    private String newState;
    private String prevState;
    private Long time;
    private Long timeEnd;
    private String text;
    private String metric;
    private String type;
    private List<String> tags = new ArrayList<String>();
    private Object data;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAlertId() {
        return alertId;
    }

    public void setAlertId(Integer alertId) {
        this.alertId = alertId;
    }

    public Integer getDashboardId() {
        return dashboardId;
    }

    public void setDashboardId(Integer dashboardId) {
        this.dashboardId = dashboardId;
    }

    public Integer getPanelId() {
        return panelId;
    }

    public void setPanelId(Integer panelId) {
        this.panelId = panelId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNewState() {
        return newState;
    }

    public void setNewState(String newState) {
        this.newState = newState;
    }

    public String getPrevState() {
        return prevState;
    }

    public void setPrevState(String prevState) {
        this.prevState = prevState;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Long getTimeEnd() {
        return timeEnd;
    }

    public void setTimeEnd(Long timeEnd) {
        this.timeEnd = timeEnd;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void setTags(String tags) {
        this.tags = Arrays.asList(tags.split("\\\\s,\\\\s"));
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Annotation{" +
                "id=" + id +
                ", alertId=" + alertId +
                ", dashboardId=" + dashboardId +
                ", panelId=" + panelId +
                ", userId=" + userId +
                ", userName='" + userName + '\'' +
                ", newState='" + newState + '\'' +
                ", prevState='" + prevState + '\'' +
                ", time=" + time +
                ", timeEnd=" + timeEnd +
                ", text='" + text + '\'' +
                ", metric='" + metric + '\'' +
                ", type='" + type + '\'' +
                ", tags=" + tags +
                ", data=" + data +
                '}';
    }
}
